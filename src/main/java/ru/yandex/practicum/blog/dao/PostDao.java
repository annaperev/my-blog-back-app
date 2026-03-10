package ru.yandex.practicum.blog.dao;

import ru.yandex.practicum.blog.model.Post;

import java.util.List;
import java.util.Optional;

/**
 * DAO abstraction. Service layer depends on interface, not concrete storage.
 * Common Dependency Inversion Principle usage.
 */
public interface PostDao {
    Optional<Post> findById(long id);

    /**
     * Returns one page of posts in feed order.
     * For now, search filtering is intentionally not applied yet.
     */
    List<Post> findPage(int pageNumber, int pageSize);

    /**
     * Total amount of posts, used to calculate paging metadata.
     */
    long countAll();

    /**
     * Reads raw image bytes linked to a post id.
     */
    Optional<byte[]> findImageByPostId(long id);
}
