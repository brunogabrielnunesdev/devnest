create table project_tasks (
    id uuid primary key,
    project_id uuid not null references projects(id) on delete cascade,
    title varchar(160) not null,
    description text,
    status varchar(20) not null check (status in ('TODO', 'IN_PROGRESS', 'DONE')),
    priority varchar(20) not null check (priority in ('LOW', 'MEDIUM', 'HIGH')),
    assigned_to_id uuid references users(id),
    due_date date,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table project_notes (
    id uuid primary key,
    project_id uuid not null references projects(id) on delete cascade,
    author_id uuid not null references users(id),
    content text not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table project_members (
    id uuid primary key,
    project_id uuid not null references projects(id) on delete cascade,
    user_id uuid not null references users(id),
    role varchar(20) not null check (role in ('OWNER', 'ADMIN', 'MEMBER', 'VIEWER')),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uk_project_members_project_user unique (project_id, user_id)
);

create table project_activity_logs (
    id uuid primary key,
    project_id uuid not null references projects(id) on delete cascade,
    actor_id uuid not null references users(id),
    type varchar(40) not null,
    message varchar(255) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);
