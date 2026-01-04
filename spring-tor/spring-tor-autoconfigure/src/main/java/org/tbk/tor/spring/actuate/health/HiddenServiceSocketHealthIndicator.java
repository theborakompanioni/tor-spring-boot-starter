package org.tbk.tor.spring.actuate.health;

import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.berndpruenster.netlayer.tor.HiddenServiceSocket;
import org.berndpruenster.netlayer.tor.TorSocket;
import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;

import java.io.IOException;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@Slf4j
public class HiddenServiceSocketHealthIndicator extends AbstractHealthIndicator {

    private static final String STREAM_ID = "health";

    private final HiddenServiceSocket socket;

    public HiddenServiceSocketHealthIndicator(HiddenServiceSocket socket) {
        this.socket = requireNonNull(socket);
    }

    @SuppressFBWarnings(
            value = "SECCRLFLOG",
            justification = "It's acceptable to log HiddenServiceSocket details."
    )
    @Override
    protected void doHealthCheck(Health.Builder builder) {
        Map<String, Object> details = ImmutableMap.<String, Object>builder()
                .put("name", socket.getServiceName())
                .put("address", socket.getSocketAddress())
                .put("local_address", socket.getLocalSocketAddress())
                .build();

        if (log.isDebugEnabled()) {
            log.debug("Performing health check on {}", socket);
        }

        try (TorSocket s1 = new TorSocket(socket.getSocketAddress(), STREAM_ID)) {
            log.debug("Successfully performed health check on {}", socket);

            builder.up().withDetails(details);
        } catch (IOException e) {
            log.warn("Exception while performing hidden service health check: {}", e.getMessage());

            builder.outOfService()
                    .withException(e)
                    .withDetails(details);
        }
    }
}
