create table community_user_blocks (
    id uuid primary key,
    blocker_id uuid not null references users(id),
    blocked_user_id uuid not null references users(id),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uk_community_user_blocks_pair unique (blocker_id, blocked_user_id),
    constraint ck_community_user_blocks_not_self check (blocker_id <> blocked_user_id)
);

create index idx_community_user_blocks_blocked
    on community_user_blocks (blocked_user_id, blocker_id);

create table community_user_mutes (
    id uuid primary key,
    user_id uuid not null references users(id),
    muted_user_id uuid not null references users(id),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uk_community_user_mutes_pair unique (user_id, muted_user_id),
    constraint ck_community_user_mutes_not_self check (user_id <> muted_user_id)
);

create index idx_community_user_mutes_muted
    on community_user_mutes (muted_user_id, user_id);
