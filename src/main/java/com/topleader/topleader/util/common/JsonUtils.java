package com.topleader.topleader.util.common;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vavr.control.Try;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JsonUtils {

    private ObjectMapper OB = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .registerModule(new Jdk8Module());

    public String toJson(Object data) {
       return Try.of(() -> OB.writeValueAsString(data))
                .getOrElseThrow(e -> new  JsonConversionException("Error while parsing json!", e));
    }
}
