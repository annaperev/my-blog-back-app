package ru.yandex.practicum.blog.service.impl;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.blog.dao.PostDao;
import ru.yandex.practicum.blog.dto.PostPageResponse;
import ru.yandex.practicum.blog.dto.PostResponse;
import ru.yandex.practicum.blog.model.Post;
import ru.yandex.practicum.blog.service.PostNotFoundException;
import ru.yandex.practicum.blog.service.PostService;

import java.util.List;

@Service
public class DefaultPostService implements PostService {
    private static final int FEED_TEXT_LIMIT = 128;
    private static final String ELLIPSIS = "\u2026";

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
}
