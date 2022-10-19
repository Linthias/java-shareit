CREATE TABLE IF NOT EXISTS "users"
(
    id        INTEGER     PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_name VARCHAR(30) NOT NULL,
    email     VARCHAR(50) NOT NULL UNIQUE
);
-- здесь и далее ограничения веншнего ключа (REFERENCES...)
-- убраны т.к. h2 в тестах на гитхабе некорректно их обрабатывает эти ограничения
CREATE TABLE IF NOT EXISTS "items"
(
    id           INTEGER      PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    item_name    VARCHAR(50)  NOT NULL,
    description  VARCHAR(200) NOT NULL,
    is_available BOOLEAN      NOT NULL,
    owner_id     INTEGER      NOT NULL --REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS "comments"
(
    id           INTEGER                     PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    comment_text VARCHAR(200)                NOT NULL,
    item         INTEGER                     NOT NULL,-- REFERENCES items (id),
    author       INTEGER                     NOT NULL,-- REFERENCES users (id),
    created      TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS "bookings"
(
    id         INTEGER                     PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    start_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_date   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    item_id    INTEGER                     NOT NULL,-- REFERENCES items (id),
    booker_id  INTEGER                     NOT NULL,-- REFERENCES users (id),
    status     VARCHAR(8)                  NOT NULL
);