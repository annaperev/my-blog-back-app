package ru.yandex.practicum.blog.service;

import ru.yandex.practicum.blog.dto.CommentResponse;
import ru.yandex.practicum.blog.dto.CreatePostRequest;
import ru.yandex.practicum.blog.dto.PostPageResponse;
import ru.yandex.practicum.blog.dto.PostResponse;
import ru.yandex.practicum.blog.dto.UpdatePostRequest;

import java.util.List;

public interface PostService {
    PostResponse createPost(CreatePostRequest request);

    PostResponse updatePost(UpdatePostRequest request);

    PostResponse getPostById(long id);

    PostPageResponse getPosts(String search, int pageNumber, int pageSize);

    byte[] getPostImage(long id);

    List<CommentResponse> getCommentsByPostId(long postId);
}
