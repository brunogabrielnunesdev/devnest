create table community_rate_limit_events (
    id uuid primary key,
    actor_id uuid not null references users(id),
    action varchar(40) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create index idx_community_rate_limit_actor_action_created
    on community_rate_limit_events (actor_id, action, created_at);
