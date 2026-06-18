package consulting_service.configs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import consulting_service.exceptions.NotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class RemoteErrorDecoder implements ErrorDecoder {

    private final ObjectMapper mapper;
    private final ErrorDecoder defaultDecoder = new ErrorDecoder.Default();

    public RemoteErrorDecoder(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response == null || response.body() == null) {
            return defaultDecoder.decode(methodKey, response);
        }

        String payload = readBody(response.body());
        if (payload.isBlank()) {
            return defaultDecoder.decode(methodKey, response);
        }

        try {
            JsonNode root = mapper.readTree(payload);
            int status = response.status();
            String message = root.has("detail") ? root.get("detail").asText()
                    : root.has("message") ? root.get("message").asText()
                    : "Error remoto";

            if (status == 404) return new NotFoundException(message);
            if (status >= 400 && status < 600) return new ResponseStatusException(HttpStatus.valueOf(status), message);

            return new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Respuesta inválida del servicio remoto");

        } catch (Exception e) {
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
}
