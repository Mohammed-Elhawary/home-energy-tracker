package com.micro.usage_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;

@Configuration  
public class InfluxDbConfig {
    
    @Value("${influx.url}") 
    private String influxUrl;

    @Value("${influx.token}") 
    private String influxToken;

    @Value("${influx.org}") 
    private String influxOrg;

    @Bean 
    public InfluxDBClient  InfluxDBClient(){
        return InfluxDBClientFactory.create(influxUrl, influxToken.toCharArray(),influxOrg);
    }

}
