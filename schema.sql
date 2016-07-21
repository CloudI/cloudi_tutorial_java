---*-Mode:sql;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
-- ex: set ft=sql fenc=utf-8 sts=4 ts=4 sw=4 et:

-- Refresh schema with:
-- psql -h localhost cloudi_tutorial_java cloudi_tutorial_java < schema.sql

DROP TABLE IF EXISTS books;

CREATE TABLE books (
    id TEXT PRIMARY KEY NOT NULL,
    creator TEXT NULL,
    creator_link TEXT NULL,
    title TEXT NOT NULL,
    date_created DATE NOT NULL,
    languages TEXT[] NOT NULL,
    subjects TEXT[] NOT NULL,
    downloads INTEGER NOT NULL
);

