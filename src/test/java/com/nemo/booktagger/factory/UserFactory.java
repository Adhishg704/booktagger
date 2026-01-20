package com.nemo.booktagger.factory;

import com.nemo.booktagger.entity.User;

import java.util.ArrayList;
import java.util.List;

public class UserFactory {
    private static int counter = 0;

    public static User createUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        return user;
    }

    public static User createUser() {
        counter ++;
        return createUser("user" + counter, "user" + counter + "@example.com");
    }

    public static List<User> createUsers(int count) {
        List<User> users = new ArrayList<>();

        for(int i = 0; i < count; i ++) {
            users.add(createUser());
        }

        return users;
    }
}
