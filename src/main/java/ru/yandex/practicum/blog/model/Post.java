package ru.yandex.practicum.blog.model;

import java.util.List;

/**
 * Domain model. This object represents business data inside backend layers.
 * Pattern note: keep model independent from transport concerns when possible.
 */
public record Post(
        long id,
        String title,
        String text,
        List<String> tags,
        long likesCount,
        long commentsCount
) {
}
