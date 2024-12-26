package com.topleader.topleader.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import org.mockito.Mockito;


import org.springframework.ai.chat.model.ChatModel;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestBeanConfiguration {



    @Bean
    public GreenMail greenMail(@Value("${spring.mail.port}") final Integer emailPort) {
        return new GreenMail(new ServerSetup(emailPort, null, ServerSetup.PROTOCOL_SMTP))
                .withConfiguration(GreenMailConfiguration.aConfig().withDisabledAuthentication());
    }

    @Bean
    public ChatModel chatClient() {
        return Mockito.mock(ChatModel.class);
    }

    @Bean
    public WireMockServer mockServer() {
        return new WireMockServer(8060);
    }

}
