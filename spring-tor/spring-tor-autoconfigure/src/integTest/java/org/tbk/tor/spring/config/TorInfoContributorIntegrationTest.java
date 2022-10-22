package org.tbk.tor.spring.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.LOG_DEBUG, printOnlyOnFailure = false)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "server.port=13337",
        "management.server.port=13337",
        "management.endpoints.web.exposure.include=info"
})
class TorInfoContributorIntegrationTest {

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
    void itShouldAddTorInformationToInfoEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(jsonPath("tor").exists())
                .andExpect(jsonPath("tor.proxy_available").exists())
                .andExpect(jsonPath("tor.proxy_port").exists())
                .andExpect(jsonPath("tor.proxy_address").exists())
                .andExpect(status().isOk());
    }

    @Test
    void itShouldAddHiddenServiceInformationToInfoEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(jsonPath("hiddenService").exists())
                .andExpect(jsonPath("hiddenService.spring_boot_app").exists())
                .andExpect(jsonPath("hiddenService.spring_boot_app.name").value("spring_boot_app"))
                .andExpect(jsonPath("hiddenService.spring_boot_app.virtual_host").exists())
                .andExpect(jsonPath("hiddenService.spring_boot_app.virtual_port").value(80))
                .andExpect(jsonPath("hiddenService.spring_boot_app.host").exists())
                .andExpect(jsonPath("hiddenService.spring_boot_app.port").exists())
                .andExpect(status().isOk());
    }
}