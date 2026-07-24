create table community_comments (
    id uuid primary key,
    post_id uuid not null references community_posts(id),
    author_id uuid not null references users(id),
    content varchar(5000) not null,
    status varchar(30) not null,
    removed_by_id uuid references users(id),
    removed_at timestamptz,
    removal_reason varchar(500),
    content_filter_rule_version varchar(100),
    content_filter_matched_terms text,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint ck_community_comments_status
        check (status in ('ACTIVE', 'HELD_FOR_REVIEW', 'HIDDEN', 'REMOVED')),
    constraint ck_community_comments_removal_data
        check (
            (status = 'REMOVED' and removed_by_id is not null and removed_at is not null and removal_reason is not null)
            or
            (status <> 'REMOVED' and removed_by_id is null and removed_at is null and removal_reason is null)
        ),
    constraint ck_community_comments_filter_data
        check (
            ((content_filter_rule_version is null) = (content_filter_matched_terms is null))
            and (status <> 'HELD_FOR_REVIEW' or content_filter_rule_version is not null)
            and (status <> 'ACTIVE' or content_filter_rule_version is null)
        )
);

create index idx_community_comments_post_feed
    on community_comments (post_id, status, created_at, id);

create index idx_community_comments_author
    on community_comments (author_id, status, created_at);
