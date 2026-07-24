create table community_reactions (
    id uuid primary key,
    user_id uuid not null references users(id),
    post_id uuid references community_posts(id),
    comment_id uuid references community_comments(id),
    type varchar(30) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint ck_community_reactions_type
        check (type in ('LIKE', 'HELPFUL', 'CELEBRATE', 'INSIGHTFUL')),
    constraint ck_community_reactions_single_target
        check ((post_id is not null) <> (comment_id is not null)),
    constraint uk_community_reactions_user_post unique (user_id, post_id),
    constraint uk_community_reactions_user_comment unique (user_id, comment_id)
);

create index idx_community_reactions_post_type
    on community_reactions (post_id, type);

create index idx_community_reactions_comment_type
    on community_reactions (comment_id, type);
