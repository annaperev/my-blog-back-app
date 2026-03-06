package ru.yandex.practicum.blog.service.impl;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.blog.dao.PostDao;
import ru.yandex.practicum.blog.dto.PostResponse;
import ru.yandex.practicum.blog.model.Post;
import ru.yandex.practicum.blog.service.PostNotFoundException;
import ru.yandex.practicum.blog.service.PostService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultPostServiceTest {

    // Unit test keeps service independent from real DB by using a small test double.
    private final PostDao testDao = id -> {
        if (id == 1L) {
            return Optional.of(new Post(
                    1L,
                    "Название поста 1",
                    "Текст поста в формате Markdown...",
                    List.of("tag_1", "tag_2"),
                    5L,
                    1L
            ));
        }
        return Optional.empty();
    };

    private final PostService postService = new DefaultPostService(testDao);

    @Test
    void getPostByIdShouldReturnPost() {
        PostResponse response = postService.getPostById(1L);

        assertEquals(1L, response.id());
        assertEquals("Название поста 1", response.title());
        assertEquals("Текст поста в формате Markdown...", response.text());
        assertEquals(2, response.tags().size());
        assertEquals(5L, response.likesCount());
        assertEquals(1L, response.commentsCount());
    }

    @Test
    void getPostByIdShouldThrowWhenPostMissing() {
        assertThrows(PostNotFoundException.class, () -> postService.getPostById(999L));
    }
}
