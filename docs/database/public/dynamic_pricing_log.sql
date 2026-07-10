create table dynamic_pricing_log
(
    logid                serial
        primary key,
    productid            integer
        references product,
    oldprice             numeric(12, 2),
    newprice             numeric(12, 2),
    inventorylevelattime integer,
    demandscore          numeric(5, 2),
    reason               varchar(255),
    createdat            timestamp default CURRENT_TIMESTAMP
);

