package org.tbk.tor.http.client;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.berndpruenster.netlayer.tor.NativeTor;
import org.berndpruenster.netlayer.tor.TorCtlException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tbk.tor.NativeTorFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
class TorHttpClient5Test {
    // "onion.torproject.org" as onion. taken from https://onion.torproject.org/ on 2022-07-01
    private static final URI ONION_URL = URI.create("http://xao2lxsmia2edq2n5zxg6uahx6xox2t7bfjw6b5vdzsxi7ezmqob6qid.on" + "ion/");

    private static final URI CHECK_TOR_URL_HTTP = URI.create("http://check.torproject.org/");
    private static final URI CHECK_TOR_URL_HTTPS = URI.create("https://check.torproject.org/");

    private CloseableHttpClient sut;

    private NativeTor nativeTor;

    @BeforeEach
    void setUp() throws TorCtlException {
        File workingDirectory = new File("build/tmp/tor-working-dir");
        NativeTorFactory torFactory = new NativeTorFactory(workingDirectory);

        this.nativeTor = torFactory.create()
                .blockOptional(Duration.ofSeconds(30))
                .orElseThrow(() -> new IllegalStateException("Could not start tor"));

        HttpClientBuilder torHttpClientBuilder = SimpleTorHttpClient5Builder.tor(this.nativeTor);

        this.sut = torHttpClientBuilder.build();
    }

    @AfterEach
    void tearDown() {
        try {
            this.sut.close();
        } catch (IOException e) {
            log.warn("Error while closing http client in teardown phase", e);
        }

        this.nativeTor.shutdown();
    }

    @Test
    void testOnionWithTor() throws IOException {
        HttpGet req = new HttpGet(ONION_URL);

        String body = this.sut.execute(req, new BasicHttpClientResponseHandler());

        // body should contain a list of addresses - including the one we fetched from!
        assertThat(body, containsString("onion.torproject.org"));
        assertThat(body, containsString(ONION_URL.toString()));
    }

    @Test
    void testHttpWithTor() throws IOException {
        List<URI> urls = Lists.newArrayList(CHECK_TOR_URL_HTTP, CHECK_TOR_URL_HTTPS);

        for (URI url : urls) {
            HttpGet req = new HttpGet(url);

            String body = this.sut.execute(req, new BasicHttpClientResponseHandler());

            assertThat(body, containsString("Congratulations. This browser is configured to use Tor."));
            assertThat(body, not(containsStringIgnoringCase("Sorry")));
            assertThat(body, not(containsStringIgnoringCase("You are not using Tor")));
        }
    }

    @Test
    void testOnionWithoutTor() {
        HttpGet req = new HttpGet(ONION_URL);

        UnknownHostException expectedException = assertThrows(UnknownHostException.class, () -> {
            try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
                client.execute(req, new BasicHttpClientResponseHandler());
                fail("Should have thrown exception");
            }
        });

        assertThat(expectedException.getMessage(), Matchers.anyOf(
                containsString("Temporary failure in name resolution"),
                containsString("Name or service not known")
        ));
    }

    @Test
    void testHttpWithoutTor() throws IOException {
        HttpGet req = new HttpGet(CHECK_TOR_URL_HTTPS);
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            String body = client.execute(req, new BasicHttpClientResponseHandler());

            assertThat(body, containsString("Sorry. You are not using Tor."));
            assertThat(body, not(containsStringIgnoringCase("Congratulations")));
            assertThat(body, not(containsStringIgnoringCase("This browser is configured to use Tor")));
        }
    }
}