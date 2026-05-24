INSERT INTO tasks (id, title, description, priority, status, deadline, project_id, assignee_id, created_by_id) VALUES
-- Task 1: Project Alpha, Created by Alice, Assigned to Bob (HIGH, TODO)
('bbbbbbbb-1111-1111-1111-bbbbbbbbbbbb', 'Configurare Flyway', 'Create database migration scripts', 'HIGH', 'TODO', '2026-06-01', 'aaaaaaaa-1111-aaaa-aaaa-aaaaaaaaaaaa', '22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111'),

-- Task 2: Project Alpha, Created by Alice, Unassigned (MEDIUM, IN_PROGRESS)
('bbbbbbbb-2222-2222-2222-bbbbbbbbbbbb', 'Implementare Filtrare', 'Add JpaSpecificationExecutor to Task', 'MEDIUM', 'IN_PROGRESS', '2026-06-15', 'aaaaaaaa-1111-aaaa-aaaa-aaaaaaaaaaaa', NULL, '11111111-1111-1111-1111-111111111111'),

-- Task 3: Project Beta, Created by Bob, Assigned to Charlie (LOW, DONE)
('bbbbbbbb-3333-3333-3333-bbbbbbbbbbbb', 'Curatare cod controller', 'Remove redundant URL mappings', 'LOW', 'DONE', '2026-05-20', 'aaaaaaaa-2222-aaaa-aaaa-aaaaaaaaaaaa', '33333333-3333-3333-3333-333333333333', '22222222-2222-2222-2222-222222222222'),

-- Task 4: Project Gamma, Created by Charlie, Assigned to Diana (HIGH, TODO)
('bbbbbbbb-4444-4444-4444-bbbbbbbbbbbb', 'Scanare vulnerabilitati', 'Run OWASP ZAP and analyze logs', 'HIGH', 'TODO', '2026-07-01', 'aaaaaaaa-3333-aaaa-aaaa-aaaaaaaaaaaa', '44444444-4444-4444-4444-444444444444', '33333333-3333-3333-3333-333333333333'),

-- Task 5: Project Delta, Created by Alice, Assigned to Evan (LOW, IN_PROGRESS)
('bbbbbbbb-5555-5555-5555-bbbbbbbbbbbb', 'Setup VPC AWS', 'Configure public and private subnets', 'LOW', 'IN_PROGRESS', '2026-08-10', 'aaaaaaaa-4444-aaaa-aaaa-aaaaaaaaaaaa', '55555555-5555-5555-5555-555555555555', '11111111-1111-1111-1111-111111111111');
