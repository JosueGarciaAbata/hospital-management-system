package com.hospital.admin_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing // habilita @CreatedDate / @LastModifiedDate
public class JpaAuditingConfig {}
