drop table if exists captcha_codes;
drop table if exists global_settings;
drop table if exists hibernate_sequence;
drop table if exists post_comments;
drop table if exists post_votes;
drop table if exists posts;
drop table if exists tag2post;
drop table if exists tags;
drop table if exists users;

create table captcha_codes (
        id integer not null,
        code varchar(255),
        secret_code varchar(255),
        timestamp datetime(6),
        primary key (id)
        );

create table global_settings (
        id integer not null,
        multiuser_mode bit not null,
        post_premoderation bit not null,
        statistics_is_public bit not null,
        primary key (id)
        );
create table hibernate_sequence (next_val bigint);
insert into hibernate_sequence values ( 1 );


create table post_comments (
        id integer not null auto_increment,
        parent_id integer,
        post_id integer,
        text varchar(255),
        time datetime(6),
        user_id integer,
        primary key (id)
        );
create table post_votes (
        id integer not null auto_increment,
        post_id integer,
        time datetime(6),
        user_id integer,
        value integer,
        primary key (id)
        );
create table posts (
        id integer not null auto_increment,
        is_active integer,
        moderation_status varchar(255),
        moderator_id integer,
        text text,
        timestamp datetime(6),
        title varchar(255),
        user_id integer,
        view_count integer,
        primary key (id)
        );
create table tag2post (
        id integer not null auto_increment,
        post_id integer,
        tag_id integer,
        primary key (id)
        );
create table tags (
        id integer not null auto_increment,
        name varchar(255),
        primary key (id)
        );
create table users (
        id integer not null auto_increment,
        code varchar(255),
        e_mail varchar(255),
        is_moderator integer,
        name varchar(255),
        password varchar(255),
        photo varchar(255),
        reg_time datetime(6),
        primary key (id)
        );
alter table users add constraint user_email_fk unique (e_mail);
alter table post_comments add constraint post_comments_post_id_fk foreign key (post_id) references posts (id);
alter table post_votes add constraint post_votes_post_id_fk foreign key (post_id) references posts (id);