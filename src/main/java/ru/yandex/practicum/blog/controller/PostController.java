package ru.yandex.practicum.blog.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.blog.dto.PostResponse;
import ru.yandex.practicum.blog.service.PostService;

/**
 * REST controller exposing blog post endpoints.
 */
@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * POST /api/posts/{id}
     */
    @PostMapping("/{id}")
    public PostResponse getPost(@PathVariable("id") long id) {
        return postService.getPostById(id);
    }
}
