package org.tbk.tor.spring.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "server.port=13337",
        "management.server.port=13337",
        "management.endpoint.health.show-details=always",
        "management.health.hiddenService.enabled=false"
})
class DisabledHiddenServiceHealthIndicatorIntegrationTest {

    @SpringBootApplication
    public static class HiddenServiceTestApplication {

        public static void main(String[] args) {
            new SpringApplicationBuilder()
                    .sources(HiddenServiceTestApplication.class)
                    .web(WebApplicationType.SERVLET)
                    .run(args);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void itShouldCheckHiddenServiceHealthEndpointDoesNotExist() throws Exception {
        mockMvc.perform(get("/actuator/health/hiddenService"))
                .andExpect(status().isNotFound());
    }

    @Test
    void itShouldNotAddHiddenServiceInformationToHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(jsonPath("status").value(Status.UP.getCode()))
                .andExpect(jsonPath("components.hiddenService").doesNotExist())
                .andExpect(status().isOk());
    }
}