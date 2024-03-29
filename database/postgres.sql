create table bbs_user
(
    id           bigint            not null
        primary key,
    openid       char(28)          not null
        unique,
    phone        char(11),
    nick_name    varchar(255),
    avatar       varchar(255),
    age          integer,
    gender       integer,
    address      varchar(255),
    introduction text,
    create_time  timestamp,
    role         integer default 0 not null,
    status       integer default 0 not null,
    user_auth    integer default 0 not null
);

comment on table bbs_user is '校园论坛用户表';

comment on column bbs_user.id is '用户ID,雪花ID生成';

comment on column bbs_user.openid is '用户在微信中的Openid';

comment on column bbs_user.phone is '用户在微信中的手机号';

comment on column bbs_user.nick_name is '用户昵称';

comment on column bbs_user.avatar is '用户头像地址';

comment on column bbs_user.age is '用户年龄';

comment on column bbs_user.gender is '用户性别 0：男 1:女';

comment on column bbs_user.address is '用户所在地区 省-市，湖北-武汉';

comment on column bbs_user.introduction is '用户简介';

comment on column bbs_user.create_time is '用户创建时间';

comment on column bbs_user.role is '用户角色 0：普通用户 1：学生用户';

comment on column bbs_user.status is '是否删除 0：正常  1：已删除';

comment on column bbs_user.user_auth is '用户授权 0：正常 1：用户被禁用';

alter table bbs_user
    owner to postgres;

create table bbs_student
(
    student_id     char(7)      not null
        primary key,
    user_id        bigint       not null
        unique,
    student_name   varchar(100) not null,
    student_phone  char(11)     not null,
    student_gender integer      not null,
    academy_name   varchar(100),
    class_name     varchar(100),
    dorm_name      varchar(100),
    school         varchar(100),
    status         integer default 0
);

comment on table bbs_student is '校园论坛学生认证信息表';

comment on column bbs_student.student_id is '学生学号';

comment on column bbs_student.user_id is '关联的用户ID';

comment on column bbs_student.student_name is '学生姓名';

comment on column bbs_student.student_phone is '学生手机号';

comment on column bbs_student.student_gender is '学生性别 0：男 1：女';

comment on column bbs_student.academy_name is '学院名称';

comment on column bbs_student.class_name is '班级名称';

comment on column bbs_student.dorm_name is '宿舍号';

comment on column bbs_student.school is '毕业院校';

comment on column bbs_student.status is '状态 0:正常 1：删除';

alter table bbs_student
    owner to postgres;

create table bbs_file_upload
(
    id          serial
        primary key,
    md5         char(32)     not null,
    origin_name varchar(200) not null,
    file_name   varchar(200) not null,
    size        bigint       not null,
    url         varchar(300) not null,
    upload_time timestamp    not null
);

comment on table bbs_file_upload is '文件上传表';

comment on column bbs_file_upload.id is 'id 主键 自增';

comment on column bbs_file_upload.md5 is '文件32位MD5 非空';

comment on column bbs_file_upload.origin_name is '文件原始名称 非空';

comment on column bbs_file_upload.file_name is '文件保存的名称 非空';

comment on column bbs_file_upload.size is '文件大小';

comment on column bbs_file_upload.url is '文件访问Url';

comment on column bbs_file_upload.upload_time is '文件上传时间';

alter table bbs_file_upload
    owner to postgres;

create table bbs_forum_topic
(
    id               serial
        primary key,
    title            varchar(255)      not null,
    content          text              not null,
    content_key      text,
    image_urls       varchar(300)[],
    video_url        varchar(300),
    type             integer           not null,
    flag             integer,
    label_ids        integer[],
    is_hot           boolean default false,
    view_count       integer default 0,
    star_count       integer default 0,
    create_time      timestamp         not null,
    user_id          bigint            not null,
    status           integer default 1,
    last_update_time timestamp,
    sort             integer default 0 not null
);

comment on table bbs_forum_topic is '论坛帖子/话题信息表';

comment on column bbs_forum_topic.id is '帖子/话题ID 自增主键';

comment on column bbs_forum_topic.title is '帖子/话题标题';

comment on column bbs_forum_topic.content is '帖子/话题内容';

comment on column bbs_forum_topic.content_key is '帖子/话题关键字';

comment on column bbs_forum_topic.image_urls is '图片连接数组';

comment on column bbs_forum_topic.video_url is '视频链接';

comment on column bbs_forum_topic.type is '类型 0:帖子 1：话题';

