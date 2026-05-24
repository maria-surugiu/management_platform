CREATE TABLE projects (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          name VARCHAR(255) NOT NULL,
                          description TEXT,
                          status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
                          is_active BOOLEAN NOT NULL DEFAULT TRUE,
                          owner_id UUID NOT NULL,
                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT fk_project_owner FOREIGN KEY (owner_id) REFERENCES users (id)
);

CREATE TABLE project_members (
                                 project_id UUID NOT NULL,
                                 user_id UUID NOT NULL,

                                 PRIMARY KEY (project_id, user_id),
                                 CONSTRAINT fk_pm_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
                                 CONSTRAINT fk_pm_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);