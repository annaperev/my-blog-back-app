package ru.yandex.practicum.blog.dto;

import java.util.List;

/**
 * Transport DTO for post feed pages.
 * Pattern note: keep paging metadata near list payload so frontend can render controls in one response.
 */
public record PostPageResponse(
        List<PostResponse> posts,
        boolean hasPrev,
        boolean hasNext,
        int lastPage
) {
}
