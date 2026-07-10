create table supplier
(
    supplierid  serial
        primary key,
    name        varchar(150) not null,
    contactname varchar(100),
    phone       varchar(20),
    address     text
);

