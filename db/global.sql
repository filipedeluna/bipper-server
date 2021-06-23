-- Config ----------------------------------------------------------------
SET TIMEZONE = 'Europe/Lisbon';

CREATE TABLE IF NOT EXISTS locations
(
    location_id       SERIAL PRIMARY KEY,
    location_district TEXT NOT NULL,
    location_county   TEXT NOT NULL,
    location_zone     TEXT NOT NULL,
    CONSTRAINT unique_location UNIQUE (location_district, location_county, location_zone)
);

CREATE TABLE IF NOT EXISTS users
(
    user_id          CHAR(64) PRIMARY KEY,
    user_banned      BOOLEAN DEFAULT FALSE NOT NULL,
    user_admin       BOOLEAN DEFAULT FALSE NOT NULL,
    user_score       INTEGER DEFAULT 0     NOT NULL,
    user_location_id INTEGER               NOT NULL,
    CONSTRAINT fk_user_location_id
        FOREIGN KEY (user_location_id)
            REFERENCES locations (location_id)
);

CREATE TABLE IF NOT EXISTS posts
(
    post_id          SERIAL PRIMARY KEY,
    post_user_id     CHAR(64)                 NOT NULL,
    CONSTRAINT fk_user_id
        FOREIGN KEY (post_user_id)
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
    vote_post_id INTEGER  NOT NULL,
    CONSTRAINT fk_post_id
        FOREIGN KEY (vote_post_id)
            REFERENCES posts (post_id),
    vote_user_id CHAR(64) NOT NULL,
    CONSTRAINT fk_user_id
        FOREIGN KEY (vote_user_id)
            REFERENCES users (user_id),

    PRIMARY KEY (vote_post_id, vote_user_id)
);
