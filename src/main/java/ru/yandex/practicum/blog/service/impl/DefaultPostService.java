package ru.yandex.practicum.blog.service.impl;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.blog.dao.PostDao;
import ru.yandex.practicum.blog.dto.PostResponse;
import ru.yandex.practicum.blog.model.Post;
import ru.yandex.practicum.blog.service.PostNotFoundException;
import ru.yandex.practicum.blog.service.PostService;

@Service
public class DefaultPostService implements PostService {
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
}
