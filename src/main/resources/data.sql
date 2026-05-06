INSERT INTO product (
    name,
    price,
    total_stock,
    reserved_stock,
    check_in_at,
    check_out_at,
    event_product
)
VALUES
    (
        '호텔 1박 숙박권',
        5000,
        10,
        0,
        '2026-05-10 15:00:00',
        '2026-05-11 11:00:00',
        true
    );

INSERT INTO member_point (member_id, balance, reserved)
VALUES (1, 10000, 0);