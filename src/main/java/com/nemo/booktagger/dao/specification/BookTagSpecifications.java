package com.nemo.booktagger.dao.specification;

import com.nemo.booktagger.entity.BookTag;
import com.nemo.booktagger.enums.TagType;
import org.springframework.data.jpa.domain.Specification;

public final class BookTagSpecifications {

    private BookTagSpecifications() {

    }

    public static Specification<BookTag> hasUser(Integer userId) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

    public static Specification<BookTag> hasTagName(String tagName) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("tag").get("tagName"), tagName);
    }

    public static Specification<BookTag> hasTagType(TagType tagType) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("tag").get("tagType"), tagType);
    }

    public static Specification<BookTag> hasBook(Integer bookId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("book").get("id"), bookId);
    }
}
