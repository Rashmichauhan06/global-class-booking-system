-- ============================================================
-- V2: Seed Data — Sample teachers, parents, courses
-- ============================================================

INSERT INTO teachers (name, email, timezone) VALUES
    ('Alice Johnson',  'alice@example.com',  'America/New_York'),
    ('Bob Smith',      'bob@example.com',    'Europe/London'),
    ('Carlos Mendes',  'carlos@example.com', 'America/Sao_Paulo');

INSERT INTO parents (name, email, timezone) VALUES
    ('Priya Sharma',   'priya@example.com',  'Asia/Kolkata'),
    ('James Brown',    'james@example.com',  'America/Los_Angeles'),
    ('Fatima Al-Hassan','fatima@example.com','Asia/Dubai');

INSERT INTO courses (title, description) VALUES
    ('Python Coding',     'Learn Python programming from basics to advanced'),
    ('Art Drawing Class', 'Creative drawing and painting techniques'),
    ('Public Speaking',   'Build confidence and communication skills'),
    ('Minecraft Coding',  'Learn game development with Minecraft'),
    ('Roblox Game Design','Build your own Roblox experiences');
