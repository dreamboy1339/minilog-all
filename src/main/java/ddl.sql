CREATE DATABASE `crypto_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */

CREATE DATABASE `minilog_all_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */

CREATE DATABASE `quake_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */

CREATE DATABASE `task_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */


create table minilog_all_db.BATCH_JOB_EXECUTION_SEQ
(
    ID         bigint not null,
    UNIQUE_KEY char   not null,
    constraint UNIQUE_KEY_UN
        unique (UNIQUE_KEY)
);

create table minilog_all_db.BATCH_JOB_INSTANCE
(
    JOB_INSTANCE_ID bigint       not null
        primary key,
    VERSION         bigint       null,
    JOB_NAME        varchar(100) not null,
    JOB_KEY         varchar(32)  not null,
    constraint JOB_INST_UN
        unique (JOB_NAME, JOB_KEY)
);

create table minilog_all_db.BATCH_JOB_EXECUTION
(
    JOB_EXECUTION_ID bigint        not null
        primary key,
    VERSION          bigint        null,
    JOB_INSTANCE_ID  bigint        not null,
    CREATE_TIME      datetime(6)   not null,
    START_TIME       datetime(6)   null,
    END_TIME         datetime(6)   null,
    STATUS           varchar(10)   null,
    EXIT_CODE        varchar(2500) null,
    EXIT_MESSAGE     varchar(2500) null,
    LAST_UPDATED     datetime(6)   null,
    constraint JOB_INST_EXEC_FK
        foreign key (JOB_INSTANCE_ID) references minilog_all_db.BATCH_JOB_INSTANCE (JOB_INSTANCE_ID)
);

create table minilog_all_db.BATCH_JOB_EXECUTION_CONTEXT
(
    JOB_EXECUTION_ID   bigint        not null
        primary key,
    SHORT_CONTEXT      varchar(2500) not null,
    SERIALIZED_CONTEXT text          null,
    constraint JOB_EXEC_CTX_FK
        foreign key (JOB_EXECUTION_ID) references minilog_all_db.BATCH_JOB_EXECUTION (JOB_EXECUTION_ID)
);

create table minilog_all_db.BATCH_JOB_EXECUTION_PARAMS
(
    JOB_EXECUTION_ID bigint        not null,
    PARAMETER_NAME   varchar(100)  not null,
    PARAMETER_TYPE   varchar(100)  not null,
    PARAMETER_VALUE  varchar(2500) null,
    IDENTIFYING      char          not null,
    constraint JOB_EXEC_PARAMS_FK
        foreign key (JOB_EXECUTION_ID) references minilog_all_db.BATCH_JOB_EXECUTION (JOB_EXECUTION_ID)
);

create table minilog_all_db.BATCH_JOB_SEQ
(
    ID         bigint not null,
    UNIQUE_KEY char   not null,
    constraint UNIQUE_KEY_UN
        unique (UNIQUE_KEY)
);

create table minilog_all_db.BATCH_STEP_EXECUTION
(
    STEP_EXECUTION_ID  bigint        not null
        primary key,
    VERSION            bigint        not null,
    STEP_NAME          varchar(100)  not null,
    JOB_EXECUTION_ID   bigint        not null,
    CREATE_TIME        datetime(6)   not null,
    START_TIME         datetime(6)   null,
    END_TIME           datetime(6)   null,
    STATUS             varchar(10)   null,
    COMMIT_COUNT       bigint        null,
    READ_COUNT         bigint        null,
    FILTER_COUNT       bigint        null,
    WRITE_COUNT        bigint        null,
    READ_SKIP_COUNT    bigint        null,
    WRITE_SKIP_COUNT   bigint        null,
    PROCESS_SKIP_COUNT bigint        null,
    ROLLBACK_COUNT     bigint        null,
    EXIT_CODE          varchar(2500) null,
    EXIT_MESSAGE       varchar(2500) null,
    LAST_UPDATED       datetime(6)   null,
    constraint JOB_EXEC_STEP_FK
        foreign key (JOB_EXECUTION_ID) references minilog_all_db.BATCH_JOB_EXECUTION (JOB_EXECUTION_ID)
);

create table minilog_all_db.BATCH_STEP_EXECUTION_CONTEXT
(
    STEP_EXECUTION_ID  bigint        not null
        primary key,
    SHORT_CONTEXT      varchar(2500) not null,
    SERIALIZED_CONTEXT text          null,
    constraint STEP_EXEC_CTX_FK
        foreign key (STEP_EXECUTION_ID) references minilog_all_db.BATCH_STEP_EXECUTION (STEP_EXECUTION_ID)
);

