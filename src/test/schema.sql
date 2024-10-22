create table follow (
    is_each boolean not null,
    create_date timestamp(6),
    follow_id bigint not null,
    from_member_id bigint,
    to_member_id bigint,
    primary key (follow_id)
);

create table live_room (
    live_room_id bigint not null,
    member_id bigint unique,
    store_id bigint,
    session_id varchar(255),
    primary key (live_room_id)
);

create table live_room_participant (
    live_room_id bigint,
    live_room_participant_id bigint not null,
    member_id bigint,
    session_id varchar(255),
    primary key (live_room_participant_id)
);

create table member (
    is_live boolean not null,
    member_id bigint not null,
    name varchar(255),
    password varchar(255),
    phone_number varchar(255),
    username varchar(255),
    primary key (member_id)
);
create table member_image (
    member_id bigint unique,
    member_image_id bigint not null,
    url varchar(255),
    primary key (member_image_id)
);

create table menu (
    menu_price integer not null,
    menu_id bigint not null,
    post_id bigint,
    store_id bigint,
    menu_content varchar(255),
    menu_name varchar(255),
    primary key (menu_id)
);

create table menu_image (
    menu_id bigint unique,
    menu_image_id bigint not null,
    url varchar(255),
    primary key (menu_image_id)
);

create table post (
    category tinyint check (category between 0 and 5),
    is_auto boolean not null,
    permission tinyint check (permission between 0 and 2),
    create_date timestamp(6),
    member_id bigint,
    post_id bigint not null,
    store_id bigint,
    update_date timestamp(6),
    post_main_menu varchar(255),
    primary key (post_id)
);

create table share (
    share_id bigint not null,
    url varchar(255),
    vote_store_list blob,
    primary key (share_id)
);

create table store (
    posx float(53),
    posy float(53),
    store_id bigint not null,
    address varchar(255),
    store_name varchar(255),
    primary key (store_id)
);
