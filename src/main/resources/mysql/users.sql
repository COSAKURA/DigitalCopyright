create table users
(
    id                 int auto_increment
        primary key,
    username           varchar(50)                         not null,
    password           varchar(100)                        not null,
    email              varchar(100)                        not null,
    blockchain_address varchar(100)                        null,
    type               enum ('0', '1')                     null comment '0普通用户，1审核机构',
    created_at         timestamp default CURRENT_TIMESTAMP null,
    status             char(4)                             null comment ' 用户正常 用户异常',
    constraint blockchain_address
        unique (blockchain_address),
    constraint email
        unique (email),
    constraint username
        unique (username),
    constraint users_pk
        unique (email)
);

INSERT INTO digitalcopyright.users (id, username, password, email, blockchain_address, type, created_at, status) VALUES (1, '区块链系统开发工程师', '$2a$10$U.rapNplkMrRJHMmvq/.8.L5KDh6UEkOsxc5k08SrZcUB.rr5s62O', '847070349@qq.com', '0xf060283f22a813b3f8a2649605b2ccb8ee3a1c0c', '1', '2024-11-28 11:18:10', '正常');
INSERT INTO digitalcopyright.users (id, username, password, email, blockchain_address, type, created_at, status) VALUES (2, '区块链应用开发工程师', '$2a$10$YkZDmvQnycMR9ZcxD6l8GeoKAk/fEROf0MJeBvF7uYhjPwglUEKb.', '2515695498@qq.com', '0x2a37895d6c29d12a48e73dc3c8df0f396f995bd2', '0', '2024-12-02 22:17:30', '正常');
INSERT INTO digitalcopyright.users (id, username, password, email, blockchain_address, type, created_at, status) VALUES (5, '区块链运维工程师', '$2a$10$vRdirZPlNcNmitl6eo9GhOZiV3YEEi/PcpiJoIKVL/V2yoDi7bY0K', '2064583375@qq.com', '0x50ef918bd22b2bb25eb4e118d3480f2b39a2b76e', '0', '2024-12-19 18:44:07', '正常');
INSERT INTO digitalcopyright.users (id, username, password, email, blockchain_address, type, created_at, status) VALUES (6, '产品经理', '$2a$10$iQazGGJOnWn0dHjldbtg2eaaDWPOk3GxI9oXn05TC9VC9njGhhUwe', '2794841874@qq.com', '0xf2b14c896ea0cb6d7362e321c73888441ecb9023', '0', '2024-12-19 18:56:27', '正常');
