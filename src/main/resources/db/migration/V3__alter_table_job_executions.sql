ALTER TABLE job_executions
DROP COLUMN started_at;

ALTER TABLE job_executions
ADD COLUMN started_at TIMESTAMP;