package ru.yandex.practicum.blog.service;

/**
 * Domain-specific exception helps keep controller code clean and declarative.
 */
public class PostNotFoundException extends RuntimeException {
    public PostNotFoundException(long postId) {
        super("Post with id " + postId + " was not found");
    }
}
