package com.nemo.booktagger.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class GoogleBooksClient {
    private final WebClient webClient;

    public GoogleBooksClient(WebClient googleBooksWebClient) {
        this.webClient = googleBooksWebClient;
    }

    public GoogleBooksResponse searchByIsbn(String isbn) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.
                        path("/volumes").
                        queryParam("q", "isbn:" + isbn).
                        queryParam("maxResults", 1).build())
                .retrieve()
                .bodyToMono(GoogleBooksResponse.class)
                .block();
    }

    public GoogleBooksResponse searchByTitleAndAuthor(String title, String author) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.
                        path("/volumes").
                        queryParam("q", "intitle:" + title + "+inauthor:" + author).
                        queryParam("maxResults", 1).build())
                .retrieve()
                .bodyToMono(GoogleBooksResponse.class)
                .block();
    }
}
