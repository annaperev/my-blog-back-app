package ru.yandex.practicum.blog.service.impl;

import org.junit.jupiter.api.Test;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultPostServiceTest {
    private final byte[][] savedImageHolder = new byte[1][];

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
        public Post create(String title, String text, List<String> tags) {
            return new Post(3L, title, text, tags, 0L, 0L);
        }

        @Override
        public Optional<Post> update(long id, String title, String text, List<String> tags) {
            if (id == 1L) {
                return Optional.of(new Post(id, title, text, tags, 5L, 1L));
            }
            return Optional.empty();
        }

        @Override
        public boolean deleteById(long id) {
            return id == 1L;
        }

        @Override
        public void saveImage(long id, byte[] imageBytes) {
            if (id == 1L) {
                savedImageHolder[0] = imageBytes;
            }
        }

        @Override
        public Optional<Long> incrementLikes(long id) {
            if (id == 1L) {
                return Optional.of(6L);
            }
            return Optional.empty();
        }

        @Override
        public Optional<Comment> findCommentById(long postId, long commentId) {
            return COMMENTS.stream()
                    .filter(comment -> comment.postId() == postId && comment.id() == commentId)
                    .findFirst();
        }

        @Override
        public Comment createComment(long postId, String text) {
            return new Comment(4L, text, postId);
        }

        @Override
        public Optional<Comment> updateComment(long postId, long commentId, String text) {
            if (postId == 1L && commentId == 1L) {
                return Optional.of(new Comment(commentId, text, postId));
            }
            return Optional.empty();
        }

        @Override
        public boolean deleteComment(long postId, long commentId) {
            return postId == 1L && commentId == 1L;
        }

        @Override
        public Optional<Post> findById(long id) {
            return POSTS.stream()
                    .filter(post -> post.id() == id)
                    .findFirst();
        }

        @Override
        public List<Post> findPage(String search, int pageNumber, int pageSize) {
            List<Post> filteredPosts = POSTS.stream()
                    .filter(post -> matchesSearchRules(post, search))
                    .toList();
            int fromIndex = Math.min((pageNumber - 1) * pageSize, filteredPosts.size());
            int toIndex = Math.min(fromIndex + pageSize, filteredPosts.size());
            return filteredPosts.subList(fromIndex, toIndex);
        }

        @Override
        public long countAll(String search) {
            return POSTS.stream()
                    .filter(post -> matchesSearchRules(post, search))
                    .count();
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
    void createPostShouldReturnCreatedPostWithZeroCounters() {
        CreatePostRequest request = new CreatePostRequest(
                "New post",
                "New text",
                List.of("java", "spring")
        );

        PostResponse response = postService.createPost(request);

        assertEquals(3L, response.id());
        assertEquals("New post", response.title());
        assertEquals("New text", response.text());
        assertEquals(List.of("java", "spring"), response.tags());
        assertEquals(0L, response.likesCount());
        assertEquals(0L, response.commentsCount());
    }

    @Test
    void updatePostShouldReturnUpdatedPostAndKeepCounters() {
        UpdatePostRequest request = new UpdatePostRequest(
                1L,
                "Updated title",
                "Updated text",
                List.of("updated_tag")
        );

        PostResponse response = postService.updatePost(request);

        assertEquals(1L, response.id());
        assertEquals("Updated title", response.title());
        assertEquals("Updated text", response.text());
        assertEquals(List.of("updated_tag"), response.tags());
        assertEquals(5L, response.likesCount());
        assertEquals(1L, response.commentsCount());
    }

    @Test
    void updatePostShouldThrowWhenPostMissing() {
        UpdatePostRequest request = new UpdatePostRequest(
                999L,
                "Updated title",
                "Updated text",
                List.of("updated_tag")
        );

        assertThrows(PostNotFoundException.class, () -> postService.updatePost(request));
    }

    @Test
    void deletePostShouldCompleteWhenPostExists() {
        postService.deletePost(1L);
    }

    @Test
    void deletePostShouldThrowWhenPostMissing() {
        assertThrows(PostNotFoundException.class, () -> postService.deletePost(999L));
    }

    @Test
    void updatePostImageShouldStoreBytesWhenPostExists() {
        byte[] imageBytes = new byte[]{4, 5, 6};

        postService.updatePostImage(1L, imageBytes);

        assertEquals(3, savedImageHolder[0].length);
        assertEquals(4, savedImageHolder[0][0]);
        assertEquals(5, savedImageHolder[0][1]);
        assertEquals(6, savedImageHolder[0][2]);
    }

    @Test
    void updatePostImageShouldThrowWhenPostMissing() {
        assertThrows(PostNotFoundException.class, () -> postService.updatePostImage(999L, new byte[]{1}));
    }

    @Test
    void incrementLikesShouldReturnUpdatedCount() {
        long likesCount = postService.incrementLikes(1L);

        assertEquals(6L, likesCount);
    }

    @Test
    void incrementLikesShouldThrowWhenPostMissing() {
        assertThrows(PostNotFoundException.class, () -> postService.incrementLikes(999L));
    }

    @Test
    void getCommentByIdShouldReturnComment() {
        CommentResponse response = postService.getCommentById(1L, 1L);

        assertEquals(1L, response.id());
        assertEquals("First comment", response.text());
        assertEquals(1L, response.postId());
    }

    @Test
    void getCommentByIdShouldThrowWhenCommentMissing() {
        assertThrows(CommentNotFoundException.class, () -> postService.getCommentById(1L, 999L));
    }

    @Test
    void createCommentShouldReturnCreatedComment() {
        CreateCommentRequest request = new CreateCommentRequest("New comment", 1L);

        CommentResponse response = postService.createComment(request);

        assertEquals(4L, response.id());
        assertEquals("New comment", response.text());
        assertEquals(1L, response.postId());
    }

    @Test
    void updateCommentShouldReturnUpdatedComment() {
        UpdateCommentRequest request = new UpdateCommentRequest(1L, "Updated comment", 1L);

        CommentResponse response = postService.updateComment(request);

        assertEquals(1L, response.id());
        assertEquals("Updated comment", response.text());
        assertEquals(1L, response.postId());
    }

    @Test
    void updateCommentShouldThrowWhenCommentMissing() {
        UpdateCommentRequest request = new UpdateCommentRequest(999L, "Updated comment", 1L);

        assertThrows(CommentNotFoundException.class, () -> postService.updateComment(request));
    }

    @Test
    void deleteCommentShouldCompleteWhenCommentExists() {
        postService.deleteComment(1L, 1L);
    }

    @Test
    void deleteCommentShouldThrowWhenCommentMissing() {
        assertThrows(CommentNotFoundException.class, () -> postService.deleteComment(1L, 999L));
    }

    @Test
    void getPostsShouldReturnPageAndApplyFeedTruncation() {
        PostPageResponse response = postService.getPosts("", 1, 1);

        assertEquals(1, response.posts().size());
        assertEquals("a".repeat(128) + "\u2026", response.posts().getFirst().text());
        assertFalse(response.hasPrev());
        assertTrue(response.hasNext());
        assertEquals(2, response.lastPage());
    }

    @Test
    void getPostsShouldFilterByHashTagTokens() {
        PostPageResponse response = postService.getPosts("#tag_2", 1, 10);

        assertEquals(1, response.posts().size());
        assertEquals(1L, response.posts().getFirst().id());
        assertFalse(response.hasPrev());
        assertFalse(response.hasNext());
        assertEquals(1, response.lastPage());
    }

    @Test
    void getPostsShouldApplyAndRulesForTitleAndTags() {
        PostPageResponse response = postService.getPosts("First #tag_1 #tag_2", 1, 10);

        assertEquals(1, response.posts().size());
        assertEquals(1L, response.posts().getFirst().id());
    }

    @Test
    void getPostsShouldNotMatchWhenTagRequirementFails() {
        PostPageResponse response = postService.getPosts("First #missing_tag", 1, 10);

        assertEquals(0, response.posts().size());
        assertEquals(1, response.lastPage());
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

    private boolean matchesSearchRules(Post post, String search) {
        SearchTerms searchTerms = parseSearch(search);
        if (searchTerms.titleSubstring() == null && searchTerms.requiredTags().isEmpty()) {
            return true;
        }

        boolean matchesTitle = searchTerms.titleSubstring() == null
                || post.title().toLowerCase().contains(searchTerms.titleSubstring());
        boolean matchesTags = searchTerms.requiredTags().stream()
                .allMatch(requiredTag -> post.tags().stream()
                        .anyMatch(tag -> tag.equalsIgnoreCase(requiredTag)));

        return matchesTitle && matchesTags;
    }

    private SearchTerms parseSearch(String search) {
        if (search == null || search.isBlank()) {
            return new SearchTerms(null, List.of());
        }

        List<String> titleWords = new java.util.ArrayList<>();
        List<String> requiredTags = new java.util.ArrayList<>();
        for (String word : search.trim().split("\\s+")) {
            if (word.isBlank()) {
                continue;
            }

            if (word.startsWith("#")) {
                String tag = word.substring(1).trim().toLowerCase();
                if (!tag.isBlank()) {
                    requiredTags.add(tag);
                }
                continue;
            }

            titleWords.add(word);
        }

        String titleSubstring = titleWords.isEmpty() ? null : String.join(" ", titleWords).toLowerCase();
        return new SearchTerms(titleSubstring, requiredTags.stream().distinct().toList());
    }

    private record SearchTerms(String titleSubstring, List<String> requiredTags) {
    }
}
