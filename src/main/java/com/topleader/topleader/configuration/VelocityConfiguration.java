package com.topleader.topleader.configuration;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class VelocityConfiguration {

    @Bean
    public VelocityEngine createVelocityEngine() {
        final VelocityEngine v = new VelocityEngine();
        v.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        v.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        v.init();
        return v;
    }
}
