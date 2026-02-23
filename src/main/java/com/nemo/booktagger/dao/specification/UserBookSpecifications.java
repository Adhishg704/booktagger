package com.nemo.booktagger.dao.specification;

import com.nemo.booktagger.entity.UserBook;
import com.nemo.booktagger.enums.ReadingStatus;
import org.springframework.data.jpa.domain.Specification;

public final class UserBookSpecifications {

    private UserBookSpecifications() {

    }

    public static Specification<UserBook> hasUser(Integer userId) {
        return (root, query, cb) ->
                cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<UserBook> hasYearPublished(String yearPublished) {
        return (root, query, cb) ->
                cb.equal(root.get("book").get("yearPublished"), yearPublished);
    }

    public static Specification<UserBook> hasYearRead(String yearRead) {
        return (root, query, cb) ->
                cb.equal(root.get("yearRead"), yearRead);
    }

    public static Specification<UserBook> hasStatus(ReadingStatus status) {
        return (root, query, cb) ->
                cb.equal(root.get("status"), status);
    }
}
