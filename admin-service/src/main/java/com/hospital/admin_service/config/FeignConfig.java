package com.hospital.admin_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableFeignClients(basePackages = "com.hospital.admin_service.external")
public class FeignConfig {

    @Bean
    public feign.codec.ErrorDecoder errorDecoder(ObjectMapper mapper) {
        return new RemoteErrorDecoder(mapper);
    }
}