create table public.client_sync_entity
(
    client_id varchar(255) not null
        primary key
);

alter table public.client_sync_entity
    owner to postgres;

create table public.path
(
    id      uuid         not null
        primary key,
    path    varchar(255),
    sync_id varchar(255) not null
        constraint fkpu4o6u3oqp7ciusuw0h80pc5k
            references public.client_sync_entity,
    unique (path, sync_id)
);

alter table public.path
    owner to postgres;

