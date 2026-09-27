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
public class UserServiceRoute {

    @Bean
    public RouterFunction<ServerResponse> userRoutes() {
        return route("User-service")
                .route(RequestPredicates.path("/api/v1/users/**"), http())
                .before(uri("http://localhost:8080"))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("userServiceCircuitBreaker",
                        URI.create("forward:/fallback/user-service")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> userServiceApi() {
        return route("user-service-route")
                .route(RequestPredicates.path("/docs/user-service/v3/api-docs"), http())
                .before(uri("http://localhost:8080"))
                .filter(setPath("/v3/api-docs")) 
                .build();
    }

}
