package com.hospital.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FeighAuthInterceptor implements RequestInterceptor {

    @Autowired
    private HttpServletRequest request;

    @Override
    public void apply(RequestTemplate template) {
        String roles = request.getHeader("X-Roles");
        String userId = request.getHeader("X-User-Id");
        String centerId = request.getHeader("X-Center-Id");

        if (roles != null) template.header("X-Roles", roles);
        if (userId != null) template.header("X-User-Id", userId);
        if (centerId != null) template.header("X-Center-Id", centerId);
    }
}
