package com.infybuzz.external.decoder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infybuzz.exceptions.ErrorDetailsResponse;
import com.infybuzz.exceptions.OrderServiceException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
public class CustomErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Exception decode(String methodKey, Response response) {
        log.info("::{}", response.request().url());
        log.info("::{}", response.request().headers());

        HttpStatus status = HttpStatus.resolve(response.status());
        if (status == null || !status.isError()) {
            return defaultDecoder.decode(methodKey, response);
        }

        try (InputStream body = response.body().asInputStream()) {
            String responseBody = new String(body.readAllBytes(), StandardCharsets.UTF_8);
            log.info("Error response: {}", responseBody);

            try {
                return new OrderServiceException(status,
                        objectMapper.readValue(responseBody, ErrorDetailsResponse.class).details());
            } catch (IOException e) {
                log.error("Non-JSON error response");
                return new OrderServiceException(status, responseBody);
            }
        } catch (IOException | NullPointerException e) {
            return new OrderServiceException(status, e.getMessage());
        }
    }
}