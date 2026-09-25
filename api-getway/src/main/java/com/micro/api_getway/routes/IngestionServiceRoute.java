package com.micro.api_getway.routes;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

import java.net.URI;

import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class IngestionServiceRoute {

    @Bean
    public RouterFunction<ServerResponse> ingestionRoutes() {
        return route("ingestion_service").route(RequestPredicates.path("/api/v1/ingestion/**"), http())
                .before(uri("http://localhost:8082")).filter(CircuitBreakerFilterFunctions
                        .circuitBreaker("IngistionServiceCircutBreaker", URI.create("forward:/fallback/Ingistion-service")))
  .build();
    }

}
