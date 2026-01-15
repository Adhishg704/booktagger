package com.nemo.booktagger.entity;

import com.nemo.booktagger.enums.ReadingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_books")
@Getter
@Setter
@NoArgsConstructor
public class UserBook {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "year_read")
    private Integer yearRead;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ReadingStatus status;

    @Column(name = "rating", precision = 3, scale = 2)
    private Double rating;

    public UserBook(User user, Book book, Integer yearRead, ReadingStatus status, Double rating) {
        this.user = user;
        this.book = book;
        this.yearRead = yearRead;
        this.status = status;
        this.rating = rating;
    }
}
