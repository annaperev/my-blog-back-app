DELETE FROM post_tags;
DELETE FROM posts;

INSERT INTO posts (id, title, text, likes_count, comments_count)
VALUES (1, 'Название поста 1', 'Текст поста в формате Markdown...', 5, 1);

INSERT INTO post_tags (post_id, tag)
VALUES (1, 'tag_1'),
       (1, 'tag_2');