create table minilog_all_db.BATCH_STEP_EXECUTION_SEQ
(
    ID         bigint not null,
    UNIQUE_KEY char   not null,
    constraint UNIQUE_KEY_UN
        unique (UNIQUE_KEY)
);

create table crypto_db.crypto_prices
(
    id         bigint auto_increment
        primary key,
    coin       varchar(255)   not null,
    fetched_at datetime(6)    not null,
    price_krw  decimal(20, 8) null,
    price_usd  decimal(20, 8) null
);

create table task_db.devices
(
    id         bigint auto_increment
        primary key,
    created_at datetime(6)  not null,
    name       varchar(255) not null,
    type       varchar(255) not null,
    updated_at datetime(6)  not null
);

create table quake_db.earthquakes
(
    id         varchar(255) not null
        primary key,
    depth      double       null,
    event_time datetime(6)  null,
    latitude   double       null,
    longitude  double       null,
    mag_type   varchar(255) null,
    magnitude  double       null,
    place      varchar(512) null,
    updated_at datetime(6)  null,
    url        varchar(512) null
);

create table task_db.tasks
(
    id          bigint auto_increment
        primary key,
    created_at  datetime(6)                                  not null,
    description varchar(1000)                                null,
    name        varchar(255)                                 not null,
    status      enum ('COMPLETED', 'IN_PROGRESS', 'STARTED') not null,
    updated_at  datetime(6)                                  not null,
    device_id   bigint                                       not null,
    constraint FKpjvio5g6yb6oh9x95gddb2hrf
        foreign key (device_id) references task_db.devices (id)
);

create table minilog_all_db.users
(
    id         bigint auto_increment
        primary key,
    created_at datetime(6)  not null,
    password   varchar(255) not null,
    updated_at datetime(6)  not null,
    username   varchar(255) not null,
    constraint UKr43af9ap4edm43mmtq01oddj6
        unique (username)
);

create table minilog_all_db.articles
(
    id         bigint auto_increment
        primary key,
    content    text        null,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    author_id  bigint      not null,
    constraint FKe02fs2ut6qqoabfhj325wcjul
        foreign key (author_id) references minilog_all_db.users (id)
);

create table minilog_all_db.follows
(
    id          bigint auto_increment
        primary key,
    followee_id bigint not null,
    follower_id bigint not null,
    constraint UK53c7l7tclgps8jhvuioqbti7n
        unique (follower_id, followee_id),
    constraint FKeo7hqi2bt2vdwk6mpu0w2ihyb
        foreign key (followee_id) references minilog_all_db.users (id),
    constraint FKqnkw0cwwh6572nyhvdjqlr163
        foreign key (follower_id) references minilog_all_db.users (id)
);

create index idx_followee_id
    on minilog_all_db.follows (followee_id);

create index idx_follower_id
    on minilog_all_db.follows (follower_id);

create table minilog_all_db.task_reports
(
    id          bigint auto_increment
        primary key,
    content     text                                                          null,
    created_at  datetime(6)                                                   not null,
    status      enum ('APPROVAL', 'APPROVED', 'DRAFT', 'REVIEW', 'SUBMITTED') not null,
    updated_at  datetime(6)                                                   not null,
    approver_id bigint                                                        null,
    author_id   bigint                                                        not null,
    reviewer_id bigint                                                        null,
    task_id     bigint                                                        not null,
    constraint UK6rypobx7xtxrjvqkx909vbc5v
        unique (task_id),
    constraint FK201f9jpgyjyssusbnhci13y2b
        foreign key (task_id) references task_db.tasks (id),
    constraint FKf0qvmsd44jauq29tdy7axqb3s
        foreign key (approver_id) references minilog_all_db.users (id),
    constraint FKg2gqgjhcbtfn3ije4ehvyowsb
        foreign key (author_id) references minilog_all_db.users (id),
    constraint FKj3r0hnmqbyfnd1i10b0k0ceq7
        foreign key (reviewer_id) references minilog_all_db.users (id)
);

create table minilog_all_db.user_roles
(
    user_id bigint                                                               not null,
    role    enum ('ROLE_ADMIN', 'ROLE_APPROVER', 'ROLE_AUTHOR', 'ROLE_REVIEWER') null,
    constraint FKhfh9dx7w3ubf1co1vdev94g3f
        foreign key (user_id) references minilog_all_db.users (id)
);

