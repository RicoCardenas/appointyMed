INSERT INTO roles (name, description) VALUES
    ('DEVELOPER', 'Developer with restricted user-management access'),
    ('CLIENT', 'Client with appointment access')
ON CONFLICT (name) DO NOTHING;
