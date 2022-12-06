package org.tbk.tor.http.client;

import com.runjva.sourceforge.jsocks.protocol.Socks5Proxy;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.berndpruenster.netlayer.tor.Tor;
import org.berndpruenster.netlayer.tor.TorCtlException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.*;

import static java.util.Objects.requireNonNull;

public final class SimpleTorHttpClient5Builder {

    private SimpleTorHttpClient5Builder() {
        throw new UnsupportedOperationException();
    }

    public static HttpClientBuilder tor(Tor tor) throws TorCtlException {
        Socks5Proxy proxy = tor.getProxy();
        InetAddress inetAddress = proxy.getInetAddress();

        SocketAddress socketAddress = new InetSocketAddress(inetAddress, proxy.getPort());
        return custom(new Proxy(Proxy.Type.SOCKS, socketAddress));
    }

    public static HttpClientBuilder custom(Proxy proxy) {
        DefaultHostnameVerifier defaultHostnameVerifier = new DefaultHostnameVerifier();
        SSLContext sslContext = SSLContexts.createSystemDefault();
        ProxySelectorSslConnectionSocketFactory sslSocketFactory = new ProxySelectorSslConnectionSocketFactory(proxy, sslContext, defaultHostnameVerifier);
        ProxySelectorPlainConnectionSocketFactory plainSocketFactory = new ProxySelectorPlainConnectionSocketFactory(proxy);

        Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", plainSocketFactory)
                .register("https", sslSocketFactory)
                .build();

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(
                reg, PoolConcurrencyPolicy.STRICT, PoolReusePolicy.LIFO, TimeValue.NEG_ONE_MILLISECOND, null, new FakeDnsResolver(), null);

        return HttpClients.custom()
                .setConnectionManager(cm);
    }

    @SuppressFBWarnings("UNENCRYPTED_SOCKET")
    private static Socket createSocket(HttpContext context, Proxy proxyOrNull) {
        return new Socket(requireNonNull(proxyOrNull));
    }

    /*
     * It is very difficult to say to an apache http client to "not use my DNS servers while connecting through a proxy".
     *
     * Some code taken from:
     * https://stackoverflow.com/questions/22937983/how-to-use-socks-5-proxy-with-apache-http-client-4/25203021#25203021
     */
    private static class ProxySelectorPlainConnectionSocketFactory implements ConnectionSocketFactory {

        private final Proxy proxy;

        ProxySelectorPlainConnectionSocketFactory(Proxy proxy) {
            this.proxy = requireNonNull(proxy);
        }

        @Override
        public Socket createSocket(HttpContext context) {
            return SimpleTorHttpClient5Builder.createSocket(context, this.proxy);
        }

        @Override
        public Socket connectSocket(TimeValue connectTimeout, Socket sock, HttpHost host, InetSocketAddress remoteAddress, InetSocketAddress localAddress, HttpContext context) throws IOException {
            InetSocketAddress unresolvedRemote = InetSocketAddress.createUnresolved(host.getHostName(), remoteAddress.getPort());

            return PlainConnectionSocketFactory.INSTANCE.connectSocket(connectTimeout, sock, host, unresolvedRemote, localAddress, context);
        }
    }

    private static final class ProxySelectorSslConnectionSocketFactory extends SSLConnectionSocketFactory {
        private final Proxy proxy;

        ProxySelectorSslConnectionSocketFactory(Proxy proxy,
                                                SSLContext sslContext,
                                                HostnameVerifier hostnameVerifier) {
            super(sslContext, hostnameVerifier);
            this.proxy = requireNonNull(proxy);
        }

        @Override
        public Socket createSocket(HttpContext context) {
            return SimpleTorHttpClient5Builder.createSocket(context, this.proxy);
        }

        @Override
        public Socket connectSocket(
                final Socket socket,
                final HttpHost host,
                final InetSocketAddress remoteAddress,
                final InetSocketAddress localAddress,
                final Timeout connectTimeout,
                final Object attachment,
                final HttpContext context) throws IOException {
            InetSocketAddress unresolvedRemote = InetSocketAddress.createUnresolved(host.getHostName(), remoteAddress.getPort());
            return super.connectSocket(socket, host, unresolvedRemote, localAddress, connectTimeout, attachment, context);
        }
    }

    static class FakeDnsResolver implements DnsResolver {
        // Return this fake DNS record for every request, we won't be using it
        private static final InetAddress[] fakeDnsEntry = {InetAddress.getLoopbackAddress()};

        @Override
        public InetAddress[] resolve(String host) {
            return fakeDnsEntry;
        }

        @Override
        public String resolveCanonicalHostname(String host) {
            return fakeDnsEntry[0].getHostName();
        }
    }
}