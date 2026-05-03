package com.nemo.booktagger.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OpenLibraryConfig {

    @Bean
    public WebClient openLibraryWebClient() {
        return WebClient
                .builder()
                .baseUrl("https://openlibrary.org")
                .build();
    }
}
