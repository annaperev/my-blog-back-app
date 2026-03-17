package ru.yandex.practicum.blog.service;

/**
 * Separate exception keeps nested-resource 404s explicit in controller advice.
 */
public class CommentNotFoundException extends RuntimeException {
    public CommentNotFoundException(long postId, long commentId) {
        super("Comment with id " + commentId + " for post with id " + postId + " was not found");
    }
}
