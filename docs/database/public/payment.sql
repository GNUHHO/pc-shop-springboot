create table payment
(
    paymentid   serial
        primary key,
    orderid     integer
        references orders,
    amount      numeric(12, 2) not null,
    method      varchar(50),
    referenceno varchar(100),
    paymentdate timestamp default CURRENT_TIMESTAMP
);

