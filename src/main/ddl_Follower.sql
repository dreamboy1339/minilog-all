CREATE TABLE follows
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    follower_id BIGINT                NOT NULL,
    followee_id BIGINT                NOT NULL,
    CONSTRAINT pk_follows PRIMARY KEY (id)
);

ALTER TABLE follows
    ADD CONSTRAINT uc_001950816864276edd9fe655e UNIQUE (follower_id, followee_id);

ALTER TABLE follows
    ADD CONSTRAINT FK_FOLLOWS_ON_FOLLOWEE FOREIGN KEY (followee_id) REFERENCES users (id);

CREATE INDEX idx_followee_id ON follows (followee_id);

ALTER TABLE follows
    ADD CONSTRAINT FK_FOLLOWS_ON_FOLLOWER FOREIGN KEY (follower_id) REFERENCES users (id);

CREATE INDEX idx_follower_id ON follows (follower_id);
