create table admins (
    id bigint not null,
    primary key (id)
) engine=InnoDB;
create table cod_remittances (
    created_at datetime(6),
    id bigint not null auto_increment,
    reviewed_at datetime(6),
    shipper_id bigint not null,
    total_amount bigint not null,
    code varchar(20),
    reviewed_by varchar(100),
    note varchar(500),
    status enum ('CHO_XAC_NHAN','DA_XAC_NHAN','TU_CHOI') not null,
    primary key (id)
) engine=InnoDB;
create table delivery_zones (
    id bigint not null auto_increment,
    code varchar(10) not null,
    name varchar(100) not null,
    description varchar(255),
    primary key (id)
) engine=InnoDB;
create table order_status_histories (
    created_at datetime(6),
    id bigint not null auto_increment,
    order_id bigint not null,
    actor_role varchar(30),
    actor_name varchar(100) not null,
    note varchar(500),
    from_status enum ('CHO_LAY_HANG','CHO_PHAN_CONG','DANG_GIAO','DANG_HOAN','DA_HOAN','DA_HUY','GIAO_THANH_CONG','GIAO_THAT_BAI'),
    to_status enum ('CHO_LAY_HANG','CHO_PHAN_CONG','DANG_GIAO','DANG_HOAN','DA_HOAN','DA_HUY','GIAO_THANH_CONG','GIAO_THAT_BAI') not null,
    primary key (id)
) engine=InnoDB;
create table orders (
    failed_attempts integer not null,
    weight_gram integer not null,
    cod_amount bigint not null,
    cod_remittance_id bigint,
    collected_amount bigint not null,
    created_at datetime(6),
    delivered_at datetime(6),
    delivery_ward_id bigint not null,
    finished_at datetime(6),
    id bigint not null auto_increment,
    pickup_ward_id bigint not null,
    settlement_id bigint,
    shipper_id bigint,
    shipping_fee bigint not null,
    shop_id bigint not null,
    updated_at datetime(6),
    version bigint not null,
    code varchar(20),
    receiver_phone varchar(20) not null,
    receiver_name varchar(100) not null,
    last_fail_reason varchar(500),
    note varchar(500),
    item_description varchar(255) not null,
    pickup_address varchar(255) not null,
    receiver_address varchar(255) not null,
    service_type enum ('HOA_TOC','TIEU_CHUAN') not null,
    status enum ('CHO_LAY_HANG','CHO_PHAN_CONG','DANG_GIAO','DANG_HOAN','DA_HOAN','DA_HUY','GIAO_THANH_CONG','GIAO_THAT_BAI') not null,
    primary key (id)
) engine=InnoDB;
create table price_configs (
    included_weight_gram integer not null,
    max_weight_gram integer not null,
    step_weight_gram integer not null,
    cross_zone_fee bigint not null,
    extra_fee_per_step bigint not null,
    id bigint not null auto_increment,
    same_zone_fee bigint not null,
    surcharge bigint not null,
    service_type enum ('HOA_TOC','TIEU_CHUAN') not null,
    primary key (id)
) engine=InnoDB;
create table settlements (
    created_at datetime(6),
    id bigint not null auto_increment,
    paid_at datetime(6),
    payout_amount bigint not null,
    shop_id bigint not null,
    total_cod bigint not null,
    total_fee bigint not null,
    code varchar(20),
    bank_account_no varchar(30),
    bank_account_holder varchar(100),
    bank_name varchar(100),
    payment_ref varchar(100),
    note varchar(500),
    status enum ('CHO_THANH_TOAN','DA_THANH_TOAN') not null,
    primary key (id)
) engine=InnoDB;
create table shippers (
    id bigint not null,
    zone_id bigint,
    vehicle_plate varchar(20),
    primary key (id)
) engine=InnoDB;
create table shops (
    id bigint not null,
    ward_id bigint not null,
    bank_account_no varchar(30),
    bank_account_holder varchar(100),
    bank_name varchar(100),
    shop_name varchar(150) not null,
    address varchar(255) not null,
    primary key (id)
) engine=InnoDB;
create table users (
    active bit not null,
    created_at datetime(6),
    id bigint not null auto_increment,
    phone varchar(20),
    username varchar(50) not null,
    full_name varchar(100) not null,
    password_hash varchar(255) not null,
    primary key (id)
) engine=InnoDB;
create table wards (
    id bigint not null auto_increment,
    zone_id bigint,
    name varchar(100) not null,
    primary key (id)
) engine=InnoDB;
alter table cod_remittances
   add constraint UK1dk5bfmlhgffd9abhvj1xjtcy unique (code);
alter table delivery_zones
   add constraint UKstx41s9bh35pwb08ceeablajt unique (code);
alter table orders
   add constraint UKgt3o4a5bqj59e9y6wakgk926t unique (code);
alter table price_configs
   add constraint UKjulm5ng6m87bhxko8qy0svo0j unique (service_type);
alter table settlements
   add constraint UKqcy9g1mnk154uert9u1ggw185 unique (code);
alter table users
   add constraint UKr43af9ap4edm43mmtq01oddj6 unique (username);
alter table wards
   add constraint UKp4qpc4vqg3wmcws774tb1v0qo unique (name);
alter table admins
   add constraint FKanhsicqm3lc8ya77tr7r0je18
   foreign key (id)
   references users (id);
alter table cod_remittances
   add constraint FKrmiqal789p2ckapga4k1nu3lg
   foreign key (shipper_id)
   references shippers (id);
alter table order_status_histories
   add constraint FKgair3047h1rin6u8y0fvc1fg5
   foreign key (order_id)
   references orders (id);
alter table orders
   add constraint FKco7djyl1xxqw9csbnhtfhfsit
   foreign key (cod_remittance_id)
   references cod_remittances (id);
alter table orders
   add constraint FKfyi3iyrysicuduf7q1j0l2wul
   foreign key (delivery_ward_id)
   references wards (id);
alter table orders
   add constraint FKlrkm4obkxpf3sfnfjse40y310
   foreign key (pickup_ward_id)
   references wards (id);
alter table orders
   add constraint FKijr115aumw04x5fe8yimi18fh
   foreign key (settlement_id)
   references settlements (id);
alter table orders
   add constraint FKsk2tyu7xrdu2ienuay5yrpgoe
   foreign key (shipper_id)
   references shippers (id);
alter table orders
   add constraint FK21gttsw5evi5bbsvleui69d7r
   foreign key (shop_id)
   references shops (id);
alter table settlements
   add constraint FKh53ycuwct0ace2tlkr4o4p198
   foreign key (shop_id)
   references shops (id);
alter table shippers
   add constraint FKrod8pde8r9oyddvm0e62hm3an
   foreign key (zone_id)
   references delivery_zones (id);
alter table shippers
   add constraint FKkkcun7hnrq8owetvqlqtijhav
   foreign key (id)
   references users (id);
alter table shops
   add constraint FK1tfea2bcy9s4ej59wxuutnulu
   foreign key (ward_id)
   references wards (id);
alter table shops
   add constraint FKt2sr62h19rfhk0ciye35rjw3s
   foreign key (id)
   references users (id);
alter table wards
   add constraint FK9i3qex1y4mqvpy8in2gghr7t4
   foreign key (zone_id)
   references delivery_zones (id);
