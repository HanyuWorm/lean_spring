create table if not exists registered_order (
    order_id varchar(100) primary key,
    status varchar(30) not null
);
