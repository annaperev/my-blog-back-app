package ru.yandex.practicum.blog.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.blog.model.Post;

import java.util.List;
import java.util.Optional;

/**
 * PostgreSQL-based DAO.
 * We keep SQL here so service/controller stay DB-agnostic.
 */
@Repository
public class JdbcPostDao implements PostDao {
    private final JdbcTemplate jdbcTemplate;

    public JdbcPostDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Post> findById(long id) {
        List<Post> posts = jdbcTemplate.query(
                "SELECT id, title, text, likes_count, comments_count FROM posts WHERE id = ?",
                (rs, rowNum) -> new Post(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("text"),
                        List.of(),
                        rs.getLong("likes_count"),
                        rs.getLong("comments_count")
                ),
                id
        );

        if (posts.isEmpty()) {
            return Optional.empty();
        }

        Post postWithoutTags = posts.getFirst();
        List<String> tags = jdbcTemplate.queryForList(
                "SELECT tag FROM post_tags WHERE post_id = ? ORDER BY tag",
                String.class,
                id
        );

        return Optional.of(new Post(
                postWithoutTags.id(),
                postWithoutTags.title(),
                postWithoutTags.text(),
                tags,
                postWithoutTags.likesCount(),
                postWithoutTags.commentsCount()
        ));
    }
}
