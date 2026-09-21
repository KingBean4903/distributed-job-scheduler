CREATE UNIQUE INDEX uq_job_execution_scheduled
	ON job_executions(job_id, scheduled_at);