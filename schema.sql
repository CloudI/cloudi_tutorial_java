---*-Mode:sql;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
-- ex: set ft=sql fenc=utf-8 sts=4 ts=4 sw=4 et:

-- Refresh schema with:
-- psql -h localhost cloudi_tutorial_java cloudi_tutorial_java < schema.sql

-- data from http://www.gutenberg.org/
DROP TABLE IF EXISTS items;
CREATE TABLE items (
    id BIGINT PRIMARY KEY NOT NULL,
    creator TEXT NULL,
    creator_link TEXT NULL,
    title TEXT NOT NULL,
    date_created DATE NOT NULL,
    languages TEXT[] NOT NULL,
    subjects TEXT[] NOT NULL,
    downloads INTEGER NOT NULL
);
DROP TABLE IF EXISTS subjects;
CREATE TABLE subjects (
    subject TEXT PRIMARY KEY NOT NULL,
    language TEXT NOT NULL
);
DROP TABLE IF EXISTS languages;
CREATE TABLE languages (
    language TEXT PRIMARY KEY NOT NULL
);

-- lenskit JDBC DAO values
-- (based on org.grouplens.lenskit.data.sql.BasicSQLStatementFactory
--  and org.grouplens.lenskit.data.event.Rating)
DROP TABLE IF EXISTS ratings;
CREATE TABLE ratings (
    user_id BIGINT NOT NULL,             -- Java long
    item_id BIGINT NOT NULL,             -- Java long
    rating DOUBLE PRECISION NOT NULL,    -- Java double [0.5 .. 5.0]
    timestamp BIGINT NULL,               -- Java long (microseconds)
    PRIMARY KEY(user_id, item_id)
);
-- upsert as a stored procedure for the ratings table
DROP FUNCTION IF EXISTS rate(BIGINT,
                             BIGINT,
                             DOUBLE PRECISION);
CREATE FUNCTION rate(rate_user_id BIGINT,
                     rate_item_id BIGINT,
                     rate_rating DOUBLE PRECISION) RETURNS VOID AS
$$
DECLARE
    rate_timestamp CONSTANT BIGINT := (EXTRACT(epoch FROM now()) *
                                       1000000)::BIGINT;
BEGIN
    -- UPSERT on missed table
    LOOP
        UPDATE ratings
        SET rating = rate_rating,
            timestamp = rate_timestamp
        WHERE user_id = rate_user_id AND
              item_id = rate_item_id;
        IF found THEN
            RETURN;
        END IF;
        -- If concurrent insert, unique-key failure causes an UPDATE instead
        BEGIN
            INSERT INTO ratings(user_id, item_id, rating, timestamp)
            VALUES (rate_user_id, rate_item_id, rate_rating, rate_timestamp);
            RETURN;
        EXCEPTION WHEN unique_violation THEN
            -- concurrent insert caused unique-key exception, UPDATE instead
        END;
    END LOOP;
END;
$$
LANGUAGE plpgsql;

