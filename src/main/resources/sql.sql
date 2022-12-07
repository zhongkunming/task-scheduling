create table sys_task
(
    task_name     varchar(255)  not null primary key,
    task_cron     varchar(255)  not null,
    execute_bean  varchar(255)  not null,
    task_enable   tinyint(1) not null,
    retry_time    int default 0 not null,
    is_concurrent tinyint(1) default 0 null,
    constraint sys_task_pk unique (task_name)
);

create table sys_task_log
(
    server_name       varchar(255)                       not null,
    task_name         varchar(255)                       not null,
    schedule_id       varchar(255)                       not null,
    success            tinyint(1)                         not null,
    schedule_time     datetime default CURRENT_TIMESTAMP not null,
    exception_message text                               null,
    exception_stack   text                               null
);