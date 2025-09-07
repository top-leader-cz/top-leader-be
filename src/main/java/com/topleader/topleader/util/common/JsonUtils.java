package com.topleader.topleader.util.common;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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


    public JsonNode toJsonNode(String json) {
        return Try.of(() -> OB.readTree(json))
                .getOrElseThrow(e -> new  JsonConversionException("Error while parsing json!", e));
    }

    public ObjectNode toJObjectNode(String json) {
        return (ObjectNode) toJsonNode(json);
    }

    public ObjectNode toJArrayNode(String json) {
        return (ObjectNode) toJsonNode(json);
    }
}
