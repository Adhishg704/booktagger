package com.nemo.booktagger.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ai_embedded_books")
@Getter
@Setter
@NoArgsConstructor
public class AiEmbeddedBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Setter(AccessLevel.NONE)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "title", length = 500)
    private String title;

    @Column(name = "author", length = 300)
    private String author;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "genres", columnDefinition = "TEXT")
    private String genres;

    @Column(name = "themes", columnDefinition = "TEXT")
    private String themes;

    @Column(name = "tones", columnDefinition = "TEXT")
    private String tones;

    @Column(name = "keywords", columnDefinition = "TEXT")
    private String keywords;

    public AiEmbeddedBook(Book book, String title, String author, String summary, String genres, String themes,
            String tones, String keywords
    ) {
        this.book = book;
        this.title = title;
        this.author = author;
        this.summary = summary;
        this.genres = genres;
        this.themes = themes;
        this.tones = tones;
        this.keywords = keywords;
    }
}
