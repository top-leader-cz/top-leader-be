package com.topleader.topleader.common.email;

import lombok.RequiredArgsConstructor;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VelocityService {

    private final VelocityEngine velocityEngine;

    public String getMessage(final Map<String, Object> parameters, final String templatePath) {
        VelocityContext context = new VelocityContext(parameters);

        StringWriter response = new StringWriter();
        velocityEngine.getTemplate(templatePath).merge(context, response);
        return response.toString();

    }

}