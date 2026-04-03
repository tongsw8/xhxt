create table banner
(
    id          bigint auto_increment comment '主键'
        primary key,
    title       varchar(100)                       null comment '标题',
    img_url     varchar(255)                       not null comment '图片地址',
    link_url    varchar(255)                       null comment '跳转链接',
    sort_no     int      default 0                 null comment '排序',
    status      tinyint  default 1                 null comment '1启用 0禁用',
    create_time datetime default CURRENT_TIMESTAMP null
)
    comment '轮播图' charset = utf8mb4;

create table comment
(
    id          bigint auto_increment comment '主键'
        primary key,
    target_id   bigint                             not null comment '目标ID(资讯ID或帖子ID)',
    target_type varchar(20)                        not null comment '类型: NEWS / POST',
    user_id     bigint                             not null comment '评论人ID',
    parent_id   bigint                             null comment '父评论ID(回复用)',
    content     text                               not null comment '评论内容',
    is_staff    tinyint  default 0                 null comment '是否官方回复:1是0否',
    status      tinyint  default 1                 null comment '1审核通过 0待审核 -1审核拒绝',
    create_time datetime default CURRENT_TIMESTAMP null
)
    comment '通用评论表' charset = utf8mb4;

create index idx_comment_target
    on comment (target_id, target_type);

create index idx_comment_user
    on comment (user_id);

create table forum_category
(
    id            bigint auto_increment comment '主键'
        primary key,
    category_name varchar(64)                        not null comment '板块名称',
    description   varchar(255)                       null comment '板块描述',
    sort_no       int      default 0                 null,
    create_time   datetime default CURRENT_TIMESTAMP null
)
    comment '论坛板块' charset = utf8mb4;

create table forum_post
(
    id          bigint auto_increment comment '主键'
        primary key,
    category_id bigint                             not null comment '板块ID',
    user_id     bigint                             not null comment '发布人ID',
    title       varchar(200)                       not null comment '帖子标题',
    content     longtext                           not null comment '帖子内容',
    view_count  int      default 0                 null comment '浏览量',
    is_top      tinyint  default 0                 null comment '是否置顶:1是0否',
    is_best     tinyint  default 0                 null comment '是否加精:1是0否',
    status      tinyint  default 1                 null comment '1正常 0违规删除',
    create_time datetime default CURRENT_TIMESTAMP null,
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    product_id  bigint                             null comment '关联商品ID'
)
    comment '论坛帖子' charset = utf8mb4;

create index idx_post_category
    on forum_post (category_id);

create index idx_post_user
    on forum_post (user_id);

create table news_category
(
    id            bigint auto_increment comment '主键'
        primary key,
    category_name varchar(64)                        not null comment '分类名称',
    sort_no       int      default 0                 null,
    create_time   datetime default CURRENT_TIMESTAMP null
)
    comment '资讯分类' charset = utf8mb4;

create table news_info
(
    id          bigint auto_increment comment '主键'
        primary key,
    category_id bigint                             not null comment '分类ID',
    title       varchar(200)                       not null comment '资讯标题',
    summary     varchar(500)                       null comment '简介',
    content     longtext                           not null comment '正文内容',
    cover_img   varchar(255)                       null comment '封面图',
    view_count  int      default 0                 null comment '浏览量',
    status      tinyint  default 1                 null comment '1发布 0下线',
    create_time datetime default CURRENT_TIMESTAMP null,
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
)
    comment '新闻资讯' charset = utf8mb4;

create index idx_news_category
    on news_info (category_id);

create table notice
(
    id           bigint auto_increment comment '主键'
        primary key,
    title        varchar(200)                         not null comment '公告标题',
    content      text                                 not null comment '公告内容',
    status       tinyint    default 1                 null comment '1发布 0撤回',
    create_time  datetime   default CURRENT_TIMESTAMP null,
    update_time  datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    cover_img    varchar(500)                         null comment '封面图URL',
    is_top       tinyint(1) default 0                 not null comment '是否置顶 0否1是',
    publish_time datetime                             null comment '发布时间'
)
    comment '系统公告' charset = utf8mb4;

create table product_category
(
    id            bigint auto_increment comment '主键'
        primary key,
    category_name varchar(64)                        not null comment '分类名称',
    sort_no       int      default 0                 not null comment '排序号，越小越靠前',
    status        tinyint  default 1                 not null comment '1启用 0停用',
    create_time   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_category_name
        unique (category_name)
)
    comment '商品分类' charset = utf8mb4;

create table product_info
(
    id           bigint auto_increment comment '主键'
        primary key,
    category_id  bigint                                   not null comment '分类ID',
    product_name varchar(100)                             not null comment '商品名称',
    price        decimal(10, 2) default 0.00              not null comment '售价',
    stock        int            default 0                 not null comment '库存',
    cover_img    varchar(255)                             null comment '主图路径',
    detail_text  text                                     null comment '图文详情（先存文本）',
    status       tinyint        default 1                 not null comment '1上架 0下架',
    create_time  datetime       default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time  datetime       default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint fk_product_category
        foreign key (category_id) references product_category (id)
)
    comment '商品信息' charset = utf8mb4;

create index idx_product_category
    on product_info (category_id);

create index idx_product_status
    on product_info (status);