comment on column bbs_forum_topic.flag is '帖子标签 0：其他 1：失物招领 2：二手交易 3：校园求助 4：学习资源 话题则空';

comment on column bbs_forum_topic.label_ids is '话题的标签列表，帖子则为空';

comment on column bbs_forum_topic.is_hot is '是否热门话题';

comment on column bbs_forum_topic.view_count is '查看次数';

comment on column bbs_forum_topic.star_count is '收藏次数';

comment on column bbs_forum_topic.create_time is '创建时间';

comment on column bbs_forum_topic.user_id is '用户ID';

comment on column bbs_forum_topic.status is '状态 0：正常 1：待审核 2：审核未通过 3：已删除';

comment on column bbs_forum_topic.last_update_time is '帖子/话题的最后更新时间（包括审核状态更新，帖子被删除等）';

comment on column bbs_forum_topic.sort is '帖子排序字段，值越大优先级越高';

alter table bbs_forum_topic
    owner to postgres;

create table bbs_propose_topic
(
    id          serial
        primary key,
    user_id     bigint           not null,
    topic_id    integer          not null,
    create_time timestamp        not null,
    similarity  double precision not null
);

comment on table bbs_propose_topic is '用户话题推送表';

comment on column bbs_propose_topic.id is 'ID主键 自增';

comment on column bbs_propose_topic.user_id is '推送的用户ID';

comment on column bbs_propose_topic.topic_id is '推送的话题ID';

comment on column bbs_propose_topic.create_time is '推送创建时间';

comment on column bbs_propose_topic.similarity is '话题与用户的相关度';

alter table bbs_propose_topic
    owner to postgres;

create table bbs_public_message
(
    id            serial
        primary key,
    content       text      not null,
    send_time     timestamp not null,
    read_user_ids bigint[],
    del_user_ids  bigint[],
    status        integer default 0
);

comment on table bbs_public_message is '公共消息表';

comment on column bbs_public_message.id is 'ID 主键 自增';

comment on column bbs_public_message.content is '消息内容';

comment on column bbs_public_message.send_time is '消息发送时间';

comment on column bbs_public_message.read_user_ids is '已读该消息的用户';

comment on column bbs_public_message.del_user_ids is '删除该消息的用户';

comment on column bbs_public_message.status is '状态 0：正常 1：删除';

alter table bbs_public_message
    owner to postgres;

create table bbs_user_extras
(
    id          serial
        primary key,
    user_id     bigint not null
        unique,
    update_time timestamp default CURRENT_TIMESTAMP,
    topic_stars integer[]
);

comment on table bbs_user_extras is '用户额外信息表';

comment on column bbs_user_extras.id is 'ID主键 自增';

comment on column bbs_user_extras.user_id is '用户ID';

comment on column bbs_user_extras.update_time is '最后更新时间';

comment on column bbs_user_extras.topic_stars is '用户收藏的帖子/话题列表';

alter table bbs_user_extras
    owner to postgres;

create table bbs_user_orbit
(
    id          bigserial
        primary key,
    user_id     bigint       not null,
    create_time timestamp    not null,
    ip_address  varchar(100) not null,
    coordinate  varchar(255) not null
);

comment on table bbs_user_orbit is '用户轨迹表';

comment on column bbs_user_orbit.id is 'ID 主键 自增';

comment on column bbs_user_orbit.user_id is '用户ID';

comment on column bbs_user_orbit.create_time is '轨迹上传时间';

comment on column bbs_user_orbit.ip_address is '用户IP地址';

comment on column bbs_user_orbit.coordinate is '用户坐标，gps经纬度和高度';

alter table bbs_user_orbit
    owner to postgres;

create table bbs_user_action
(
    id          bigserial
        primary key,
    user_id     bigint    not null,
    topic_id    integer   not null,
    create_time timestamp not null,
    stay_time   integer   not null
);

comment on table bbs_user_action is '用户行为表';

comment on column bbs_user_action.id is 'ID 主键 自增';

comment on column bbs_user_action.user_id is '用户Id';

comment on column bbs_user_action.topic_id is '浏览的帖子/话题ID';

comment on column bbs_user_action.stay_time is '浏览时长';

alter table bbs_user_action
    owner to postgres;

create table bbs_user_profile
(
    id          serial
        primary key,
    user_id     bigint    not null
        unique,
    update_time timestamp not null,
    user_labels varchar(200)[]
);

comment on table bbs_user_profile is '用户画像表';

comment on column bbs_user_profile.id is 'ID 主键 自增';

