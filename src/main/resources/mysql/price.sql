create table price
(
    id               int auto_increment comment '出价id'
        primary key,
    auction_id       int                                 not null comment '拍卖的id',
    work_id          int                                 not null comment '作品id',
    user_id          int                                 null comment '出价id',
    price            int                                 not null comment '出价金额',
    transaction_hash varchar(255)                        not null comment '交易哈希',
    created_at       timestamp default CURRENT_TIMESTAMP not null comment '出价时间',
    constraint price_pk_2
        unique (id),
    constraint price_auctions_auction_id_fk
        foreign key (auction_id) references auctions (auction_id),
    constraint price_users_id_fk
        foreign key (user_id) references users (id),
    constraint price_works_work_id_fk
        foreign key (work_id) references works (work_id)
)
    comment '出价信息表';

INSERT INTO digitalcopyright.price (id, auction_id, work_id, user_id, price, transaction_hash, created_at) VALUES (6, 13, 8, 1, 7000, '0x387a13937b5ebf6bf93427a1e70b57de95cab8fdbeaff82d733d636a74074dc8', '2025-01-03 21:01:14');
INSERT INTO digitalcopyright.price (id, auction_id, work_id, user_id, price, transaction_hash, created_at) VALUES (7, 13, 8, 1, 8100, '0x9dc2a44dfbd03fd367bb103d8917f768b0200e877ce555d87aac1a067b38cc78', '2025-01-03 21:01:29');
INSERT INTO digitalcopyright.price (id, auction_id, work_id, user_id, price, transaction_hash, created_at) VALUES (8, 13, 8, 1, 8200, '0x60aef3bc9742948ec6fcc61691a7f13b46fcfb8e7f8ebb6f325f37cb6afa2343', '2025-01-03 21:01:32');
INSERT INTO digitalcopyright.price (id, auction_id, work_id, user_id, price, transaction_hash, created_at) VALUES (9, 13, 8, 1, 8300, '0xe64f802fd660471660ddf8285130d417d4c745bb31c8255f89fd61a6fee3f8e4', '2025-01-03 21:01:35');
INSERT INTO digitalcopyright.price (id, auction_id, work_id, user_id, price, transaction_hash, created_at) VALUES (10, 13, 8, 1, 8500, '0x0eafb61527f32ae1a9d05c37b8cccb18a172d9bb5b58da6a387fc99b44197cb9', '2025-01-03 21:40:14');
INSERT INTO digitalcopyright.price (id, auction_id, work_id, user_id, price, transaction_hash, created_at) VALUES (11, 12, 7, 1, 3000, '0x4f1b5931b33efe5b0d7226378f9b266de69358a63d9be1ab1dfb075dbf547e2a', '2025-01-03 21:41:06');
INSERT INTO digitalcopyright.price (id, auction_id, work_id, user_id, price, transaction_hash, created_at) VALUES (12, 3, 13, 1, 3000, '0x06087c757c1dfaf0d6d0a84bf6234c0bd0d4e308deb900e6b981544d1c2c1e58', '2025-01-03 21:41:26');
INSERT INTO digitalcopyright.price (id, auction_id, work_id, user_id, price, transaction_hash, created_at) VALUES (13, 17, 17, 1, 3500, '0xfda3cb51069a0a8ecd5160b0ab2a1689d343e9e90cbc714d915a23052bdbcab9', '2025-01-03 21:41:43');
INSERT INTO digitalcopyright.price (id, auction_id, work_id, user_id, price, transaction_hash, created_at) VALUES (14, 13, 8, 2, 9000, '0x9f32667b31e4efe40a151d215f03838f511acec6d65a2fa04cde83298864fed5', '2025-01-04 09:59:12');
INSERT INTO digitalcopyright.price (id, auction_id, work_id, user_id, price, transaction_hash, created_at) VALUES (15, 13, 8, 2, 10100, '0x4ae58d79e51b088435748ef8cd57ac43025f9262e31ee80a1be6c6a7ebcdffc6', '2025-01-04 09:59:32');
INSERT INTO digitalcopyright.price (id, auction_id, work_id, user_id, price, transaction_hash, created_at) VALUES (16, 13, 8, 2, 11100, '0x72757fe0ccb5e955f03028e44794704f24d136036aac50b5f3c61c64b00117cf', '2025-01-04 09:59:37');
INSERT INTO digitalcopyright.price (id, auction_id, work_id, user_id, price, transaction_hash, created_at) VALUES (17, 13, 8, 2, 12000, '0xf133a4a9281f52d623216d0b3b358d84dfc31ce00ba681f5566da81a3b5208cc', '2025-01-04 10:02:34');
INSERT INTO digitalcopyright.price (id, auction_id, work_id, user_id, price, transaction_hash, created_at) VALUES (18, 13, 8, 2, 13000, '0xe5e5dd97d1b39a87698b4b6da4ec8ad9e9c1eb8d0124e732d3f4651069904d9f', '2025-01-04 10:03:37');
INSERT INTO digitalcopyright.price (id, auction_id, work_id, user_id, price, transaction_hash, created_at) VALUES (19, 13, 8, 2, 14000, '0xcadf01cf801b57bb668e16488eaa1e6561379576774a5ff8059aee80df4104b3', '2025-01-04 10:07:44');
INSERT INTO digitalcopyright.price (id, auction_id, work_id, user_id, price, transaction_hash, created_at) VALUES (20, 15, 10, 1, 2000, '0x2098b2310e747bcfd4fb0190c4be5234b475655590572dfa3a5fd216dd2e38db', '2025-01-04 10:59:14');
INSERT INTO digitalcopyright.price (id, auction_id, work_id, user_id, price, transaction_hash, created_at) VALUES (21, 16, 16, 1, 4100, '0xb01a315193eb9fae91d2e7249400ebf38385c5e5cfe28c656466dc4c598747d0', '2025-01-04 10:59:31');
