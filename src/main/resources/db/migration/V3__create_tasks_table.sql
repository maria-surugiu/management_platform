CREATE TABLE tasks (
                       id UUID PRIMARY KEY,
                       title VARCHAR(255) NOT NULL,
                       description TEXT,
                       priority VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
                       status VARCHAR(50) NOT NULL DEFAULT 'TODO',
                       deadline DATE,
                       project_id UUID NOT NULL,
                       assignee_id UUID,
                       created_by_id UUID NOT NULL,
                       created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT fk_tasks_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
                       CONSTRAINT fk_tasks_assignee FOREIGN KEY (assignee_id) REFERENCES users(id) ON DELETE SET NULL,
                       CONSTRAINT fk_tasks_created_by FOREIGN KEY (created_by_id) REFERENCES users(id)
);