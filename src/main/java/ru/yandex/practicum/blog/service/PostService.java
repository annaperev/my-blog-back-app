package ru.yandex.practicum.blog.service;

import ru.yandex.practicum.blog.dto.PostPageResponse;
import ru.yandex.practicum.blog.dto.PostResponse;

public interface PostService {
    PostResponse getPostById(long id);

    PostPageResponse getPosts(String search, int pageNumber, int pageSize);
}
