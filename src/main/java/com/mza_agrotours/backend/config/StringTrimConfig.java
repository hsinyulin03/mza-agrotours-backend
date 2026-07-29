package com.mza_agrotours.backend.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;


//Elimina los espacios que puede haber en las cadenas de texto antes de setear en el DTO
@Configuration
public class StringTrimConfig {

    @Bean
    public SimpleModule jacksonStringTrimModule() {
        SimpleModule module = new SimpleModule();
        // Le decimos que cada vez que vea un String, le haga .trim() antes de incluirlo al DTO
        module.addDeserializer(String.class, new StdScalarDeserializer<String>(String.class) {
            @Override
            public String deserialize(JsonParser jsonParser, DeserializationContext ctx) throws IOException {
                String value = jsonParser.getValueAsString();
                return value != null ? value.trim() : null;
            }
        });
        return module;
    }
}