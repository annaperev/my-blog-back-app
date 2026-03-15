package ru.yandex.practicum.blog.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.blog.dto.CommentResponse;
import ru.yandex.practicum.blog.dto.CreatePostRequest;
import ru.yandex.practicum.blog.dto.PostPageResponse;
import ru.yandex.practicum.blog.dto.PostResponse;
import ru.yandex.practicum.blog.dto.UpdatePostRequest;
import ru.yandex.practicum.blog.service.PostService;

import java.util.List;

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
     * POST /api/posts
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public PostResponse createPost(@RequestBody CreatePostRequest request) {
        validatePostPayload(request.title(), request.text(), request.tags());
        return postService.createPost(request);
    }

    /**
     * PUT /api/posts/{id}
     */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public PostResponse updatePost(@PathVariable("id") long id, @RequestBody UpdatePostRequest request) {
        validateUpdatePostRequest(id, request);
        return postService.updatePost(request);
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
     * GET /api/posts/{id}
     */
    @GetMapping("/{id}")
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

    /**
     * GET /api/posts/{id}/comments
     */
    @GetMapping("/{id}/comments")
    public List<CommentResponse> getPostComments(@PathVariable("id") long id) {
        return postService.getCommentsByPostId(id);
    }

    private void validateUpdatePostRequest(long pathId, UpdatePostRequest request) {
        if (request == null || request.id() != pathId) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Path id must match request body id"
            );
        }
        validatePostPayload(request.title(), request.text(), request.tags());
    }

    private void validatePostPayload(String title, String text, List<String> tags) {
        if (isBlank(title) || isBlank(text) || tags == null || tags.stream().anyMatch(this::isBlank)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "title, text and tags are required; tags cannot contain blank values"
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
