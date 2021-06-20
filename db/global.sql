-- Config ----------------------------------------------------------------
SET TIMEZONE = 'Europe/Lisbon';

CREATE TABLE IF NOT EXISTS locations
(
    location_id SERIAL PRIMARY KEY,
    district    TEXT NOT NULL,
    county      TEXT NOT NULL,
    zone        TEXT NOT NULL,
    CONSTRAINT unique_location UNIQUE (district, county, zone)
);

CREATE TABLE IF NOT EXISTS users
(
    user_id     CHAR(32) PRIMARY KEY,
    user_banned BOOLEAN DEFAULT FALSE NOT NULL,
    user_admin  BOOLEAN DEFAULT FALSE NOT NULL,
    user_points INTEGER DEFAULT 0     NOT NULL
);

CREATE TABLE IF NOT EXISTS posts
(
    post_id          SERIAL PRIMARY KEY,
    user_id          CHAR(32)                 NOT NULL,
    CONSTRAINT fk_user_id
        FOREIGN KEY (user_id)
            REFERENCES users (user_id),
    post_location_id INTEGER                  NOT NULL,
    CONSTRAINT fk_post_location_id
        FOREIGN KEY (post_location_id)
            REFERENCES locations (location_id),
    post_date        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    post_score       INTEGER                           DEFAULT 0 NOT NULL,
    post_text        TEXT                     NOT NULL,
    post_image       TEXT                     NOT NULL
);

CREATE TABLE IF NOT EXISTS votes
(
    post_id INTEGER  NOT NULL,
    CONSTRAINT fk_post_id
        FOREIGN KEY (post_id)
            REFERENCES posts (post_id),
    user_id CHAR(32) NOT NULL,
    CONSTRAINT fk_user_id
        FOREIGN KEY (user_id)
            REFERENCES users (user_id),

    PRIMARY KEY (post_id, user_id)
);

CREATE OR REPLACE FUNCTION add_user_vote_on_new_post()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
BEGIN
    INSERT INTO votes(post_id, user_id)
    VALUES (new.post_id, new.user_id);
END;
$$;

CREATE TRIGGER user_new_post
    AFTER INSERT
    ON posts
    FOR EACH ROW
EXECUTE PROCEDURE add_user_vote_on_new_post();


