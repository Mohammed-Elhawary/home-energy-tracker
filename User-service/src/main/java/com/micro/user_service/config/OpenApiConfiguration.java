package com.micro.user_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI userServiceApiDocs() {
        return new OpenAPI().info(new io.swagger.v3.oas.models.info.Info()
        .title("user service api docs")
                .description("UserService Api for Home Energy Tracker")
                .contact(getContact()       )
                .version("0.0.0"));
    }
    
    private static Contact getContact() {
        
        Contact contact = new Contact();
        contact.setEmail("hawary.xom@gmail.com");
        return contact;
    }
}
