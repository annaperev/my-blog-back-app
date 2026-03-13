package ru.yandex.practicum.blog.model;

public record Comment(
        long id,
        String text,
        long postId
) {
}
