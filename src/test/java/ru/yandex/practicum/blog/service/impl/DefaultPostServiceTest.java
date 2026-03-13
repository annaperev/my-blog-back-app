package ru.yandex.practicum.blog.service.impl;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.blog.dao.PostDao;
import ru.yandex.practicum.blog.dto.CommentResponse;
import ru.yandex.practicum.blog.dto.PostPageResponse;
import ru.yandex.practicum.blog.dto.PostResponse;
import ru.yandex.practicum.blog.model.Comment;
import ru.yandex.practicum.blog.model.Post;
import ru.yandex.practicum.blog.service.PostNotFoundException;
import ru.yandex.practicum.blog.service.PostService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultPostServiceTest {

    // Unit test keeps service independent of real DB by using a test double.
    private static final List<Post> POSTS = List.of(
            new Post(
                    1L,
                    "First post",
                    "a".repeat(130),
                    List.of("tag_1", "tag_2"),
                    5L,
                    1L
            ),
            new Post(
                    2L,
                    "Second post",
                    "Short text",
                    List.of(),
                    1L,
                    5L
            )
    );
    private static final List<Comment> COMMENTS = List.of(
            new Comment(1L, "First comment", 1L),
            new Comment(2L, "Second comment", 1L),
            new Comment(3L, "Comment for post 2", 2L)
    );

    private final PostDao testDao = new PostDao() {
        @Override
        public Optional<Post> findById(long id) {
            return POSTS.stream()
                    .filter(post -> post.id() == id)
                    .findFirst();
        }

        @Override
        public List<Post> findPage(int pageNumber, int pageSize) {
            int fromIndex = Math.min((pageNumber - 1) * pageSize, POSTS.size());
            int toIndex = Math.min(fromIndex + pageSize, POSTS.size());
            return POSTS.subList(fromIndex, toIndex);
        }

        @Override
        public long countAll() {
            return POSTS.size();
        }

        @Override
        public Optional<byte[]> findImageByPostId(long id) {
            if (id == 1L) {
                return Optional.of(new byte[]{1, 2, 3});
            }
            return Optional.empty();
        }

        @Override
        public List<Comment> findCommentsByPostId(long postId) {
            return COMMENTS.stream()
                    .filter(comment -> comment.postId() == postId)
                    .toList();
        }
    };

    private final PostService postService = new DefaultPostService(testDao);

    @Test
    void getPostByIdShouldReturnPost() {
        PostResponse response = postService.getPostById(1L);

        assertEquals(1L, response.id());
        assertEquals("First post", response.title());
        assertEquals("a".repeat(130), response.text());
        assertEquals(2, response.tags().size());
        assertEquals(5L, response.likesCount());
        assertEquals(1L, response.commentsCount());
    }

    @Test
    void getPostByIdShouldThrowWhenPostMissing() {
        assertThrows(PostNotFoundException.class, () -> postService.getPostById(999L));
    }

    @Test
    void getPostsShouldReturnPageAndApplyFeedTruncation() {
        PostPageResponse response = postService.getPosts("ignored now", 1, 1);

        assertEquals(1, response.posts().size());
        assertEquals("a".repeat(128) + "\u2026", response.posts().getFirst().text());
        assertFalse(response.hasPrev());
        assertTrue(response.hasNext());
        assertEquals(2, response.lastPage());
    }

    @Test
    void getPostImageShouldReturnStoredBytes() {
        byte[] image = postService.getPostImage(1L);
        assertEquals(3, image.length);
    }

    @Test
    void getPostImageShouldThrowWhenPostMissing() {
        assertThrows(PostNotFoundException.class, () -> postService.getPostImage(999L));
    }

    @Test
    void getCommentsByPostIdShouldReturnComments() {
        List<CommentResponse> comments = postService.getCommentsByPostId(1L);

        assertEquals(2, comments.size());
        assertEquals(1L, comments.getFirst().id());
        assertEquals("First comment", comments.getFirst().text());
        assertEquals(1L, comments.getFirst().postId());
        assertEquals("Second comment", comments.get(1).text());
    }

    @Test
    void getCommentsByPostIdShouldThrowWhenPostMissing() {
        assertThrows(PostNotFoundException.class, () -> postService.getCommentsByPostId(999L));
    }
}
