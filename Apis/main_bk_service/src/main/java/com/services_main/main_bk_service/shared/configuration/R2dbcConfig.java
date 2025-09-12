package com.services_main.main_bk_service.shared.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.PostgresDialect;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;


@Configuration
public class R2dbcConfig {

    private final ObjectMapper objectMapper;

    public R2dbcConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper; // 👈 Spring Boot crea el ObjectMapper automáticamente
    }

    @Bean
    public R2dbcCustomConversions customConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new JsonToJsonNodeConverter(objectMapper));
        converters.add(new JsonNodeToJsonConverter(objectMapper));
        return R2dbcCustomConversions.of(PostgresDialect.INSTANCE, converters);
    }

    @ReadingConverter
    static class JsonToJsonNodeConverter implements Converter<Json, JsonNode> {
        private final ObjectMapper objectMapper;
        public JsonToJsonNodeConverter(ObjectMapper objectMapper) { this.objectMapper = objectMapper; }

        @Override
        public JsonNode convert(Json json) {
            try {
                return objectMapper.readTree(json.asString());
            } catch (IOException e) {
                throw new RuntimeException("Error parsing JSON from Postgres", e);
            }
        }
    }

    @WritingConverter
    static class JsonNodeToJsonConverter implements Converter<JsonNode, Json> {
        private final ObjectMapper objectMapper;
        public JsonNodeToJsonConverter(ObjectMapper objectMapper) { this.objectMapper = objectMapper; }

        @Override
        public Json convert(JsonNode source) {
            try {
                return Json.of(objectMapper.writeValueAsString(source));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error serializing JsonNode to Postgres JSON", e);
            }
        }
    }

}
