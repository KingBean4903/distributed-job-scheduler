CREATE TABLE job_executions(
	id UUID PRIMARY KEY,
	job_id UUID NOT NULL,
	status VARCHAR(50) NOT NULL,
	attempt INTEGER NOT NULL,
	scheduled_at TIMESTAMP  WITH TIME ZONE NOT NULL,
	started_at TIMESTAMP NOT NULL,
	completed_at TIMESTAMP  WITH TIME ZONE,
	worker_id VARCHAR(255),
	lease_expires_at TIMESTAMP WITH TIME ZONE,
	error TEXT,
	created_at TIMESTAMP WITH TIME ZONE NOT NULL,
	
	CONSTRAINT fk_job_executions_job
		FOREIGN KEY (job_id)
		REFERENCES jobs(id),
		
	CONSTRAINT chk_execution_attempt
		CHECK (attempt >= 0)
);