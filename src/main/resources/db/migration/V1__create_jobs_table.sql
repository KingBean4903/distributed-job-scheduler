CREATE TABLE jobs(
	Id UUID PRIMARY KEY,
	
	name VARCHAR(255) NOT NULL,
	
	type VARCHAR(50) NOT NULL,
	
	schedule TEXT NOT NULL,
	
	payload TEXT,
	
	status VARCHAR(50) NOT NULL,
			
	next_run_at TIMESTAMP WITH TIME ZONE,
	
	created_at TIMESTAMP WITH TIME ZONE NOT NULL,
	
	updated_at TIMESTAMP  WITH TIME ZONE NOT NULL,
	
	priority INTEGER NOT NULL,
		CHECK (priority >= 0),
		
	max_retries INTEGER NOT NULL
		CHECK (max_retries >= 0),
		
	timeout_seconds INTEGER NOT NULL
	 	CHECK (timeout_seconds > 0)
);