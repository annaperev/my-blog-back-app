package ru.yandex.practicum.blog.service.impl;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.blog.dao.PostDao;
import ru.yandex.practicum.blog.dto.CommentResponse;
import ru.yandex.practicum.blog.dto.PostPageResponse;
import ru.yandex.practicum.blog.dto.PostResponse;
import ru.yandex.practicum.blog.model.Comment;
import ru.yandex.practicum.blog.model.Post;
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
        long totalPosts = postDao.countAll();
        int lastPage = totalPosts == 0
                ? 1
                : (int) ((totalPosts + pageSize - 1) / pageSize);

        // filtering with "search" parameter is postponed to next step.
        List<PostResponse> posts = postDao.findPage(pageNumber, pageSize).stream()
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
        if (postDao.findById(id).isEmpty()) {
            throw new PostNotFoundException(id);
        }

        return postDao.findImageByPostId(id)
                .orElse(DEFAULT_IMAGE_BYTES);
    }

    @Override
    public List<CommentResponse> getCommentsByPostId(long postId) {
        if (postDao.findById(postId).isEmpty()) {
            throw new PostNotFoundException(postId);
        }

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
}
