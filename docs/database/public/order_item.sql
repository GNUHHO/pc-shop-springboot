create table order_item
(
    orderitemid serial
        primary key,
    orderid     integer
        references orders
            on delete cascade,
    productid   integer
        references product,
    quantity    integer        not null,
    unitprice   numeric(12, 2) not null,
    discount    numeric(12, 2) default 0
);

