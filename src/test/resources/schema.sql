CREATE TABLE category (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            name VARCHAR(255) NOT NULL
);

CREATE TABLE book (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       author VARCHAR(255) NOT NULL,
                       title VARCHAR(255) NOT NULL,
                       status VARCHAR(20) DEFAULT 'AVAILABLE'
);

CREATE TABLE book_category (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               book_id BIGINT,
                               category_id BIGINT,
                               FOREIGN KEY (book_id) REFERENCES book(id),
                               FOREIGN KEY (category_id) REFERENCES category(id)
);