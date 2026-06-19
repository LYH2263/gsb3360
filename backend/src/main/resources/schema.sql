CREATE DATABASE IF NOT EXISTS campus_trade CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campus_trade;

DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS items;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS categories;

CREATE TABLE users (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    username    VARCHAR(64) NOT NULL UNIQUE,
    password    VARCHAR(128) NOT NULL,
    phone       VARCHAR(32),
    role        VARCHAR(32) NOT NULL DEFAULT 'USER',
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categories (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(64) NOT NULL
);

CREATE TABLE items (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    title        VARCHAR(128) NOT NULL,
    description  TEXT,
    price        DECIMAL(10, 2) NOT NULL,
    seller_id    BIGINT,
    buyer_id     BIGINT,
    status       VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    category_id  BIGINT,
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    traded_at    DATETIME NULL,
    CONSTRAINT fk_items_seller FOREIGN KEY (seller_id) REFERENCES users(id),
    CONSTRAINT fk_items_buyer FOREIGN KEY (buyer_id) REFERENCES users(id),
    CONSTRAINT fk_items_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE reviews (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    item_id     BIGINT NOT NULL,
    buyer_id    BIGINT NOT NULL,
    rating      INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    content     VARCHAR(500) NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_item FOREIGN KEY (item_id) REFERENCES items(id),
    CONSTRAINT fk_reviews_buyer FOREIGN KEY (buyer_id) REFERENCES users(id),
    CONSTRAINT uk_reviews_item_buyer UNIQUE (item_id, buyer_id)
);
