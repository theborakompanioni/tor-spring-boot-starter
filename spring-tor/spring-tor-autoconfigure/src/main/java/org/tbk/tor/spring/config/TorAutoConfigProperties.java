package org.tbk.tor.spring.config;

import com.google.common.base.CharMatcher;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.net.InetAddress;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Data
@ConfigurationProperties(
        prefix = "org.tbk.tor",
        ignoreUnknownFields = false
)
public class TorAutoConfigProperties implements Validator {
    private static final boolean DEFAULT_AUTO_PUBLISH_ENABLED = true;
    private static final String DEFAULT_WORKING_DIRECTORY = "tor-working-dir";
    private static final int DEFAULT_VIRTUAL_PORT = 80; // http: 80; https: 443
    private static final Duration DEFAULT_START_TIMEOUT = Duration.ofSeconds(60);
    private static final OnionLocationHeaderProperties DEFAULT_ONION_LOCATION_HEADER = new OnionLocationHeaderProperties();

    private boolean enabled;

    private String workingDirectory;

    private Boolean autoPublishEnabled = DEFAULT_AUTO_PUBLISH_ENABLED;

    // currently only one service possible and therefore one virtual port is supported by embedded tor : /
    private Integer virtualPort;

    @DurationUnit(ChronoUnit.SECONDS)
    private Duration startupTimeout;

    private OnionLocationHeaderProperties onionLocationHeader;

    public String getWorkingDirectory() {
        return workingDirectory != null ? workingDirectory : DEFAULT_WORKING_DIRECTORY;
    }

    public boolean getAutoPublishEnabled() {
        return autoPublishEnabled != null ? autoPublishEnabled : DEFAULT_AUTO_PUBLISH_ENABLED;
    }

    public int getVirtualPort() {
        return virtualPort != null ? virtualPort : DEFAULT_VIRTUAL_PORT;
    }

    public Duration getStartupTimeout() {
        return startupTimeout != null ? startupTimeout : DEFAULT_START_TIMEOUT;
    }

    public OnionLocationHeaderProperties getOnionLocationHeader() {
        return onionLocationHeader != null ? onionLocationHeader : DEFAULT_ONION_LOCATION_HEADER;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == TorAutoConfigProperties.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        TorAutoConfigProperties properties = (TorAutoConfigProperties) target;

        String workingDirectory = properties.getWorkingDirectory();
        if (workingDirectory == null || workingDirectory.isBlank()) {
            String errorMessage = "'workingDirectory' must not be empty";
            errors.rejectValue("workingDirectory", "workingDirectory.invalid", errorMessage);
        } else if (containsWhitespaces(workingDirectory)) {
            String errorMessage = "'workingDirectory' must not contain whitespaces - unsupported value";
            errors.rejectValue("workingDirectory", "workingDirectory.unsupported", errorMessage);
        }

        if (properties.getVirtualPort() <= 0) {
            String errorMessage = "'virtualPort' must be greater than zero";
            errors.rejectValue("virtualPort", "virtualPort.invalid", errorMessage);
        }
    }

    @Data
    public static class OnionLocationHeaderProperties {
        private boolean enabled;
        private boolean allowOnLocalhostHttp;
    }

    /**
     * According to this specification an entry to torrc is written.
     * e.g.
     * <code>
     * HiddenServiceDir /var/lib/tor/my_website/
     * HiddenServicePort 80 127.0.0.1:8080
     * </code>
     */
    @Data
    public static class HiddenServiceProperties implements Validator {
        private static final int DEFAULT_VIRTUAL_PORT = 80;
        private static final String DEFAULT_HOST = InetAddress.getLoopbackAddress().getHostAddress();

        private String directory;
        private Integer virtualPort;
        // private List<Integer> virtualPorts; <-- multiple values not yet supported by netlayer
        private String host;
        private int port;

        public String getHost() {
            return host != null ? host : DEFAULT_HOST;
        }

        public int getVirtualPort() {
            return virtualPort != null ? virtualPort : DEFAULT_VIRTUAL_PORT;
        }

        @Override
        public boolean supports(Class<?> clazz) {
            return clazz == HiddenServiceProperties.class;
        }

        @Override
        public void validate(Object target, Errors errors) {
            HiddenServiceProperties properties = (HiddenServiceProperties) target;

            String directory = properties.getDirectory();
            if (directory == null || directory.isBlank()) {
                String errorMessage = "'directory' must not be empty";
                errors.rejectValue("directory", "directory.invalid", errorMessage);
            } else if (containsWhitespaces(directory)) {
                String errorMessage = "'directory' must not contain whitespaces - unsupported value";
                errors.rejectValue("directory", "directory.unsupported", errorMessage);
            }

            String host = properties.getHost();
            if (host == null || host.isBlank()) {
                String errorMessage = "'host' must not be empty";
                errors.rejectValue("host", "host.invalid", errorMessage);
            } else if (containsWhitespaces(directory)) {
                String errorMessage = "'host' must not contain whitespaces - unsupported value";
                errors.rejectValue("host", "host.unsupported", errorMessage);
            }

            if (properties.getPort() <= 0) {
                String errorMessage = "'port' must be a greater than zero";
                errors.rejectValue("port", "port.invalid", errorMessage);
            }

            if (properties.getVirtualPort() <= 0) {
                String errorMessage = "'virtualPort' must be a greater than zero";
                errors.rejectValue("virtualPort", "virtualPort.invalid", errorMessage);
            }
        }
    }

    @Data
    public static class HiddenServiceSocketProperties implements Validator {
        private String directory;
        private int port;
        private Integer virtualPort; // the "port" of the onion address

        @DurationUnit(ChronoUnit.SECONDS)
        private Duration startupTimeout;

        public int getVirtualPort() {
            return virtualPort != null ? virtualPort : port;
        }

        public Duration getStartupTimeout() {
            return startupTimeout != null ? startupTimeout : DEFAULT_START_TIMEOUT;
        }

        @Override
        public boolean supports(Class<?> clazz) {
            return clazz == HiddenServiceSocketProperties.class;
        }

        @Override
        public void validate(Object target, Errors errors) {
            HiddenServiceSocketProperties properties = (HiddenServiceSocketProperties) target;

            String directory = properties.getDirectory();
            if (directory == null || directory.isBlank()) {
                String errorMessage = "'directory' must not be empty";
                errors.rejectValue("directory", "directory.invalid", errorMessage);
            } else if (containsWhitespaces(directory)) {
                String errorMessage = "'directory' must not contain whitespaces - unsupported value";
                errors.rejectValue("directory", "directory.unsupported", errorMessage);
            }

            if (properties.getPort() <= 0) {
                String errorMessage = "'port' must be a greater than zero";
                errors.rejectValue("port", "port.invalid", errorMessage);
            }

            if (properties.getVirtualPort() <= 0) {
                String errorMessage = "'virtualPort' must be a greater than zero";
                errors.rejectValue("virtualPort", "virtualPort.invalid", errorMessage);
            }
        }
    }

    private static boolean containsWhitespaces(String value) {
        return CharMatcher.whitespace().matchesAnyOf(value);
    }
}
