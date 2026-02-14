package com.nemo.booktagger.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GoogleBooksConfig {

    @Bean
    public WebClient googleBooksWebClient() {
        return WebClient
                .builder()
                .baseUrl("https://www.googleapis.com/books/v1")
                .build();
    }
}