create table user_action
(
    id          bigint auto_increment comment '主键'
        primary key,
    user_id     bigint                             not null comment '用户ID',
    target_id   bigint                             not null comment '目标ID',
    target_type varchar(20)                        not null comment '类型: NEWS / POST / PRODUCT',
    action_type varchar(20)                        not null comment '行为: LIKE(点赞) / COLLECT(收藏)',
    create_time datetime default CURRENT_TIMESTAMP null,
    constraint uk_user_target_action
        unique (user_id, target_id, target_type, action_type)
)
    comment '用户互动行为' charset = utf8mb4;

create table user_address
(
    id             bigint auto_increment comment '主键'
        primary key,
    user_id        bigint                             not null comment '用户ID',
    receiver_name  varchar(64)                        not null comment '收货人',
    receiver_phone varchar(32)                        not null comment '联系电话',
    province       varchar(64)                        null comment '省',
    city           varchar(64)                        null comment '市',
    district       varchar(64)                        null comment '区',
    detail_address varchar(255)                       not null comment '详细地址',
    is_default     tinyint  default 0                 not null comment '是否默认地址:1是0否',
    create_time    datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '用户收货地址' charset = utf8mb4;

create index idx_address_user
    on user_address (user_id);

create table user_cart
(
    id          bigint auto_increment comment '主键'
        primary key,
    user_id     bigint                             not null comment '用户ID',
    product_id  bigint                             not null comment '商品ID',
    quantity    int      default 1                 not null comment '数量',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_user_product
        unique (user_id, product_id)
)
    comment '用户购物车' charset = utf8mb4;

create index idx_cart_product
    on user_cart (product_id);

create index idx_cart_user
    on user_cart (user_id);

create table user_favorite
(
    id          bigint auto_increment comment '主键'
        primary key,
    user_id     bigint                             not null comment '用户ID',
    product_id  bigint                             not null comment '商品ID',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    constraint uk_user_fav_product
        unique (user_id, product_id)
)
    comment '用户收藏' charset = utf8mb4;

create index idx_fav_product
    on user_favorite (product_id);

create index idx_fav_user
    on user_favorite (user_id);

create table user_order
(
    id                   bigint auto_increment comment '主键'
        primary key,
    order_no             varchar(64)                              not null comment '订单号',
    user_id              bigint                                   not null comment '用户ID',
    total_amount         decimal(10, 2) default 0.00              not null comment '订单总金额',
    status               tinyint        default 0                 not null comment '状态:0待支付 1已支付 2已关闭',
    expire_time          datetime                                 not null comment '过期时间(30分钟)',
    pay_time             datetime                                 null comment '支付时间',
    close_time           datetime                                 null comment '关闭时间',
    receiver_name        varchar(64)                              null comment '收货人',
    receiver_phone       varchar(32)                              null comment '收货电话',
    receiver_address     varchar(255)                             null comment '收货地址快照',
    create_time          datetime       default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time          datetime       default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    delivery_time        datetime                                 null comment '发货时间',
    finish_time          datetime                                 null comment '完成时间',
    express_company      varchar(64)                              null comment '快递公司',
    tracking_no          varchar(64)                              null comment '物流单号',
    card_message         varchar(255)                             null comment '贺卡留言',
    delivery_expect_time varchar(64)                              null comment '配送时间要求',
    notify_read_admin    tinyint(1)     default 1                 not null comment '管理员是否已读通知 0未读1已读',
    notify_read_staff    tinyint(1)     default 1                 not null comment '员工是否已读通知 0未读1已读',
    urge_ship            tinyint(1)     default 0                 not null comment '是否催发货 0否1是',
    urge_time            datetime                                 null comment '催发货时间',
    constraint uk_order_no
        unique (order_no)
)
    comment '用户订单' charset = utf8mb4;

create index idx_order_expire_time
    on user_order (expire_time);

create index idx_order_status
    on user_order (status);

create index idx_order_user
    on user_order (user_id);

create table user_order_item
(
    id            bigint auto_increment comment '主键'
        primary key,
    order_id      bigint                                   not null comment '订单ID',
    product_id    bigint                                   not null comment '商品ID',
    product_name  varchar(100)                             not null comment '下单时商品名快照',
    product_price decimal(10, 2) default 0.00              not null comment '下单时单价快照',
    quantity      int            default 1                 not null comment '购买数量',
    cover_img     varchar(255)                             null comment '下单时主图快照',
    create_time   datetime       default CURRENT_TIMESTAMP not null comment '创建时间'
)
    comment '订单明细' charset = utf8mb4;

create index idx_item_order
    on user_order_item (order_id);

create index idx_item_product
    on user_order_item (product_id);

create table users
(
    id          bigint auto_increment comment '主键'
        primary key,
    account     varchar(64)                           not null comment '登录账号',
    password    varchar(128)                          not null comment '密码(MD5)',
    nickname    varchar(64)                           null comment '昵称',
    role        varchar(32) default 'USER'            not null comment '角色: ADMIN/STAFF/USER',
    status      tinyint     default 1                 not null comment '1正常 0禁用',
    reg_time    datetime    default CURRENT_TIMESTAMP null comment '注册时间',
    real_name   varchar(50)                           null comment '真实姓名',
    phone       varchar(32)                           null comment '手机号',
    email       varchar(128)                          null comment '邮箱',
    gender      tinyint     default 0                 null comment '性别:0未知 1男 2女',
    update_time datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_users_account
        unique (account)
)
    comment '用户与登录' charset = utf8mb4;

