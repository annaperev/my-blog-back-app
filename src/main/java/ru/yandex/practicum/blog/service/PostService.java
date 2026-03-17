package ru.yandex.practicum.blog.service;

import ru.yandex.practicum.blog.dto.CommentResponse;
import ru.yandex.practicum.blog.dto.CreateCommentRequest;
import ru.yandex.practicum.blog.dto.CreatePostRequest;
import ru.yandex.practicum.blog.dto.PostPageResponse;
import ru.yandex.practicum.blog.dto.PostResponse;
import ru.yandex.practicum.blog.dto.UpdateCommentRequest;
import ru.yandex.practicum.blog.dto.UpdatePostRequest;

import java.util.List;

public interface PostService {
    PostResponse createPost(CreatePostRequest request);

    PostResponse updatePost(UpdatePostRequest request);

    void deletePost(long id);

    void updatePostImage(long id, byte[] imageBytes);

    long incrementLikes(long id);

    CommentResponse getCommentById(long postId, long commentId);

    CommentResponse createComment(CreateCommentRequest request);

    CommentResponse updateComment(UpdateCommentRequest request);

    void deleteComment(long postId, long commentId);

    PostResponse getPostById(long id);

    PostPageResponse getPosts(String search, int pageNumber, int pageSize);

    byte[] getPostImage(long id);

    List<CommentResponse> getCommentsByPostId(long postId);
}
