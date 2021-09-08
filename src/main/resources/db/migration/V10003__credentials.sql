create sequence if not exists seq_credentials start with 1;
create table if not exists "credentials_tb"
(
    id bigint default nextval('seq_credentials') not null primary key,
    login varchar(200) not null unique,
    password varchar(200) not null,
    refresh_token varchar(200) not null
);