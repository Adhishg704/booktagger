package com.nemo.booktagger.config;

import io.netty.channel.ChannelOption;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class OpenLibraryConfig {

    @Bean
    public WebClient openLibraryWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .responseTimeout(Duration.ofSeconds(15));

        return WebClient.builder()
                .baseUrl("https://openlibrary.org")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
