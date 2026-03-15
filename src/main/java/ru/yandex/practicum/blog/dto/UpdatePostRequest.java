package ru.yandex.practicum.blog.dto;

import java.util.List;

public record UpdatePostRequest(
        long id,
        String title,
        String text,
        List<String> tags
) {
}
