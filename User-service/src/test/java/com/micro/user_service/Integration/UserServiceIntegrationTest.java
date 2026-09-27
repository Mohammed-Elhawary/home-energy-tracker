package com.micro.user_service.Integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.assertj.core.api.Assertions.assertThat;

import com.micro.user_service.testsupport.MySqlTestContainersBase;
import com.micro.user_service.dto.UserDTo;
import com.micro.user_service.repository.UserRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class UserServiceIntegrationTest extends MySqlTestContainersBase {

        private RestTestClient restClient;

        @Autowired
        private UserRepository repository;

        @LocalServerPort
        private int port;

        @BeforeEach
        void setUp() {
                restClient = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
        }

        @Test
        void createUser_ViaRestClient_AndReturnIt() {

                UserDTo user = UserDTo.builder().name("Mohamed").surname("Elhawary").email("elhawaryh0@gmail.com")
                                .address("14 st Alex").alerting(true).energyAlertingThreshold(2500.30).build();

                // POST - Create user
                UserDTo response = restClient.post().uri("/api/v1/users").body(user).exchange().expectStatus()
                                .isOk().expectBody(UserDTo.class).returnResult().getResponseBody();

                // Check created user
                assertThat(response).isNotNull();
                assertThat(response.getName()).isEqualTo("Mohamed");
                assertThat(response.getSurname()).isEqualTo("Elhawary");
                assertThat(response.getEmail()).isEqualTo("elhawaryh0@gmail.com");
                assertThat(response.getAddress()).isEqualTo("14 st Alex");

                // GET - Get the same user
                var result = restClient.get().uri("/api/v1/users/" + response.getId()).exchange()
                                .expectBody(UserDTo.class).returnResult();

                assertThat(result.getStatus().value()).isEqualTo(HttpStatus.OK.value());

                assertThat(result.getResponseBody()).isNotNull();               
                assertThat(result.getResponseBody().getEmail()).isEqualTo("elhawaryh0@gmail.com");
        }

}
