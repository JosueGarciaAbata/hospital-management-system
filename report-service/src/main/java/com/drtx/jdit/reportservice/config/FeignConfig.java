package com.drtx.jdit.reportservice.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.drtx.jdit.reportservice.external.feign")
public class FeignConfig {
}
