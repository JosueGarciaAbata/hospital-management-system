package com.hospital.admin_service.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.admin_service.exception.RemoteFieldValidationException;
import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

final class RemoteErrorDecoder implements ErrorDecoder {

    private final ObjectMapper mapper;
    private final ErrorDecoder defaultDecoder = new ErrorDecoder.Default();

    RemoteErrorDecoder(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response == null || response.body() == null) {
            return defaultDecoder.decode(methodKey, response);
        }
        String payload = readBody(response.body());
        if (payload.isBlank()) {
            return FeignException.errorStatus(methodKey, response);
        }
        try {
            JsonNode root = mapper.readTree(payload);
            if (root.has("errors") && root.get("errors").isObject()) {
                Map<String, String> errors = new HashMap<>();
                root.get("errors").fields().forEachRemaining(e -> errors.put(e.getKey(), e.getValue().asText()));
                return new RemoteFieldValidationException(errors);
            }
            int status = response.status();
            String detail = textOrNull(root, "detail");
            String title = textOrNull(root, "title");
            String message = firstNonBlank(detail, title, "Error remoto");
            if (status >= 400 && status < 600) {
                return new ResponseStatusException(HttpStatus.valueOf(status), message);
            }
            return new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Respuesta inválida del servicio remoto");
        } catch (Exception ignored) {
            int status = response.status();
            if (status >= 400 && status < 600) {
                return new ResponseStatusException(HttpStatus.valueOf(status), "Error remoto");
            }
            return new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Servicio remoto inaccesible");
        }
    }

    private static String readBody(Response.Body body) {
        try (InputStream is = body.asInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    private static String textOrNull(JsonNode node, String field) {
        return node.hasNonNull(field) ? node.get(field).asText() : null;
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }
}
