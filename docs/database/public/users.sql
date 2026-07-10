create table users
(
    userid       serial
        primary key,
    firstname    varchar(50)  not null,
    lastname     varchar(50)  not null,
    email        varchar(100) not null
        unique,
    passwordhash varchar(255) not null,
    role         varchar(20)  not null,
    phone        varchar(20),
    address      text
);

