insert into blog.users
(id, is_moderator, reg_time, name, e_mail, password, code, photo)
values(1, 0, now(), "user1", "user1@mail.ru", "$2a$12$8esbwYeFCScKSniYdAiNIOWnAJFb8Hqw3NCrJgg/nOrU4Ly.aiCIO", NULL, NULL);

insert into blog.users
(id, is_moderator, reg_time, name, e_mail, password, code, photo)
values(2, 1, now(), "user2", "user2@mail.ru", "$2a$12$rkhvdJPtIG1sBodQJSA7ve4eNcvBUOeOSePcq5wXt5dS7JV4PApfe", NULL, NULL);