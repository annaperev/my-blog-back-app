package ru.yandex.practicum.blog.dao;

import ru.yandex.practicum.blog.model.Post;

import java.util.Optional;

/**
 * DAO abstraction. Service layer depends on interface, not concrete storage.
 * Common Dependency Inversion Principle usage.
 */
public interface PostDao {
    Optional<Post> findById(long id);
}
