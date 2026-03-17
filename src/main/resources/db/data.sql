DELETE FROM comments;
DELETE FROM post_images;
DELETE FROM post_tags;
DELETE FROM posts;

ALTER TABLE posts ALTER COLUMN id RESTART WITH 1;
ALTER TABLE comments ALTER COLUMN id RESTART WITH 1;

INSERT INTO posts (title, text, likes_count, comments_count)
VALUES ('Название поста 1', 'Текст поста в формате Markdown...', 5, 1),
       ('Название поста 2', 'Текст поста в формате Markdown...', 2, 0);

INSERT INTO post_tags (post_id, tag)
VALUES (1, 'tag_1'),
       (1, 'tag_2');

INSERT INTO comments (text, post_id)
VALUES ('Comment for post 1', 1);
