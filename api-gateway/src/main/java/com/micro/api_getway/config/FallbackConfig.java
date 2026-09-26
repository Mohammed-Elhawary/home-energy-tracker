package com.micro.api_getway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.Map;

import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;

@Configuration
public class FallbackConfig {

    @Bean
    public RouterFunction<ServerResponse> dynamicFallbackRoute() {
        return route("dynamicFallback")
                .route(RequestPredicates.path("/fallback/{serviceName}"), request -> {
                    String serviceName = request.pathVariable("serviceName");
                    
                    Map<String, Object> responseBody = Map.of(
                            "message", serviceName.toUpperCase() + " is currently down. Please try again later.",
                            "status", HttpStatus.SERVICE_UNAVAILABLE.value()
                    );

                    return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseBody);
                })
                .build();
    }
}