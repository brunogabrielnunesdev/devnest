create table community_forums (
    id uuid primary key,
    created_by_id uuid not null references users(id),
    name varchar(80) not null,
    slug varchar(100) not null,
    description varchar(500) not null,
    status varchar(20) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uk_community_forums_slug unique (slug),
    constraint ck_community_forums_status
        check (status in ('ACTIVE', 'ARCHIVED'))
);

create index idx_community_forums_status_name
    on community_forums (status, name);

create table community_tags (
    id uuid primary key,
    name varchar(50) not null,
    slug varchar(60) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uk_community_tags_slug unique (slug)
);

create table community_posts (
    id uuid primary key,
    forum_id uuid not null references community_forums(id),
    author_id uuid not null references users(id),
    project_id uuid references projects(id),
    course_id uuid references courses(id),
    title varchar(160) not null,
    content text not null,
    type varchar(30) not null,
    status varchar(30) not null,
    comments_locked boolean not null default false,
    removed_by_id uuid references users(id),
    removed_at timestamptz,
    removal_reason varchar(500),
    content_filter_rule_version varchar(100),
    content_filter_matched_terms text,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint ck_community_posts_type
        check (type in ('DISCUSSION', 'QUESTION', 'PROJECT_SHOWCASE', 'RESOURCE')),
    constraint ck_community_posts_status
        check (status in ('ACTIVE', 'HELD_FOR_REVIEW', 'HIDDEN', 'REMOVED')),
    constraint ck_community_posts_removal_data
        check (
            (status = 'REMOVED' and removed_by_id is not null and removed_at is not null and removal_reason is not null)
            or
            (status <> 'REMOVED' and removed_by_id is null and removed_at is null and removal_reason is null)
        ),
    constraint ck_community_posts_filter_data
        check (
            ((content_filter_rule_version is null) = (content_filter_matched_terms is null))
            and (status <> 'HELD_FOR_REVIEW' or content_filter_rule_version is not null)
            and (status <> 'ACTIVE' or content_filter_rule_version is null)
        )
);

create index idx_community_posts_feed
    on community_posts (status, created_at, id);

create index idx_community_posts_forum_feed
    on community_posts (forum_id, status, created_at, id);

create index idx_community_posts_author
    on community_posts (author_id, status, created_at);

create index idx_community_posts_project
    on community_posts (project_id);

create index idx_community_posts_course
    on community_posts (course_id);

create table community_post_tags (
    post_id uuid not null references community_posts(id) on delete cascade,
    tag_id uuid not null references community_tags(id),
    constraint uk_community_post_tags_post_tag unique (post_id, tag_id)
);

create index idx_community_post_tags_tag_post
    on community_post_tags (tag_id, post_id);
