create table users (
    id uuid primary key,
    email varchar(320) not null unique,
    password_hash varchar(255) not null,
    role varchar(20) not null check (role in ('ADMIN', 'STUDENT', 'TEACHER')),
    status varchar(20) not null check (status in ('ACTIVE', 'BLOCKED', 'DELETED')),
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table user_profiles (
    id uuid primary key,
    user_id uuid not null unique references users(id),
    display_name varchar(80) not null,
    full_name varchar(120),
    bio text,
    avatar_url varchar(255),
    github_url varchar(255),
    linkedin_url varchar(255),
    portfolio_url varchar(255),
    location varchar(120),
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table courses (
    id uuid primary key,
    teacher_id uuid not null references users(id),
    title varchar(160) not null,
    description text,
    level varchar(40),
    status varchar(20) not null check (status in ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table course_modules (
    id uuid primary key,
    course_id uuid not null references courses(id),
    title varchar(160) not null,
    description text,
    position integer not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uk_course_modules_course_position unique (course_id, position)
);

create table lessons (
    id uuid primary key,
    module_id uuid not null references course_modules(id),
    title varchar(160) not null,
    description text,
    content text,
    video_url varchar(255),
    position integer not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uk_lessons_module_position unique (module_id, position)
);

create table course_enrollments (
    id uuid primary key,
    student_id uuid not null references users(id),
    course_id uuid not null references courses(id),
    status varchar(20) not null check (status in ('ACTIVE', 'COMPLETED', 'CANCELLED')),
    enrolled_at timestamptz not null,
    completed_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uk_course_enrollments_student_course unique (student_id, course_id)
);

create table lesson_progress (
    id uuid primary key,
    student_id uuid not null references users(id),
    lesson_id uuid not null references lessons(id),
    completed boolean not null,
    completed_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uk_lesson_progress_student_lesson unique (student_id, lesson_id)
);

create table quizzes (
    id uuid primary key,
    lesson_id uuid not null unique references lessons(id),
    title varchar(160) not null,
    passing_score integer not null default 70 check (passing_score between 0 and 100),
    max_attempts integer not null default 3 check (max_attempts = 3),
    max_questions integer not null default 10 check (max_questions = 10),
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table quiz_questions (
    id uuid primary key,
    quiz_id uuid not null references quizzes(id),
    statement text not null,
    position integer not null check (position between 1 and 10),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uk_quiz_questions_quiz_position unique (quiz_id, position)
);

create table quiz_options (
    id uuid primary key,
    question_id uuid not null references quiz_questions(id),
    text text not null,
    is_correct boolean not null,
    position integer not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uk_quiz_options_question_position unique (question_id, position)
);

create table quiz_attempts (
    id uuid primary key,
    quiz_id uuid not null references quizzes(id),
    student_id uuid not null references users(id),
    attempt_number integer not null check (attempt_number between 1 and 3),
    score integer not null check (score between 0 and 100),
    passed boolean not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uk_quiz_attempts_student_quiz_number unique (student_id, quiz_id, attempt_number)
);

create table quiz_answers (
    id uuid primary key,
    attempt_id uuid not null references quiz_attempts(id),
    question_id uuid not null references quiz_questions(id),
    selected_option_id uuid not null references quiz_options(id),
    correct boolean not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uk_quiz_answers_attempt_question unique (attempt_id, question_id)
);

create table lesson_comments (
    id uuid primary key,
    lesson_id uuid not null references lessons(id),
    student_id uuid not null references users(id),
    content text not null,
    rating integer not null check (rating between 1 and 10),
    status varchar(30) not null check (status in ('VISIBLE', 'HIDDEN_BY_FILTER', 'REMOVED_BY_TEACHER', 'REMOVED_BY_ADMIN')),
    moderation_reason varchar(255),
    removed_by uuid references users(id),
    removed_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table projects (
    id uuid primary key,
    owner_id uuid not null references users(id),
    title varchar(160) not null,
    description text,
    status varchar(20) not null check (status in ('PLANNING', 'IN_PROGRESS', 'PAUSED', 'COMPLETED')),
    visibility varchar(20) not null check (visibility in ('PUBLIC', 'PRIVATE')),
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table project_updates (
    id uuid primary key,
    project_id uuid not null references projects(id),
    title varchar(160) not null,
    content text not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);
