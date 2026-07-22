-- Catálogo global de ejercicios predefinidos (created_by_user_id = NULL).
-- UUIDs fijos y deterministas para que sean estables entre entornos/tests.
INSERT INTO exercises (id, name, target_muscle, category, created_by_user_id) VALUES
    ('00000000-0000-0000-0000-000000000001', 'Press banca',              'Pecho',    'Empuje',        NULL),
    ('00000000-0000-0000-0000-000000000002', 'Sentadilla',               'Piernas',  'Empuje',        NULL),
    ('00000000-0000-0000-0000-000000000003', 'Peso muerto',              'Espalda',  'Traccion',      NULL),
    ('00000000-0000-0000-0000-000000000004', 'Press militar',            'Hombros',  'Empuje',        NULL),
    ('00000000-0000-0000-0000-000000000005', 'Dominadas',                'Espalda',  'Traccion',      NULL),
    ('00000000-0000-0000-0000-000000000006', 'Remo con barra',           'Espalda',  'Traccion',      NULL),
    ('00000000-0000-0000-0000-000000000007', 'Curl de biceps',           'Brazos',   'Aislamiento',   NULL),
    ('00000000-0000-0000-0000-000000000008', 'Extension de triceps',     'Brazos',   'Aislamiento',   NULL),
    ('00000000-0000-0000-0000-000000000009', 'Zancadas',                 'Piernas',  'Empuje',        NULL),
    ('00000000-0000-0000-0000-000000000010', 'Plancha',                  'Core',     'Isometrico',    NULL);
