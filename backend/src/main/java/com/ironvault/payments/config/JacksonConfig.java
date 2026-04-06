package com.ironvault.payments.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                    .withZone(ZoneOffset.UTC);

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer instantFormatCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule();

            // Serializer — formato limpo na saída
            module.addSerializer(Instant.class, new StdSerializer<>(Instant.class) {
                @Override
                public void serialize(Instant value, JsonGenerator gen, SerializerProvider provider)
                        throws IOException {
                    gen.writeString(FORMATTER.format(value));
                }
            });

            // Deserializer — aceita múltiplos formatos na entrada
            module.addDeserializer(Instant.class, new StdDeserializer<>(Instant.class) {
                @Override
                public Instant deserialize(JsonParser p, DeserializationContext ctx)
                        throws IOException {
                    String text = p.getText();
                    try {
                        return Instant.parse(text);
                    } catch (Exception e) {
                        return OffsetDateTime.parse(text).toInstant();
                    }
                }
            });

            builder.modules(module);
        };
    }
}
