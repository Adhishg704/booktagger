package com.nemo.booktagger.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "books",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"title", "author"}
        ))
@Setter
@Getter
@NoArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Setter(AccessLevel.NONE)
    private Integer id;

    @Column(name = "title")
    private String title;

    @Column(name = "author")
    private String author;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "isbn", unique = true)
    private String isbn;

    @Column(name = "year_published")
    private String yearPublished;

    @Column(name = "thumbnail")
    private String thumbnailURL;

    public Book(String title, String author, String description, String isbn, String yearPublished, String thumbnailURL) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.isbn = isbn;
        this.yearPublished = yearPublished;
        this.thumbnailURL = thumbnailURL;
    }
}
