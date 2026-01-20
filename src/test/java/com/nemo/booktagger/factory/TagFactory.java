package com.nemo.booktagger.factory;

import com.nemo.booktagger.entity.Tag;
import com.nemo.booktagger.entity.User;
import com.nemo.booktagger.enums.TagType;

import java.util.Arrays;
import java.util.List;

public final class TagFactory {

    private TagFactory() {

    }

    public static Tag createTag(User user, TagType tagType, String tagName) {
        return new Tag(
                user,
                tagType,
                tagName
        );
    }

    public static List<Tag> createTagList(
            User user,
            TagType tagType,
            String... tagNames
    ) {
        return Arrays.stream(tagNames)
                .map(name -> createTag(user, tagType, name))
                .toList();
    }

}
