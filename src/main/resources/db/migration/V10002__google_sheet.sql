create sequence if not exists seq_google_sheet start with 1;
create table if not exists "google_sheet_tb"
(
    id bigint default nextval('seq_google_sheet') not null primary key,
    table_id bigint not null,
    sheet_id int not null,
    sheet_name varchar(200) not null
);