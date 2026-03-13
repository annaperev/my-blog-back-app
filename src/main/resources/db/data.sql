DELETE FROM comments;
DELETE FROM post_images;
DELETE FROM post_tags;
DELETE FROM posts;

INSERT INTO posts (id, title, text, likes_count, comments_count)
VALUES (1, 'Название поста 1', 'Текст поста в формате Markdown...', 5, 1),
       (2, 'Название поста 2', 'Текст поста в формате Markdown...', 2, 2);

INSERT INTO post_tags (post_id, tag)
VALUES (1, 'tag_1'),
       (1, 'tag_2');

INSERT INTO comments (id, text, post_id)
VALUES (1, 'Comment for post 1', 1);
