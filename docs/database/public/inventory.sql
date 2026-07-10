create table inventory
(
    inventoryid    serial
        primary key,
    productid      integer
        references product,
    supplierid     integer
        references supplier,
    quantityonhand integer   default 0 not null,
    costperunit    numeric(12, 2)      not null,
    location       varchar(100),
    lastrestocked  timestamp default CURRENT_TIMESTAMP
);

