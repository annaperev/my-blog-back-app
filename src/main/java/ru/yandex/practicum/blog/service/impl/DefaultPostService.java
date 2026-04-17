package ru.yandex.practicum.blog.service.impl;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.blog.dao.PostDao;
import ru.yandex.practicum.blog.dto.CommentResponse;
import ru.yandex.practicum.blog.dto.CreateCommentRequest;
import ru.yandex.practicum.blog.dto.CreatePostRequest;
import ru.yandex.practicum.blog.dto.PostPageResponse;
import ru.yandex.practicum.blog.dto.PostResponse;
import ru.yandex.practicum.blog.dto.UpdateCommentRequest;
import ru.yandex.practicum.blog.dto.UpdatePostRequest;
import ru.yandex.practicum.blog.model.Comment;
import ru.yandex.practicum.blog.model.Post;
import ru.yandex.practicum.blog.service.CommentNotFoundException;
import ru.yandex.practicum.blog.service.PostNotFoundException;
import ru.yandex.practicum.blog.service.PostService;

import java.util.Base64;
import java.util.List;

@Service
public class DefaultPostService implements PostService {
    private static final int FEED_TEXT_LIMIT = 128;
    private static final String ELLIPSIS = "\u2026";
    private static final byte[] DEFAULT_IMAGE_BYTES = Base64.getDecoder().decode(
            // 1x1 transparent PNG fallback while image upload endpoint is not implemented yet.
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO3Z4t0AAAAASUVORK5CYII="
    );

    private final PostDao postDao;

    public DefaultPostService(PostDao postDao) {
        this.postDao = postDao;
    }

    @Override
    public PostResponse createPost(CreatePostRequest request) {
        Post createdPost = postDao.create(request.title(), request.text(), request.tags());
        return toResponse(createdPost);
    }

    @Override
    public PostResponse updatePost(UpdatePostRequest request) {
        Post updatedPost = postDao.update(request.id(), request.title(), request.text(), request.tags())
                .orElseThrow(() -> new PostNotFoundException(request.id()));
        return toResponse(updatedPost);
    }

    @Override
    public void deletePost(long id) {
        boolean deleted = postDao.deleteById(id);
        if (!deleted) {
            throw new PostNotFoundException(id);
        }
    }

    @Override
    public void updatePostImage(long id, byte[] imageBytes) {
        if (postDao.findById(id).isEmpty()) {
            throw new PostNotFoundException(id);
        }
        postDao.saveImage(id, imageBytes);
    }

    @Override
    public long incrementLikes(long id) {
        return postDao.incrementLikes(id)
                .orElseThrow(() -> new PostNotFoundException(id));
    }

    @Override
    public CommentResponse getCommentById(long postId, long commentId) {
        ensurePostExists(postId);
        Comment comment = postDao.findCommentById(postId, commentId)
                .orElseThrow(() -> new CommentNotFoundException(postId, commentId));
        return toCommentResponse(comment);
    }

    @Override
    public CommentResponse createComment(CreateCommentRequest request) {
        ensurePostExists(request.postId());
        Comment createdComment = postDao.createComment(request.postId(), request.text());
        return toCommentResponse(createdComment);
    }

    @Override
    public CommentResponse updateComment(UpdateCommentRequest request) {
        ensurePostExists(request.postId());
        Comment updatedComment = postDao.updateComment(request.postId(), request.id(), request.text())
                .orElseThrow(() -> new CommentNotFoundException(request.postId(), request.id()));
        return toCommentResponse(updatedComment);
    }

    @Override
    public void deleteComment(long postId, long commentId) {
        ensurePostExists(postId);
        boolean deleted = postDao.deleteComment(postId, commentId);
        if (!deleted) {
            throw new CommentNotFoundException(postId, commentId);
        }
    }

    @Override
    public PostResponse getPostById(long id) {
        Post post = postDao.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
        return toResponse(post);
    }

    private PostResponse toResponse(Post post) {
        return new PostResponse(
                post.id(),
                post.title(),
                post.text(),
                post.tags(),
                post.likesCount(),
                post.commentsCount()
        );
    }

    @Override
    public PostPageResponse getPosts(String search, int pageNumber, int pageSize) {
        long totalPosts = postDao.countAll(search);
        int lastPage = totalPosts == 0
                ? 1
                : (int) ((totalPosts + pageSize - 1) / pageSize);

        List<PostResponse> posts = postDao.findPage(search, pageNumber, pageSize).stream()
                .map(this::toFeedResponse)
                .toList();

        return new PostPageResponse(
                posts,
                pageNumber > 1,
                pageNumber < lastPage,
                lastPage
        );
    }

    @Override
    public byte[] getPostImage(long id) {
        ensurePostExists(id);

        return postDao.findImageByPostId(id)
                .orElse(DEFAULT_IMAGE_BYTES);
    }

    @Override
    public List<CommentResponse> getCommentsByPostId(long postId) {
        ensurePostExists(postId);

        return postDao.findCommentsByPostId(postId).stream()
                .map(this::toCommentResponse)
                .toList();
    }

    private PostResponse toFeedResponse(Post post) {
        return new PostResponse(
                post.id(),
                post.title(),
                shortenForFeed(post.text()),
                post.tags(),
                post.likesCount(),
                post.commentsCount()
        );
    }

    private String shortenForFeed(String text) {
        if (text.length() <= FEED_TEXT_LIMIT) {
            return text;
        }
        return text.substring(0, FEED_TEXT_LIMIT) + ELLIPSIS;
    }

    private CommentResponse toCommentResponse(Comment comment) {
        return new CommentResponse(
                comment.id(),
                comment.text(),
                comment.postId()
        );
    }

    private void ensurePostExists(long postId) {
        if (postDao.findById(postId).isEmpty()) {
            throw new PostNotFoundException(postId);
        }
    }
}
