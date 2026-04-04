-- ============================================================
-- V13: Update vessel IMOs and names to real vessels for AIS tracking
-- Santos → Buenos Aires : LOG IN ENDURANCE  (IMO 9571296)
-- Santos → Rotterdam    : MAERSK LETICIA    (IMO 9526916)
-- Singapore → Santos    : CMA CGM COBALT    (IMO 9996680)
-- ============================================================

-- Santos → Buenos Aires: CAP SAN MARCO → LOG IN ENDURANCE
UPDATE vessels
SET imo = '9571296', name = 'LOG IN ENDURANCE', flag = 'LR', updated_at = NOW()
WHERE id = 'a0000002-0000-0000-0000-000000000001';

-- Santos → Rotterdam: MONTE TAMARO → MAERSK LETICIA
UPDATE vessels
SET imo = '9526916', name = 'MAERSK LETICIA', flag = 'DK', updated_at = NOW()
WHERE id = 'a0000002-0000-0000-0000-000000000003';

-- Singapore → Santos: MAERSK EDMONTON → CMA CGM COBALT
UPDATE vessels
SET imo = '9996680', name = 'CMA CGM COBALT', flag = 'MT', updated_at = NOW()
WHERE id = 'a0000001-0000-0000-0000-000000000004';

-- Update voyage numbers to match real operators
UPDATE voyages SET voyage_number = 'LOG-2026-015', updated_at = NOW()
WHERE id = 'b0000001-0000-0000-0000-000000000003';

UPDATE voyages SET voyage_number = 'MSK-2026-001', updated_at = NOW()
WHERE id = 'b0000002-0000-0000-0000-000000000003';

UPDATE voyages SET voyage_number = 'CMB-2026-088', updated_at = NOW()
WHERE id = 'b0000001-0000-0000-0000-000000000004';

-- Update event descriptions that referenced old vessel names
UPDATE events
SET description = REPLACE(description, 'CAP SAN MARCO', 'LOG IN ENDURANCE')
WHERE description LIKE '%CAP SAN MARCO%';

UPDATE events
SET description = REPLACE(description, 'MONTE TAMARO', 'MAERSK LETICIA')
WHERE description LIKE '%MONTE TAMARO%';

UPDATE events
SET description = REPLACE(description, 'Maersk Edmonton', 'CMA CGM COBALT')
WHERE description LIKE '%Maersk Edmonton%';
