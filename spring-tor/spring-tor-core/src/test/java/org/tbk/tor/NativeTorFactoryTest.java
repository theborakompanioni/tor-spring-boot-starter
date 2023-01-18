package org.tbk.tor;

import org.berndpruenster.netlayer.tor.NativeTor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Duration;

class NativeTorFactoryTest {

    private NativeTorFactory sut;

    @BeforeEach
    void setUp() {
        File workingDirectory = new File("build/tmp/tor-working-dir");
        this.sut = new NativeTorFactory(workingDirectory);
    }

    @Test
    void itShouldCreateTorSuccessfully() {
        NativeTor nativeTor = sut.create().blockOptional(Duration.ofSeconds(30))
                .orElseThrow(() -> new IllegalStateException("Could not start tor"));

        nativeTor.shutdown();
    }
}