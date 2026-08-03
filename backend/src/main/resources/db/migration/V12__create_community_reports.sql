create table community_reports (
    id uuid primary key,
    reporter_id uuid not null references users(id),
    post_id uuid references community_posts(id),
    comment_id uuid references community_comments(id),
    reason varchar(40) not null,
    description varchar(1000),
    status varchar(30) not null,
    reviewed_by_id uuid references users(id),
    reviewed_at timestamptz,
    review_note varchar(1000),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uk_community_reports_reporter_post unique (reporter_id, post_id),
    constraint uk_community_reports_reporter_comment unique (reporter_id, comment_id),
    constraint ck_community_reports_single_target check (
        (post_id is not null and comment_id is null)
        or (post_id is null and comment_id is not null)
    ),
    constraint ck_community_reports_reason check (
        reason in (
            'SPAM',
            'HARASSMENT',
            'HATE_SPEECH',
            'SEXUAL_CONTENT',
            'VIOLENCE',
            'MISINFORMATION',
            'OTHER'
        )
    ),
    constraint ck_community_reports_status check (
        status in ('PENDING', 'CONFIRMED', 'DISMISSED')
    ),
    constraint ck_community_reports_review_data check (
        (
            status = 'PENDING'
            and reviewed_by_id is null
            and reviewed_at is null
            and review_note is null
        )
        or (
            status in ('CONFIRMED', 'DISMISSED')
            and reviewed_by_id is not null
            and reviewed_at is not null
            and review_note is not null
        )
    )
);

create index idx_community_reports_queue
    on community_reports (status, created_at, id);

create index idx_community_reports_post
    on community_reports (post_id, status);

create index idx_community_reports_comment
    on community_reports (comment_id, status);
