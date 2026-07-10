create table orders
(
    orderid     serial
        primary key,
    customerid  integer
        references users,
    employeeid  integer
        references users,
    orderdate   timestamp      default CURRENT_TIMESTAMP,
    totalamount numeric(12, 2) not null,
    tax         numeric(12, 2) default 0,
    status      varchar(50)    default 'PENDING'::character varying
);

