package com.micro.home_energy_tracker;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.micro.user_service.HomeEnergyTrackerApplication;
import com.micro.user_service.entity.User;
import com.micro.user_service.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(
	classes = HomeEnergyTrackerApplication.class
)
class HomeEnergyTrackerApplicationTests {

    @Autowired
    private UserRepository userRepository;

    private final int NumberOfUsers = 10;

    @Test
    void contextLoads() {
    }

    @Disabled
    @Test
    void addUserToDatabase() {
        for (int i = 1; i <= NumberOfUsers; i++) {
            User user = User.builder()
                    .name("User " + i)
                    .surname("surname " + i)
                    .email("user" + i + "@example.com")
                    .address(i % 2 == 0 ? "123 Main St" : "456 Oak Ave")
                    .alerting(i % 2 == 0)
                    .energyAlertingThreshold(i % 2 == 0 ? 100.0 : 200.0
                    )
                    .build();
            userRepository.save(user);
            log.info("User added to database successfully: ");
        }
    }

}