comment on column bbs_user_profile.user_id is '用户ID 唯一';

comment on column bbs_user_profile.update_time is '最后更新时间';

comment on column bbs_user_profile.user_labels is '用户标签列表';

alter table bbs_user_profile
    owner to postgres;

create table bbs_topic_label
(
    id          serial
        primary key,
    name        varchar(100) not null
        unique,
    create_time timestamp    not null,
    status      integer default 0
);

comment on table bbs_topic_label is '话题标签表';

comment on column bbs_topic_label.id is 'ID 主键 自增';

comment on column bbs_topic_label.name is '标签名称 唯一 非空';

comment on column bbs_topic_label.create_time is '标签创建时间';

comment on column bbs_topic_label.status is '状态 0：正常 1：删除';

alter table bbs_topic_label
    owner to postgres;

create table bbs_comment
(
    id          bigserial
        primary key,
    content     text      not null,
    create_time timestamp not null,
    topic_id    integer   not null,
    fid         bigint  default 0,
    rid         bigint  default 0,
    user_id     bigint    not null,
    status      integer default 0
);

comment on table bbs_comment is '统一评论表';

comment on column bbs_comment.id is 'ID 主键 自增';

comment on column bbs_comment.content is '评论内容';

comment on column bbs_comment.create_time is '评论创建时间';

comment on column bbs_comment.topic_id is '帖子/话题ID';

comment on column bbs_comment.fid is '一级评论ID';

comment on column bbs_comment.rid is '上级评论ID';

comment on column bbs_comment.user_id is '发送评论的用户ID';

comment on column bbs_comment.status is '状态 0：正常 1：删除';

alter table bbs_comment
    owner to postgres;

create table bbs_user_message
(
    id         serial
        primary key,
    user_id    bigint    not null,
    content    text      not null,
    send_time  timestamp not null,
    type       integer   not null,
    topic_id   integer,
    comment_id bigint,
    is_read    boolean default false,
    status     integer default 0
);

comment on table bbs_user_message is '用户消息表';

comment on column bbs_user_message.id is 'ID 主键 自增';

comment on column bbs_user_message.user_id is '消息需要送达的UserId';

comment on column bbs_user_message.content is '消息内容';

comment on column bbs_user_message.send_time is '消息的发送时间';

comment on column bbs_user_message.type is '消息类型 0：话题/帖子回复消息 1：评论回复消息';

comment on column bbs_user_message.topic_id is '消息对应的帖子/话题ID';

comment on column bbs_user_message.comment_id is '消息对应的评论ID, 如果是话题/帖子回复消息则为空';

comment on column bbs_user_message.is_read is '消息用户是否已读';

comment on column bbs_user_message.status is '状态 0：正常 1：删除';

alter table bbs_user_message
    owner to postgres;

create table bbs_review_task
(
    id           serial
        primary key,
    topic_id     integer           not null,
    create_time  timestamp         not null,
    stage1       boolean,
    stage2       boolean,
    stage3       boolean,
    execute_time timestamp         not null,
    retry_count  integer default 0 not null
);

comment on table bbs_review_task is '帖子/话题审查任务表，如果在帖子/话题审查阶段出现报错或者审查失败则写入任务表，后台定时通过自动任务重试';

comment on column bbs_review_task.topic_id is '帖子/话题Id';

comment on column bbs_review_task.create_time is '任务的创建时间';

comment on column bbs_review_task.stage1 is '一阶段任务，帖子的文本审查 通过：true 不通过：false 调用失败：null';

comment on column bbs_review_task.stage2 is '二阶段任务，帖子的图片审查';

comment on column bbs_review_task.stage3 is '三阶段任务，帖子的视频审查';

comment on column bbs_review_task.execute_time is '当前任务的最后一次执行时间';

comment on column bbs_review_task.retry_count is '当前任务的重试次数';

alter table bbs_review_task
    owner to postgres;

create table bbs_user_friends
(
    id          serial
        primary key,
    user_id     bigint not null
        unique,
    friends_ids bigint[],
    update_time timestamp default CURRENT_TIMESTAMP
);

comment on table bbs_user_friends is '用户好友表';

comment on column bbs_user_friends.id is '主键Id,自增';

comment on column bbs_user_friends.user_id is '用户ID,唯一';

comment on column bbs_user_friends.friends_ids is '该用户的好友id列表';

comment on column bbs_user_friends.update_time is '数据的最后更新时间';

alter table bbs_user_friends
    owner to postgres;