create table auctions
(
    id               int auto_increment
        primary key,
    work_id          int            not null,
    seller_id        int            not null,
    auction_id       int            not null comment '链上拍卖id',
    start_price      decimal(10, 2) not null,
    current_price    decimal(10, 2) null,
    buyer_id         int            null,
    end_time         timestamp      not null,
    status           tinyint        not null comment '0 - active, 1 - ended',
    transaction_hash varchar(100)   not null,
    constraint auctions_pk
        unique (auction_id),
    constraint auctions_ibfk_1
        foreign key (work_id) references works (work_id),
    constraint auctions_ibfk_2
        foreign key (seller_id) references users (id),
    constraint auctions_ibfk_3
        foreign key (buyer_id) references users (id)
);

create index buyer_id
    on auctions (buyer_id);

create index seller_id
    on auctions (seller_id);

INSERT INTO digitalcopyright.auctions (id, work_id, seller_id, auction_id, start_price, current_price, buyer_id, end_time, status, transaction_hash) VALUES (39, 11, 5, 1, 999.00, 999.00, null, '2025-01-31 15:32:35', 1, '0x1ec46e9fc7cc58e9612a1eaafc5baf3d2464fa619ef8e14b380705416c1049a1');
INSERT INTO digitalcopyright.auctions (id, work_id, seller_id, auction_id, start_price, current_price, buyer_id, end_time, status, transaction_hash) VALUES (40, 12, 5, 2, 1999.00, 1999.00, null, '2025-01-30 15:32:49', 1, '0x6810703ddb11cd261d3c51f1872749257bc0ab4e8aa442e2cb5314dd36a03ea9');
INSERT INTO digitalcopyright.auctions (id, work_id, seller_id, auction_id, start_price, current_price, buyer_id, end_time, status, transaction_hash) VALUES (41, 13, 5, 3, 2999.00, 2999.00, null, '2025-01-29 15:33:07', 1, '0x9a8ebb2f8f2ce8ab7afc01eb297c001b9b9c87a2b5377203e610fe1e00c97a1b');
INSERT INTO digitalcopyright.auctions (id, work_id, seller_id, auction_id, start_price, current_price, buyer_id, end_time, status, transaction_hash) VALUES (42, 14, 5, 4, 2999.00, 2999.00, null, '2025-01-28 15:33:48', 1, '0x30572fca8c453581a47e0dad126bd9ddf7a9d1d228c5f0a7178bfce63d616885');
INSERT INTO digitalcopyright.auctions (id, work_id, seller_id, auction_id, start_price, current_price, buyer_id, end_time, status, transaction_hash) VALUES (43, 15, 5, 5, 888.00, 888.00, null, '2025-01-27 15:34:28', 1, '0x56fe6c647edddbc9b5e384f6d089afb3b075dd3ea849e66d8c0b7f69b5c64607');
INSERT INTO digitalcopyright.auctions (id, work_id, seller_id, auction_id, start_price, current_price, buyer_id, end_time, status, transaction_hash) VALUES (44, 1, 1, 6, 980.00, 980.00, null, '2025-01-26 15:35:31', 1, '0x16a1d7b195a5a722bf7a616055c6bf30058e8aea0879f8bf3b611b22844cc59a');
INSERT INTO digitalcopyright.auctions (id, work_id, seller_id, auction_id, start_price, current_price, buyer_id, end_time, status, transaction_hash) VALUES (45, 2, 1, 7, 4999.00, 4999.00, null, '2025-01-25 15:35:48', 1, '0x0ec7b92115dc0d5b9de0b8389efbbda7af03c08b4352debe383ee1a75a37b453');
INSERT INTO digitalcopyright.auctions (id, work_id, seller_id, auction_id, start_price, current_price, buyer_id, end_time, status, transaction_hash) VALUES (46, 3, 1, 8, 3399.00, 3399.00, null, '2025-01-24 15:36:10', 1, '0x805241d18d72dbbc879abec0e3516a65a3ea628ad8acde78d0693c01cd8eb0fd');
INSERT INTO digitalcopyright.auctions (id, work_id, seller_id, auction_id, start_price, current_price, buyer_id, end_time, status, transaction_hash) VALUES (47, 4, 1, 9, 4699.00, 4699.00, null, '2025-01-23 15:36:25', 1, '0xbadb5e606d6f13cd64e14d760d2ac74df9e3df84f7a498f97d7d0a2d75803d2c');
INSERT INTO digitalcopyright.auctions (id, work_id, seller_id, auction_id, start_price, current_price, buyer_id, end_time, status, transaction_hash) VALUES (48, 5, 1, 10, 6999.00, 6999.00, null, '2025-01-21 15:36:39', 1, '0x968b7499a363465e886222a422f0655125c1f29109b1de142e1efce2305cc1c1');
INSERT INTO digitalcopyright.auctions (id, work_id, seller_id, auction_id, start_price, current_price, buyer_id, end_time, status, transaction_hash) VALUES (49, 6, 6, 11, 300.00, 300.00, null, '2025-01-20 15:38:24', 1, '0x61da4987f6672d1a3110d44cdc3352b926ddfdd96541c9a49738199de008aa01');
INSERT INTO digitalcopyright.auctions (id, work_id, seller_id, auction_id, start_price, current_price, buyer_id, end_time, status, transaction_hash) VALUES (50, 7, 6, 12, 2499.00, 2499.00, null, '2025-01-19 15:38:40', 1, '0xc4217246039095e80aa008b124ca75e74618d6b87f98d3fc6a0c15e1efc29331');
INSERT INTO digitalcopyright.auctions (id, work_id, seller_id, auction_id, start_price, current_price, buyer_id, end_time, status, transaction_hash) VALUES (51, 8, 6, 13, 5600.00, 5600.00, null, '2025-01-18 15:38:57', 1, '0x784a162120c0e069505282b25be613c41f6b2dd7170799df932ec6fcef395c89');
INSERT INTO digitalcopyright.auctions (id, work_id, seller_id, auction_id, start_price, current_price, buyer_id, end_time, status, transaction_hash) VALUES (52, 9, 6, 14, 3600.00, 3600.00, null, '2025-01-16 15:39:11', 1, '0x5f322e0162b758c0c1a5586e4cfc455e8d6a7d842f77c85f4400b6256bb84a5b');
INSERT INTO digitalcopyright.auctions (id, work_id, seller_id, auction_id, start_price, current_price, buyer_id, end_time, status, transaction_hash) VALUES (53, 10, 6, 15, 1899.00, 1899.00, null, '2025-01-15 15:39:37', 1, '0x1558351fc331650005d701228063cfcecc7cf3a4925e3687349546a546ef067a');
INSERT INTO digitalcopyright.auctions (id, work_id, seller_id, auction_id, start_price, current_price, buyer_id, end_time, status, transaction_hash) VALUES (54, 16, 2, 16, 4000.00, 4000.00, null, '2025-01-14 15:53:19', 1, '0xef4ddf0660a6ea0e1b57026e711f2413a41adda4571e391d22dd3603108cc0e5');
INSERT INTO digitalcopyright.auctions (id, work_id, seller_id, auction_id, start_price, current_price, buyer_id, end_time, status, transaction_hash) VALUES (55, 17, 2, 17, 3400.00, 3400.00, null, '2025-01-13 15:54:08', 1, '0x9109e300987b58dd620978602d6713f677393a33ae76d414d3816c2e7d1f7cc3');
