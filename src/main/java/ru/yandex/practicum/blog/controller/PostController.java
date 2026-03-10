package ru.yandex.practicum.blog.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.blog.dto.PostPageResponse;
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
     * GET /api/posts?search=...&pageNumber=...&pageSize=...
     */
    @GetMapping
    public PostPageResponse getPosts(@RequestParam("search") String search,
                                     @RequestParam("pageNumber") int pageNumber,
                                     @RequestParam("pageSize") int pageSize) {
        if (pageNumber < 1 || pageSize < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "pageNumber and pageSize must be positive");
        }

        return postService.getPosts(search, pageNumber, pageSize);
    }

    /**
     * POST /api/posts/{id}
     */
    @PostMapping("/{id}")
    public PostResponse getPost(@PathVariable("id") long id) {
        return postService.getPostById(id);
    }

    /**
     * GET /api/posts/{id}/image
     */
    @GetMapping(value = "/{id}/image", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getPostImage(@PathVariable("id") long id) {
        return ResponseEntity.ok(postService.getPostImage(id));
    }
}
