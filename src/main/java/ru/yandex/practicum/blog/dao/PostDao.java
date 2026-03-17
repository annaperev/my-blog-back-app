package ru.yandex.practicum.blog.dao;

import ru.yandex.practicum.blog.model.Comment;
import ru.yandex.practicum.blog.model.Post;

import java.util.List;
import java.util.Optional;

/**
 * DAO abstraction. Service layer depends on interface, not concrete storage.
 * Common Dependency Inversion Principle usage.
 */
public interface PostDao {
    /**
     * Creates a new post with initial counters set by backend rules.
     */
    Post create(String title, String text, List<String> tags);

    /**
     * Updates editable post fields and replaces tags with the provided list.
     */
    Optional<Post> update(long id, String title, String text, List<String> tags);

    /**
     * Deletes post aggregate by id.
     */
    boolean deleteById(long id);

    /**
     * Stores or replaces raw image bytes for the post.
     */
    void saveImage(long id, byte[] imageBytes);

    /**
     * Adds one like and returns updated likes count.
     */
    Optional<Long> incrementLikes(long id);

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

    /**
     * Returns comments for one post in stable order (oldest first by id).
     */
    List<Comment> findCommentsByPostId(long postId);
}
