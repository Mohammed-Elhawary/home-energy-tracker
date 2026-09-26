package com.micro.api_getway.routes;

import java.net.URI;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions;
import static org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions.setPath;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class DeviceServiceRoute {

    @Bean
    public RouterFunction<ServerResponse> deviceRoutes() {
        return route("device_service").route(RequestPredicates.path("/api/v1/devices/**"), http())
                .before(uri("http://localhost:8081")).filter(CircuitBreakerFilterFunctions
                .circuitBreaker("DeviceServiceCircuitBreaker", URI.create("forward:/fallback/Device-service")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> deviceServiceApi() {
        return route("device-service-route")
                .route(RequestPredicates.path("/docs/device-service/v3/api-docs"), http())
                .before(uri("http://localhost:8081"))
                .filter(setPath("/v3/api-docs"))
                .build();
    }
    
}
