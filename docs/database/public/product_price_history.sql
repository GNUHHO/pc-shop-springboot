CREATE TABLE product_price_history
(
    pricehistoryid  SERIAL PRIMARY KEY,
    productid       INTEGER        NOT NULL,
    oldbaseprice    NUMERIC(12, 2) NOT NULL,
    newbaseprice    NUMERIC(12, 2) NOT NULL,
    olddynamicprice NUMERIC(12, 2) NOT NULL,
    newdynamicprice NUMERIC(12, 2) NOT NULL,
    changetype      VARCHAR(30)    NOT NULL,
    changesource    VARCHAR(30)    NOT NULL,
    changereason    VARCHAR(500),
    changedat       TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_price_history_product
        FOREIGN KEY (productid)
        REFERENCES product (productid)
        ON DELETE RESTRICT,

    CONSTRAINT chk_price_history_change_type
        CHECK (
            changetype IN (
                'BASE_PRICE',
                'CURRENT_DYNAMIC_PRICE',
                'BOTH'
            )
        ),

    CONSTRAINT chk_price_history_change_source
        CHECK (
            changesource IN (
                'MANUAL',
                'DYNAMIC_PRICING'
            )
        ),

    CONSTRAINT chk_price_history_prices_non_negative
        CHECK (
            oldbaseprice >= 0
            AND newbaseprice >= 0
            AND olddynamicprice >= 0
            AND newdynamicprice >= 0
        )
);

CREATE INDEX idx_price_history_product_changed_at
    ON product_price_history (productid, changedat DESC);