package ru.yandex.practicum.blog.dto;

public record CreateCommentRequest(
        String text,
        long postId
) {
}
