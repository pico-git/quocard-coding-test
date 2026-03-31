CREATE TABLE author (
                        id SERIAL PRIMARY KEY,
                        name VARCHAR(255) NOT NULL,
                        birth_date DATE NOT NULL
);

CREATE TABLE book (
                      id SERIAL PRIMARY KEY,
                      title VARCHAR(255) NOT NULL,
                      price INT NOT NULL,
                      status INT NOT NULL DEFAULT 0 -- 0:未出版, 1:出版済み
);

CREATE TABLE book_author (
                             book_id INT REFERENCES book(id) ON DELETE CASCADE,
                             author_id INT REFERENCES author(id) ON DELETE CASCADE,
                             PRIMARY KEY (book_id, author_id)
);