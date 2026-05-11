CREATE TABLE articles
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    content    TEXT                  NULL,
    author_id  BIGINT                NOT NULL,
    created_at datetime              NOT NULL,
    updated_at datetime              NOT NULL,
    CONSTRAINT pk_articles PRIMARY KEY (id)
);

ALTER TABLE articles
    ADD CONSTRAINT FK_ARTICLES_ON_AUTHOR FOREIGN KEY (author_id) REFERENCES users (id);
