package ru.yandex.practicum.blog.dto;

public record CommentResponse(
        long id,
        String text,
        long postId
) {
}
