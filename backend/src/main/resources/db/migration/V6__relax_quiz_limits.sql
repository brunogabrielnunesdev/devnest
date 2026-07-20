alter table quizzes drop constraint if exists quizzes_passing_score_check;
alter table quizzes drop constraint if exists quizzes_max_attempts_check;
alter table quizzes drop constraint if exists quizzes_max_questions_check;

alter table quizzes
    add constraint ck_quizzes_passing_score_range check (passing_score between 0 and 100),
    add constraint ck_quizzes_max_attempts_range check (max_attempts between 1 and 10),
    add constraint ck_quizzes_max_questions_range check (max_questions between 1 and 50);
