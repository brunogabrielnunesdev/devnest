create table community_moderation_cases (
    id uuid primary key,
    report_id uuid not null references community_reports(id),
    post_id uuid references community_posts(id),
    comment_id uuid references community_comments(id),
    status varchar(30) not null,
    opened_by_id uuid not null references users(id),
    opened_at timestamptz not null,
    resolved_by_id uuid references users(id),
    resolved_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uk_community_moderation_cases_report unique (report_id),
    constraint ck_community_moderation_cases_single_target check (
        (post_id is not null and comment_id is null)
        or (post_id is null and comment_id is not null)
    ),
    constraint ck_community_moderation_cases_status check (status in ('OPEN', 'RESOLVED')),
    constraint ck_community_moderation_cases_resolution check (
        (status = 'OPEN' and resolved_by_id is null and resolved_at is null)
        or (status = 'RESOLVED' and resolved_by_id is not null and resolved_at is not null)
    )
);

create index idx_community_moderation_cases_queue
    on community_moderation_cases (status, created_at, id);

create table community_moderation_actions (
    id uuid primary key,
    case_id uuid not null references community_moderation_cases(id),
    action_type varchar(30) not null,
    moderator_id uuid not null references users(id),
    reason varchar(1000) not null,
    previous_state varchar(100) not null,
    new_state varchar(100) not null,
    performed_at timestamptz not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint ck_community_moderation_actions_type check (
        action_type in (
            'HIDE',
            'RESTORE',
            'REMOVE',
            'LOCK_COMMENTS',
            'UNLOCK_COMMENTS',
            'RESOLVE_CASE'
        )
    )
);

create index idx_community_moderation_actions_case
    on community_moderation_actions (case_id, created_at, id);
