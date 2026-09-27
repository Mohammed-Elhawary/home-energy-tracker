package com.micro.user_service.testsupport;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
public class MySqlTestContainersBase {

    @Container
    @ServiceConnection
    static MariaDBContainer myMaria = new MariaDBContainer("mariadb:10.3.39").withDatabaseName("home-energy-tracker")
            .withUsername("root").withPassword("your_password");

}
