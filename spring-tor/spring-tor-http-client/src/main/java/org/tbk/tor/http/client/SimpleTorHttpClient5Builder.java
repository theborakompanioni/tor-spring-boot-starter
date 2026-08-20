package org.tbk.tor.http.client;

import com.runjva.sourceforge.jsocks.protocol.Socks5Proxy;
import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.io.SocketConfig;
import org.berndpruenster.netlayer.tor.Tor;
import org.berndpruenster.netlayer.tor.TorCtlException;

import java.net.*;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

public final class SimpleTorHttpClient5Builder {

    private SimpleTorHttpClient5Builder() {
        throw new UnsupportedOperationException();
    }

    public static HttpClientBuilder tor(Tor tor) throws TorCtlException {
        Socks5Proxy proxy = tor.getProxy("127.0.0.1");
        InetAddress inetAddress = proxy.getInetAddress();

        SocketAddress socketAddress = new InetSocketAddress(inetAddress, proxy.getPort());
        return custom(socketAddress);
    }

    /**
     * Creates an {@link HttpClientBuilder} that routes all traffic through the given SOCKS proxy address.
     *
     * <p>Uses the httpcomponents-client 5.4+ API: the SOCKS proxy is configured via
     * {@link SocketConfig#getSocksProxyAddress()}, and a custom {@link DnsResolver} returns
     * <em>unresolved</em> {@link InetSocketAddress} instances so that the SOCKS proxy (Tor)
     * performs DNS resolution instead of the local system — preventing DNS leaks.
     *
     * @param socksProxyAddress the SOCKS proxy address (e.g. Tor's SOCKS port)
     * @return an {@link HttpClientBuilder} pre-configured for Tor
     * @see <a href="https://stackoverflow.com/questions/22937983/how-to-use-socks-5-proxy-with-apache-http-client-4/25203021#25203021">SO: SOCKS 5 proxy with Apache HTTP Client</a>
     */
    public static HttpClientBuilder custom(SocketAddress socksProxyAddress) {
        requireNonNull(socksProxyAddress);

        SocketConfig socketConfig = SocketConfig.custom()
                .setSocksProxyAddress(socksProxyAddress)
                .build();

        PoolingHttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
                .setDnsResolver(new SocksProxyFakeDnsResolver())
                .setDefaultSocketConfig(socketConfig)
                .build();

        return HttpClients.custom()
                .setConnectionManager(cm);
    }

    /**
     * A DNS resolver that returns <em>unresolved</em> socket addresses so that DNS resolution
     * is delegated to the SOCKS proxy (Tor) rather than being performed locally.
     *
     * <p>The {@link #resolve(String)} method is kept for backward compatibility and returns
     * a loopback address that will never actually be used, because
     * {@link #resolve(String, int)} is the method called by
     * {@link org.apache.hc.client5.http.impl.io.DefaultHttpClientConnectionOperator} (since 5.5).
     */
    static final class SocksProxyFakeDnsResolver implements DnsResolver {

        @Override
        public InetAddress[] resolve(String host) {
            // Fallback for the legacy single-arg resolve; should not be called in 5.5+.
            return new InetAddress[]{InetAddress.getLoopbackAddress()};
        }

        @Override
        public List<InetSocketAddress> resolve(String host, int port) {
            // Return an *unresolved* address so the SOCKS proxy performs DNS resolution.
            return Collections.singletonList(InetSocketAddress.createUnresolved(host, port));
        }

        @Override
        public String resolveCanonicalHostname(String host) {
            return host;
        }
    }
}