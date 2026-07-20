alter table courses
    add column archived boolean not null default false;

update courses
set archived = true
where status = 'ARCHIVED';

alter table lesson_comments
    add column hidden boolean not null default false;
