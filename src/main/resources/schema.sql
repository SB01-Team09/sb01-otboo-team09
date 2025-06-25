CREATE TABLE feeds
(
    id            UUID PRIMARY KEY,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITH TIME ZONE,
    author_id     UUID                     NOT NULL,
    content       TEXT                     NOT NULL,
    like_count    INTEGER                  NOT NULL,
    comment_count INTEGER                  NOT NULL
);

CREATE TABLE comments
(
    id         UUID PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    author_id  UUID                     NOT NULL,
    feed_id    UUID                     NOT NULL,
    content    TEXT                     NOT NULL
);

CREATE TABLE ootds
(
    id         UUID PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    feed_id    UUID                     NOT NULL,
    clothes_id UUID                     NOT NULL,

    CONSTRAINT unique_ootds_feed_clothes UNIQUE (feed_id, clothes_id)
);

CREATE TABLE likes
(
    id         UUID PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    feed_id    UUID                     NOT NULL,
    user_id    UUID                     NOT NULL,

    CONSTRAINT unique_likes_feed_user UNIQUE (feed_id, user_id)
);

CREATE TABLE direct_messages
(
    id          UUID PRIMARY KEY,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    sender_id   UUID                     NOT NULL,
    receiver_id UUID                     NOT NULL,
    content     TEXT                     NOT NULL
);

CREATE TABLE follows
(
    id          UUID PRIMARY KEY,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    followee_id UUID                     NOT NULL,
    follower_id UUID                     NOT NULL,

    CONSTRAINT unique_follow UNIQUE (followee_id, follower_id)
);

CREATE TABLE notifications
(
    id          UUID PRIMARY KEY,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    receiver_id UUID                     NOT NULL,
    title       VARCHAR(255)             NOT NULL,
    content     TEXT                     NOT NULL,
    level       VARCHAR(50)              NOT NULL
);

CREATE TABLE locations
(
    id         UUID PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    latitude   DOUBLE PRECISION,
    longitude  DOUBLE PRECISION,
    x          INTEGER,
    y          INTEGER
);

CREATE TABLE location_names
(
    id          UUID PRIMARY KEY,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    item        VARCHAR(255)             NOT NULL,
    location_id UUID                     NOT NULL
);

CREATE TABLE precipitations
(
    id          UUID PRIMARY KEY,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    type        VARCHAR(50),
    amount      DOUBLE PRECISION,
    probability DOUBLE PRECISION
);

CREATE TABLE humidities
(
    id                     UUID PRIMARY KEY,
    created_at             TIMESTAMP WITH TIME ZONE NOT NULL,
    current                DOUBLE PRECISION,
    compared_to_day_before DOUBLE PRECISION
);

CREATE TABLE temperatures
(
    id                     UUID PRIMARY KEY,
    created_at             TIMESTAMP WITH TIME ZONE NOT NULL,
    current                DOUBLE PRECISION,
    compared_to_day_before DOUBLE PRECISION,
    min                    DOUBLE PRECISION,
    max                    DOUBLE PRECISION
);

CREATE TABLE wind_speeds
(
    id         UUID PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    speed      DOUBLE PRECISION,
    as_word    VARCHAR(50)
);

CREATE TABLE weathers
(
    id               UUID PRIMARY KEY,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    forecasted_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    forecast_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    sky_status       VARCHAR(50)              NOT NULL,
    location_id      UUID                     NOT NULL,
    precipitation_id UUID                     NOT NULL,
    humidity_id      UUID                     NOT NULL,
    temperature_id   UUID                     NOT NULL,
    wind_speed_id    UUID                     NOT NULL
);

CREATE TABLE users
(
    id                      UUID PRIMARY KEY,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at              TIMESTAMP WITH TIME ZONE,
    email                   VARCHAR(100)             NOT NULL UNIQUE,
    name                    VARCHAR(100)             NOT NULL,
    password                TEXT                     NOT NULL,
    role                    VARCHAR(10)              NOT NULL,
    locked                  BOOLEAN                  NOT NULL,
    birth_date              DATE,
    temperature_sensitivity INTEGER,
    gender                  VARCHAR(10),
    location_id             uuid,
    profile_image_url       TEXT
);

CREATE TABLE oauth_providers
(
    id       UUID PRIMARY KEY,
    user_id  UUID        NOT NULL,
    provider VARCHAR(50) NOT NULL
);

CREATE TABLE clothes
(
    id         UUID PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    owner_id   UUID                     NOT NULL,
    name       VARCHAR(50)              NOT NULL,
    type       VARCHAR(10)              NOT NULL,
    image_url  TEXT
);

CREATE TABLE clothes_attributes
(
    id                  UUID PRIMARY KEY,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    clothes_id          UUID                     NOT NULL,
    selectable_value_id UUID                     NOT NULL
);

CREATE TABLE clothes_attribute_defs
(
    id         UUID PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    name       VARCHAR(50)              NOT NULL
);

CREATE TABLE selectable_values
(
    id               UUID PRIMARY KEY,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    attribute_def_id UUID                     NOT NULL,
    item             VARCHAR(50)              NOT NULL
);
