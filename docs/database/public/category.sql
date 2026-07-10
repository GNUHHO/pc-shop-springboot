create table category
(
    categoryid  serial
        primary key,
    name        varchar(100) not null,
    description text
);

