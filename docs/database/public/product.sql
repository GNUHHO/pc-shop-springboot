create table product
(
    productid           serial
        primary key,
    categoryid          integer
        references category,
    sku                 varchar(50)    not null
        unique,
    name                varchar(200)   not null,
    baseprice           numeric(12, 2) not null,
    currentdynamicprice numeric(12, 2) not null,
    specs               jsonb,
    isactive            boolean default true
);

