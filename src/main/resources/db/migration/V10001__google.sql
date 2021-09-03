create sequence if not exists seq_google start with 1;
create table if not exists "google_tb"
(
    id bigint default nextval('seq_google') not null primary key,
    table_id varchar(200),
    table_name varchar(200),
    owner varchar(200)
);
