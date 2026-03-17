package ru.yandex.practicum.blog.dto;

public record UpdateCommentRequest(
        long id,
        String text,
        long postId
) {
}
