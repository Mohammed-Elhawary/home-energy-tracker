package com.micro.api_getway.routes;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class InsightServiceRoute {

    @Bean
    public RouterFunction<ServerResponse> userServiceRoutes() {
        return route("insight-service").route(RequestPredicates.path("/api/v1/insight/**"), http())
                .before(uri("http://localhost:8085")).build();
    }

}
