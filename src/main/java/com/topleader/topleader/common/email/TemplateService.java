package com.topleader.topleader.common.email;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TemplateService implements Templating {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    public String getMessage(Map<String, Object> parameters, String templatePath) {
        var template = loadTemplate(templatePath);
        return replaceVariables(template, parameters);
    }

    private String loadTemplate(String templatePath) {
        try (InputStream is = new ClassPathResource(templatePath).getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("Template not found: " + templatePath, e);
        }
    }

    private String replaceVariables(String template, Map<String, Object> parameters) {
        var matcher = VARIABLE_PATTERN.matcher(template);
        var result = new StringBuilder();

        while (matcher.find()) {
            var variableName = matcher.group(1);
            var value = parameters.get(variableName);
            matcher.appendReplacement(result, value != null ? Matcher.quoteReplacement(value.toString()) : StringUtils.EMPTY);
        }
        matcher.appendTail(result);

        return result.toString();
    }
}