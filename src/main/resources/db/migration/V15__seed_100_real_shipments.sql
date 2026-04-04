-- ============================================================
-- V15: Seed 100 real maritime shipments
-- Ports, Vessels, Voyages, Shipments, Events
-- ============================================================

-- ==================== PORTS (new) ====================
INSERT INTO ports (id, unlocode, name, country, timezone, latitude, longitude, created_at) VALUES
  (gen_random_uuid(), 'LKCMB', 'Colombo',          'LK', 'Asia/Colombo',           6.9271,   79.8612,  NOW()),
  (gen_random_uuid(), 'ZADUR', 'Durban',            'ZA', 'Africa/Johannesburg',   -29.8587,  31.0218,  NOW()),
  (gen_random_uuid(), 'USLAX', 'Los Angeles',       'US', 'America/Los_Angeles',   33.7405, -118.2774,  NOW()),
  (gen_random_uuid(), 'INHZX', 'Hazira',            'IN', 'Asia/Kolkata',          21.0888,   72.6385,  NOW()),
  (gen_random_uuid(), 'MXLZC', 'Lazaro Cardenas',  'MX', 'America/Mazatlan',      17.9419, -102.1656,  NOW()),
  (gen_random_uuid(), 'KRPUS', 'Busan',             'KR', 'Asia/Seoul',            35.1046,  129.0351,  NOW()),
  (gen_random_uuid(), 'CNSHA', 'Shanghai',          'CN', 'Asia/Shanghai',         31.2304,  121.4737,  NOW()),
  (gen_random_uuid(), 'USNYC', 'New York',          'US', 'America/New_York',      40.6840,  -74.0440,  NOW()),
  (gen_random_uuid(), 'FOSFM', 'Fos-sur-Mer',      'FR', 'Europe/Paris',          43.4384,   4.9481,   NOW()),
  (gen_random_uuid(), 'CNYTN', 'Yantian',           'CN', 'Asia/Shanghai',         22.5743,  114.2617,  NOW()),
  (gen_random_uuid(), 'PABLB', 'Balboa',            'PA', 'America/Panama',         8.9500,  -79.5700,  NOW()),
  (gen_random_uuid(), 'MAPTM', 'Tanger Med',        'MA', 'Africa/Casablanca',     35.8833,  -5.5000,   NOW()),
  (gen_random_uuid(), 'USORF', 'Norfolk',           'US', 'America/New_York',      36.9468,  -76.2951,  NOW()),
  (gen_random_uuid(), 'UYMVD', 'Montevideo',        'UY', 'America/Montevideo',   -34.9011,  -56.1645,  NOW()),
  (gen_random_uuid(), 'BEANR', 'Antwerp',           'BE', 'Europe/Brussels',       51.2330,   4.4025,   NOW()),
  (gen_random_uuid(), 'DEHAM', 'Hamburg',           'DE', 'Europe/Berlin',         53.5511,   9.9937,   NOW()),
  (gen_random_uuid(), 'ESALG', 'Algeciras',         'ES', 'Europe/Madrid',         36.1231,  -5.4531,   NOW()),
  (gen_random_uuid(), 'JPUKB', 'Kobe',              'JP', 'Asia/Tokyo',            34.6939,  135.1956,  NOW()),
  (gen_random_uuid(), 'JPYOK', 'Yokohama',          'JP', 'Asia/Tokyo',            35.4438,  139.6380,  NOW()),
  (gen_random_uuid(), 'LBBEY', 'Beirut',            'LB', 'Asia/Beirut',           33.8886,   35.4955,  NOW()),
  (gen_random_uuid(), 'USMIA', 'Miami',             'US', 'America/New_York',      25.7617,  -80.1918,  NOW()),
  (gen_random_uuid(), 'BRRIG', 'Rio Grande',        'BR', 'America/Sao_Paulo',    -32.0350,  -52.0990,  NOW()),
  (gen_random_uuid(), 'BRPNG', 'Paranagua',         'BR', 'America/Sao_Paulo',    -25.5190,  -48.5120,  NOW())
ON CONFLICT (unlocode) DO NOTHING;

-- ==================== VESSELS (28 real) ====================
INSERT INTO vessels (id, imo, name, flag, type, capacity_teu, created_at, updated_at) VALUES
  (gen_random_uuid(), '9996680', 'CMA CGM COBALT',       'FR', 'CONTAINER', 13102, NOW(), NOW()),
  (gen_random_uuid(), '9882499', 'CMA CGM HERMES',       'FR', 'CONTAINER',  5622, NOW(), NOW()),
  (gen_random_uuid(), '9951525', 'CMA CGM INNOVATION',   'FR', 'CONTAINER', 23112, NOW(), NOW()),
  (gen_random_uuid(), '9679907', 'CMA CGM MISSISSIPPI',  'FR', 'CONTAINER', 16020, NOW(), NOW()),
  (gen_random_uuid(), '9351141', 'CMA CGM NEW JERSEY',   'FR', 'CONTAINER',  6788, NOW(), NOW()),
  (gen_random_uuid(), '9637246', 'HMM DRIVE',            'KR', 'CONTAINER', 13154, NOW(), NOW()),
  (gen_random_uuid(), '9385013', 'HMM OAKLAND',          'KR', 'CONTAINER',  4578, NOW(), NOW()),
  (gen_random_uuid(), '9625530', 'HMM PREMIUM',          'KR', 'CONTAINER',  8586, NOW(), NOW()),
  (gen_random_uuid(), '9868364', 'HMM ST PETERSBURG',    'KR', 'CONTAINER', 16000, NOW(), NOW()),
  (gen_random_uuid(), '9385001', 'HMM TACOMA',           'KR', 'CONTAINER',  4578, NOW(), NOW()),
  (gen_random_uuid(), '9571296', 'LOG IN ENDURANCE',     'BR', 'CONTAINER',  3800, NOW(), NOW()),
  (gen_random_uuid(), '9348649', 'MAERSK ATLANTA',       'DK', 'CONTAINER',  7400, NOW(), NOW()),
  (gen_random_uuid(), '9332975', 'MAERSK CHICAGO',       'DK', 'CONTAINER',  6600, NOW(), NOW()),
  (gen_random_uuid(), '9332987', 'MAERSK COLUMBUS',      'DK', 'CONTAINER',  6600, NOW(), NOW()),
  (gen_random_uuid(), '9333008', 'MAERSK HARTFORD',      'DK', 'CONTAINER',  6600, NOW(), NOW()),
  (gen_random_uuid(), '9526916', 'MAERSK LETICIA',       'DK', 'CONTAINER',  8450, NOW(), NOW()),
  (gen_random_uuid(), '9298698', 'MAERSK OHIO',          'DK', 'CONTAINER',  6600, NOW(), NOW()),
  (gen_random_uuid(), '9342176', 'MAERSK PITTSBURGH',    'DK', 'CONTAINER',  6600, NOW(), NOW()),
  (gen_random_uuid(), '9963580', 'MSC AMERICA',          'CH', 'CONTAINER', 24116, NOW(), NOW()),
  (gen_random_uuid(), '9282261', 'MSC ANS',              'CH', 'CONTAINER',  3534, NOW(), NOW()),
  (gen_random_uuid(), '9327798', 'MSC LOS ANGELES',      'CH', 'CONTAINER',  9200, NOW(), NOW()),
  (gen_random_uuid(), '9196864', 'MSC MICHIGAN VII',     'CH', 'CONTAINER',  3534, NOW(), NOW()),
  (gen_random_uuid(), '9606314', 'MSC NEW YORK',         'CH', 'CONTAINER', 14000, NOW(), NOW()),
  (gen_random_uuid(), '9937323', 'ONE AMAZON',           'JP', 'CONTAINER', 14000, NOW(), NOW()),
  (gen_random_uuid(), '9588079', 'ONE HONOLULU',         'JP', 'CONTAINER', 13208, NOW(), NOW()),
  (gen_random_uuid(), '9566382', 'ONE HOUSTON',          'JP', 'CONTAINER', 13208, NOW(), NOW()),
  (gen_random_uuid(), '9475636', 'ONE MODERN',           'JP', 'CONTAINER',  8000, NOW(), NOW()),
  (gen_random_uuid(), '9622203', 'SAN NICOLAS MAERSK',   'DK', 'CONTAINER',  3596, NOW(), NOW())
ON CONFLICT (imo) DO NOTHING;


-- ==================== VOYAGES (98 unique) ====================
INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '126E',
    (SELECT id FROM vessels WHERE imo = '9332975'),
    (SELECT id FROM ports WHERE unlocode = 'NLRTM'),
    (SELECT id FROM ports WHERE unlocode = 'BRRIG'),
    '2026-03-24T09:00Z', '2026-03-25T02:00Z', '2026-04-12T12:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '126E');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '133E',
    (SELECT id FROM vessels WHERE imo = '9475636'),
    (SELECT id FROM ports WHERE unlocode = 'PABLB'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-03-24T04:00Z', '2026-03-24T17:00Z', '2026-04-09T12:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '133E');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '133W',
    (SELECT id FROM vessels WHERE imo = '9327798'),
    (SELECT id FROM ports WHERE unlocode = 'CNSHA'),
    (SELECT id FROM ports WHERE unlocode = 'UYMVD'),
    '2026-04-16T11:00Z', '2026-05-23T11:00Z', 'SCHEDULED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '133W');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '138N',
    (SELECT id FROM vessels WHERE imo = '9475636'),
    (SELECT id FROM ports WHERE unlocode = 'PABLB'),
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    '2026-03-21T06:00Z', '2026-03-21T19:00Z', '2026-04-10T12:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '138N');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '158E',
    (SELECT id FROM vessels WHERE imo = '9566382'),
    (SELECT id FROM ports WHERE unlocode = 'JPYOK'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-03-10T09:00Z', '2026-03-11T01:00Z', '2026-04-16T12:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '158E');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '169S',
    (SELECT id FROM vessels WHERE imo = '9342176'),
    (SELECT id FROM ports WHERE unlocode = 'USNYC'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    '2026-03-30T00:00Z', '2026-03-30T17:00Z', '2026-04-03T12:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '169S');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '172S',
    (SELECT id FROM vessels WHERE imo = '9679907'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'),
    '2026-04-14T05:00Z', '2026-05-14T05:00Z', 'SCHEDULED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '172S');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '174N',
    (SELECT id FROM vessels WHERE imo = '9333008'),
    (SELECT id FROM ports WHERE unlocode = 'ZADUR'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-03-27T03:00Z', '2026-03-27T18:00Z', '2026-04-12T18:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '174N');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '180W',
    (SELECT id FROM vessels WHERE imo = '9963580'),
    (SELECT id FROM ports WHERE unlocode = 'KRPUS'),
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    '2026-04-06T00:00Z', '2026-05-11T00:00Z', 'SCHEDULED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '180W');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '182S',
    (SELECT id FROM vessels WHERE imo = '9351141'),
    (SELECT id FROM ports WHERE unlocode = 'USLAX'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-02-28T12:00Z', '2026-02-28T16:00Z', '2026-03-22T16:00Z', 'ARRIVED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '182S');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '183W',
    (SELECT id FROM vessels WHERE imo = '9196864'),
    (SELECT id FROM ports WHERE unlocode = 'ZADUR'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    '2026-04-05T02:00Z', '2026-04-15T02:00Z', 'SCHEDULED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '183W');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '184N',
    (SELECT id FROM vessels WHERE imo = '9868364'),
    (SELECT id FROM ports WHERE unlocode = 'BEANR'),
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    '2026-04-15T13:00Z', '2026-05-05T13:00Z', 'SCHEDULED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '184N');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '200N',
    (SELECT id FROM vessels WHERE imo = '9951525'),
    (SELECT id FROM ports WHERE unlocode = 'ESALG'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    '2026-04-01T06:00Z', '2026-04-01T20:00Z', '2026-04-16T20:00Z', 'DEPARTED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '200N');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '201E',
    (SELECT id FROM vessels WHERE imo = '9882499'),
    (SELECT id FROM ports WHERE unlocode = 'MAPTM'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    '2026-03-31T06:00Z', '2026-03-31T08:00Z', '2026-04-16T08:00Z', 'DEPARTED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '201E');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '202S',
    (SELECT id FROM vessels WHERE imo = '9332987'),
    (SELECT id FROM ports WHERE unlocode = 'USORF'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-03-31T09:00Z', '2026-03-31T14:00Z', '2026-04-15T14:00Z', 'DEPARTED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '202S');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '207N',
    (SELECT id FROM vessels WHERE imo = '9637246'),
    (SELECT id FROM ports WHERE unlocode = 'KRPUS'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-02-16T12:00Z', '2026-02-16T21:00Z', '2026-03-22T21:00Z', 'ARRIVED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '207N');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '232N',
    (SELECT id FROM vessels WHERE imo = '9348649'),
    (SELECT id FROM ports WHERE unlocode = 'USMIA'),
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    '2026-04-10T05:00Z', '2026-04-25T05:00Z', 'SCHEDULED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '232N');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '233W',
    (SELECT id FROM vessels WHERE imo = '9298698'),
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-04-13T01:00Z', '2026-05-12T01:00Z', 'SCHEDULED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '233W');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '239N',
    (SELECT id FROM vessels WHERE imo = '9963580'),
    (SELECT id FROM ports WHERE unlocode = 'CNSHA'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-03-27T05:00Z', '2026-03-27T11:00Z', '2026-04-30T12:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '239N');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '240S',
    (SELECT id FROM vessels WHERE imo = '9342176'),
    (SELECT id FROM ports WHERE unlocode = 'USNYC'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    '2026-03-11T12:00Z', '2026-03-12T06:00Z', '2026-03-27T06:00Z', 'ARRIVED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '240S');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '244S',
    (SELECT id FROM vessels WHERE imo = '9951525'),
    (SELECT id FROM ports WHERE unlocode = 'ESALG'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    '2026-03-27T09:00Z', '2026-03-27T22:00Z', '2026-04-14T12:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '244S');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '256W',
    (SELECT id FROM vessels WHERE imo = '9637246'),
    (SELECT id FROM ports WHERE unlocode = 'KRPUS'),
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    '2026-03-11T09:00Z', '2026-03-11T23:00Z', '2026-04-20T12:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '256W');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '276E',
    (SELECT id FROM vessels WHERE imo = '9868364'),
    (SELECT id FROM ports WHERE unlocode = 'DEHAM'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-04-03T07:00Z', '2026-04-20T07:00Z', 'DEPARTED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '276E');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '285W',
    (SELECT id FROM vessels WHERE imo = '9298698'),
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    '2026-04-11T11:00Z', '2026-05-11T11:00Z', 'SCHEDULED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '285W');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '286N',
    (SELECT id FROM vessels WHERE imo = '9951525'),
    (SELECT id FROM ports WHERE unlocode = 'FOSFM'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-03-19T01:00Z', '2026-03-19T15:00Z', '2026-04-10T15:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '286N');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '299W',
    (SELECT id FROM vessels WHERE imo = '9332987'),
    (SELECT id FROM ports WHERE unlocode = 'USNYC'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    '2026-03-13T12:00Z', '2026-03-13T15:00Z', '2026-03-29T15:00Z', 'ARRIVED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '299W');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '319S',
    (SELECT id FROM vessels WHERE imo = '9351141'),
    (SELECT id FROM ports WHERE unlocode = 'USLAX'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-04-06T11:00Z', '2026-04-27T11:00Z', 'SCHEDULED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '319S');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '328N',
    (SELECT id FROM vessels WHERE imo = '9282261'),
    (SELECT id FROM ports WHERE unlocode = 'CNSHA'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-03-05T01:00Z', '2026-03-05T10:00Z', '2026-04-08T12:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '328N');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '337N',
    (SELECT id FROM vessels WHERE imo = '9571296'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    (SELECT id FROM ports WHERE unlocode = 'UYMVD'),
    '2026-04-15T06:00Z', '2026-04-20T06:00Z', 'SCHEDULED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '337N');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '349W',
    (SELECT id FROM vessels WHERE imo = '9951525'),
    (SELECT id FROM ports WHERE unlocode = 'FOSFM'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-04-10T01:00Z', '2026-04-24T01:00Z', 'SCHEDULED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '349W');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '355N',
    (SELECT id FROM vessels WHERE imo = '9622203'),
    (SELECT id FROM ports WHERE unlocode = 'CNSHA'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    '2026-02-16T12:00Z', '2026-02-16T15:00Z', '2026-03-22T15:00Z', 'ARRIVED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '355N');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '358W',
    (SELECT id FROM vessels WHERE imo = '9348649'),
    (SELECT id FROM ports WHERE unlocode = 'USMIA'),
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    '2026-04-05T10:00Z', '2026-04-19T10:00Z', 'SCHEDULED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '358W');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '371N',
    (SELECT id FROM vessels WHERE imo = '9282261'),
    (SELECT id FROM ports WHERE unlocode = 'KRPUS'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    '2026-03-30T07:00Z', '2026-03-30T14:00Z', '2026-05-02T14:00Z', 'DEPARTED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '371N');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '386W',
    (SELECT id FROM vessels WHERE imo = '9333008'),
    (SELECT id FROM ports WHERE unlocode = 'ZADUR'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-03-23T02:00Z', '2026-03-23T04:00Z', '2026-04-06T12:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '386W');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '390E',
    (SELECT id FROM vessels WHERE imo = '9385013'),
    (SELECT id FROM ports WHERE unlocode = 'LKCMB'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-02-27T12:00Z', '2026-02-28T00:00Z', '2026-03-24T00:00Z', 'ARRIVED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '390E');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '396E',
    (SELECT id FROM vessels WHERE imo = '9882499'),
    (SELECT id FROM ports WHERE unlocode = 'MAPTM'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    '2026-03-14T12:00Z', '2026-03-14T14:00Z', '2026-03-28T14:00Z', 'ARRIVED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '396E');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '399N',
    (SELECT id FROM vessels WHERE imo = '9622203'),
    (SELECT id FROM ports WHERE unlocode = 'CNSHA'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    '2026-03-31T03:00Z', '2026-03-31T21:00Z', '2026-05-06T21:00Z', 'DEPARTED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '399N');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '402N',
    (SELECT id FROM vessels WHERE imo = '9937323'),
    (SELECT id FROM ports WHERE unlocode = 'CNYTN'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-04-05T20:00Z', '2026-05-09T20:00Z', 'SCHEDULED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '402N');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '406W',
    (SELECT id FROM vessels WHERE imo = '9348649'),
    (SELECT id FROM ports WHERE unlocode = 'USMIA'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-03-30T09:00Z', '2026-03-30T21:00Z', '2026-04-11T21:00Z', 'DEPARTED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '406W');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '431E',
    (SELECT id FROM vessels WHERE imo = '9882499'),
    (SELECT id FROM ports WHERE unlocode = 'LBBEY'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-04-13T17:00Z', '2026-05-01T17:00Z', 'SCHEDULED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '431E');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '451S',
    (SELECT id FROM vessels WHERE imo = '9298698'),
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-03-26T00:00Z', '2026-03-26T02:00Z', '2026-04-26T12:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '451S');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '453E',
    (SELECT id FROM vessels WHERE imo = '9298698'),
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-04-01T10:00Z', '2026-04-01T20:00Z', '2026-05-02T20:00Z', 'DEPARTED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '453E');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '461W',
    (SELECT id FROM vessels WHERE imo = '9385013'),
    (SELECT id FROM ports WHERE unlocode = 'LKCMB'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-04-07T10:00Z', '2026-04-30T10:00Z', 'SCHEDULED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '461W');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '468W',
    (SELECT id FROM vessels WHERE imo = '9333008'),
    (SELECT id FROM ports WHERE unlocode = 'ZADUR'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-03-23T08:00Z', '2026-03-23T16:00Z', '2026-04-06T12:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '468W');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '469N',
    (SELECT id FROM vessels WHERE imo = '9526916'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    (SELECT id FROM ports WHERE unlocode = 'BEANR'),
    '2026-03-23T00:00Z', '2026-03-23T06:00Z', '2026-04-11T12:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '469N');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '474N',
    (SELECT id FROM vessels WHERE imo = '9282261'),
    (SELECT id FROM ports WHERE unlocode = 'KRPUS'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    '2026-03-31T02:00Z', '2026-03-31T19:00Z', '2026-05-05T19:00Z', 'DEPARTED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '474N');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '487N',
    (SELECT id FROM vessels WHERE imo = '9385013'),
    (SELECT id FROM ports WHERE unlocode = 'LKCMB'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-04-09T01:00Z', '2026-05-02T01:00Z', 'SCHEDULED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '487N');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '487W',
    (SELECT id FROM vessels WHERE imo = '9588079'),
    (SELECT id FROM ports WHERE unlocode = 'CNYTN'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    '2026-03-23T05:00Z', '2026-03-23T11:00Z', '2026-04-28T12:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '487W');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '500N',
    (SELECT id FROM vessels WHERE imo = '9327798'),
    (SELECT id FROM ports WHERE unlocode = 'CNSHA'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-04-17T20:00Z', '2026-05-21T20:00Z', 'SCHEDULED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '500N');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '512E',
    (SELECT id FROM vessels WHERE imo = '9637246'),
    (SELECT id FROM ports WHERE unlocode = 'KRPUS'),
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    '2026-04-19T15:00Z', '2026-05-26T15:00Z', 'SCHEDULED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '512E');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '524E',
    (SELECT id FROM vessels WHERE imo = '9282261'),
    (SELECT id FROM ports WHERE unlocode = 'KRPUS'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    '2026-04-14T19:00Z', '2026-05-18T19:00Z', 'SCHEDULED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '524E');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '533W',
    (SELECT id FROM vessels WHERE imo = '9625530'),
    (SELECT id FROM ports WHERE unlocode = 'MXLZC'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-04-01T01:00Z', '2026-04-01T03:00Z', '2026-04-21T03:00Z', 'DEPARTED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '533W');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '542E',
    (SELECT id FROM vessels WHERE imo = '9588079'),
    (SELECT id FROM ports WHERE unlocode = 'CNSHA'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-04-11T16:00Z', '2026-05-17T16:00Z', 'SCHEDULED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '542E');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '547W',
    (SELECT id FROM vessels WHERE imo = '9566382'),
    (SELECT id FROM ports WHERE unlocode = 'JPYOK'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-03-30T05:00Z', '2026-03-30T22:00Z', '2026-05-07T22:00Z', 'DEPARTED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '547W');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '554E',
    (SELECT id FROM vessels WHERE imo = '9637246'),
    (SELECT id FROM ports WHERE unlocode = 'KRPUS'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-03-28T01:00Z', '2026-03-28T13:00Z', '2026-05-05T12:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '554E');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '557N',
    (SELECT id FROM vessels WHERE imo = '9625530'),
    (SELECT id FROM ports WHERE unlocode = 'MXLZC'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-03-23T04:00Z', '2026-03-23T17:00Z', '2026-04-11T12:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '557N');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '558N',
    (SELECT id FROM vessels WHERE imo = '9571296'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    (SELECT id FROM ports WHERE unlocode = 'UYMVD'),
    '2026-03-29T06:00Z', '2026-03-29T15:00Z', '2026-04-04T12:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '558N');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '562S',
    (SELECT id FROM vessels WHERE imo = '9333008'),
    (SELECT id FROM ports WHERE unlocode = 'MAPTM'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-03-18T06:00Z', '2026-03-18T20:00Z', '2026-04-07T20:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '562S');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '568E',
    (SELECT id FROM vessels WHERE imo = '9625530'),
    (SELECT id FROM ports WHERE unlocode = 'MXLZC'),
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    '2026-04-14T07:00Z', '2026-05-07T07:00Z', 'SCHEDULED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '568E');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '590E',
    (SELECT id FROM vessels WHERE imo = '9333008'),
    (SELECT id FROM ports WHERE unlocode = 'ZADUR'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-03-29T11:00Z', '2026-03-30T01:00Z', '2026-04-04T12:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '590E');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '630E',
    (SELECT id FROM vessels WHERE imo = '9385013'),
    (SELECT id FROM ports WHERE unlocode = 'LKCMB'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-03-12T01:00Z', '2026-03-12T07:00Z', '2026-04-07T12:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '630E');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '639E',
    (SELECT id FROM vessels WHERE imo = '9298698'),
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    '2026-03-30T04:00Z', '2026-03-30T19:00Z', '2026-05-01T19:00Z', 'DEPARTED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '639E');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '649E',
    (SELECT id FROM vessels WHERE imo = '9951525'),
    (SELECT id FROM ports WHERE unlocode = 'FOSFM'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-04-07T06:00Z', '2026-04-24T06:00Z', 'DEPARTED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '649E');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '665N',
    (SELECT id FROM vessels WHERE imo = '9385013'),
    (SELECT id FROM ports WHERE unlocode = 'LKCMB'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-03-16T10:00Z', '2026-03-16T19:00Z', '2026-04-13T12:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '665N');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '665W',
    (SELECT id FROM vessels WHERE imo = '9566382'),
    (SELECT id FROM ports WHERE unlocode = 'JPUKB'),
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    '2026-04-01T06:00Z', '2026-04-01T20:00Z', '2026-05-12T20:00Z', 'DEPARTED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '665W');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '668W',
    (SELECT id FROM vessels WHERE imo = '9963580'),
    (SELECT id FROM ports WHERE unlocode = 'CNSHA'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-03-03T08:00Z', '2026-03-03T23:00Z', '2026-04-08T12:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '668W');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '669N',
    (SELECT id FROM vessels WHERE imo = '9333008'),
    (SELECT id FROM ports WHERE unlocode = 'ZADUR'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-04-07T04:00Z', '2026-04-19T04:00Z', 'SCHEDULED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '669N');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '675N',
    (SELECT id FROM vessels WHERE imo = '9963580'),
    (SELECT id FROM ports WHERE unlocode = 'CNSHA'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-02-21T12:00Z', '2026-02-22T00:00Z', '2026-03-28T00:00Z', 'ARRIVED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '675N');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '687N',
    (SELECT id FROM vessels WHERE imo = '9385013'),
    (SELECT id FROM ports WHERE unlocode = 'LKCMB'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-04-01T07:00Z', '2026-04-01T11:00Z', '2026-04-27T11:00Z', 'DEPARTED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '687N');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '691N',
    (SELECT id FROM vessels WHERE imo = '9571296'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    (SELECT id FROM ports WHERE unlocode = 'UYMVD'),
    '2026-03-30T05:00Z', '2026-03-30T11:00Z', '2026-04-04T12:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '691N');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '696E',
    (SELECT id FROM vessels WHERE imo = '9868364'),
    (SELECT id FROM ports WHERE unlocode = 'NLRTM'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    '2026-03-27T04:00Z', '2026-03-27T09:00Z', '2026-04-14T12:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '696E');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '719S',
    (SELECT id FROM vessels WHERE imo = '9637246'),
    (SELECT id FROM ports WHERE unlocode = 'KRPUS'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-03-20T11:00Z', '2026-03-20T17:00Z', '2026-04-27T12:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '719S');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '740W',
    (SELECT id FROM vessels WHERE imo = '9571296'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    '2026-04-04T23:00Z', '2026-04-09T23:00Z', 'DEPARTED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '740W');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '748S',
    (SELECT id FROM vessels WHERE imo = '9332987'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM ports WHERE unlocode = 'USORF'),
    '2026-04-21T20:00Z', '2026-05-07T20:00Z', 'SCHEDULED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '748S');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '752E',
    (SELECT id FROM vessels WHERE imo = '9282261'),
    (SELECT id FROM ports WHERE unlocode = 'KRPUS'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    '2026-04-21T21:00Z', '2026-05-24T21:00Z', 'SCHEDULED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '752E');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '756S',
    (SELECT id FROM vessels WHERE imo = '9475636'),
    (SELECT id FROM ports WHERE unlocode = 'PABLB'),
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    '2026-03-19T08:00Z', '2026-03-19T21:00Z', '2026-04-08T12:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '756S');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '759S',
    (SELECT id FROM vessels WHERE imo = '9625530'),
    (SELECT id FROM ports WHERE unlocode = 'MXLZC'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-04-01T11:00Z', '2026-04-01T13:00Z', '2026-04-18T13:00Z', 'DEPARTED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '759S');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '760W',
    (SELECT id FROM vessels WHERE imo = '9637246'),
    (SELECT id FROM ports WHERE unlocode = 'KRPUS'),
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    '2026-03-04T03:00Z', '2026-03-04T21:00Z', '2026-04-16T21:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '760W');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '787N',
    (SELECT id FROM vessels WHERE imo = '9606314'),
    (SELECT id FROM ports WHERE unlocode = 'CNSHA'),
    (SELECT id FROM ports WHERE unlocode = 'UYMVD'),
    '2026-04-01T09:00Z', '2026-04-01T18:00Z', '2026-05-08T18:00Z', 'DEPARTED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '787N');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '792E',
    (SELECT id FROM vessels WHERE imo = '9637246'),
    (SELECT id FROM ports WHERE unlocode = 'KRPUS'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-03-27T00:00Z', '2026-03-27T06:00Z', '2026-05-03T12:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '792E');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '793S',
    (SELECT id FROM vessels WHERE imo = '9996680'),
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-04-06T03:00Z', '2026-05-08T03:00Z', 'SCHEDULED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '793S');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '803E',
    (SELECT id FROM vessels WHERE imo = '9332975'),
    (SELECT id FROM ports WHERE unlocode = 'MAPTM'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-03-23T06:00Z', '2026-03-23T17:00Z', '2026-04-11T17:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '803E');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '825S',
    (SELECT id FROM vessels WHERE imo = '9622203'),
    (SELECT id FROM ports WHERE unlocode = 'CNSHA'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    '2026-03-22T00:00Z', '2026-03-22T07:00Z', '2026-04-28T07:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '825S');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '835S',
    (SELECT id FROM vessels WHERE imo = '9868364'),
    (SELECT id FROM ports WHERE unlocode = 'NLRTM'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    '2026-03-04T12:00Z', '2026-03-04T22:00Z', '2026-03-22T22:00Z', 'ARRIVED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '835S');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '844N',
    (SELECT id FROM vessels WHERE imo = '9332987'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM ports WHERE unlocode = 'USORF'),
    '2026-03-23T04:00Z', '2026-03-23T17:00Z', '2026-04-09T12:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '844N');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '862E',
    (SELECT id FROM vessels WHERE imo = '9332975'),
    (SELECT id FROM ports WHERE unlocode = 'NLRTM'),
    (SELECT id FROM ports WHERE unlocode = 'BRRIG'),
    '2026-03-18T07:00Z', '2026-03-18T11:00Z', '2026-04-05T12:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '862E');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '881S',
    (SELECT id FROM vessels WHERE imo = '9526916'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    (SELECT id FROM ports WHERE unlocode = 'BEANR'),
    '2026-03-05T12:00Z', '2026-03-06T04:00Z', '2026-03-24T04:00Z', 'ARRIVED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '881S');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '891S',
    (SELECT id FROM vessels WHERE imo = '9566382'),
    (SELECT id FROM ports WHERE unlocode = 'JPYOK'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-04-20T13:00Z', '2026-05-28T13:00Z', 'SCHEDULED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '891S');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '902E',
    (SELECT id FROM vessels WHERE imo = '9298698'),
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-02-26T12:00Z', '2026-02-27T00:00Z', '2026-03-28T00:00Z', 'ARRIVED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '902E');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '902W',
    (SELECT id FROM vessels WHERE imo = '9385001'),
    (SELECT id FROM ports WHERE unlocode = 'INHZX'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-03-28T08:00Z', '2026-03-28T15:00Z', '2026-04-22T12:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '902W');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '915N',
    (SELECT id FROM vessels WHERE imo = '9637246'),
    (SELECT id FROM ports WHERE unlocode = 'KRPUS'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-03-11T10:00Z', '2026-03-11T12:00Z', '2026-04-15T12:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '915N');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '920N',
    (SELECT id FROM vessels WHERE imo = '9951525'),
    (SELECT id FROM ports WHERE unlocode = 'FOSFM'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-03-13T12:00Z', '2026-03-13T21:00Z', '2026-03-28T21:00Z', 'ARRIVED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '920N');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '925W',
    (SELECT id FROM vessels WHERE imo = '9526916'),
    (SELECT id FROM ports WHERE unlocode = 'DEHAM'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-03-20T00:00Z', '2026-03-20T15:00Z', '2026-04-11T12:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '925W');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '933E',
    (SELECT id FROM vessels WHERE imo = '9868364'),
    (SELECT id FROM ports WHERE unlocode = 'BEANR'),
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    '2026-04-14T23:00Z', '2026-05-05T23:00Z', 'SCHEDULED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '933E');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '972W',
    (SELECT id FROM vessels WHERE imo = '9637246'),
    (SELECT id FROM ports WHERE unlocode = 'KRPUS'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    '2026-03-12T03:00Z', '2026-03-12T21:00Z', '2026-04-17T12:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '972W');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '973W',
    (SELECT id FROM vessels WHERE imo = '9625530'),
    (SELECT id FROM ports WHERE unlocode = 'MXLZC'),
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    '2026-04-09T03:00Z', '2026-05-02T03:00Z', 'SCHEDULED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '973W');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '984W',
    (SELECT id FROM vessels WHERE imo = '9679907'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'),
    '2026-03-30T09:00Z', '2026-03-30T13:00Z', '2026-04-03T12:00Z', 'IN_TRANSIT', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '984W');

INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, atd, eta, status, created_at, updated_at)
SELECT gen_random_uuid(), '999E',
    (SELECT id FROM vessels WHERE imo = '9196864'),
    (SELECT id FROM ports WHERE unlocode = 'ZADUR'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    '2026-04-01T01:00Z', '2026-04-01T12:00Z', '2026-04-12T12:00Z', 'DEPARTED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '999E');

-- ==================== SHIPMENTS (100) ====================
INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'HM39958838', 'HBL013389083', 'MBL863794026', 'REF-2602-0001',
    'CMAU1819604', 'TEU40HC', 40, 'HC',
    9923.0, 8129.0, 39.35, 265, 'bales',
    'Parana Timber Exports', 'Benelux Commodities NV', 'Benelux Commodities NV', 'Marina Rocha',
    'DAP', 'Prepaid', 'Pulp bales', 'India-SA',
    NULL, 'PARTIALLY_RECEIVED', 'NOT_STARTED', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9385013', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '487N'),
    (SELECT id FROM ports WHERE unlocode = 'LKCMB'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'BOOKED',
    '2026-02-02T09:00Z', '2026-02-04T18:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'HM39958838');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MK19335534', 'HBL475255341', 'MBL928327648', 'REF-2603-0002',
    'MSCU3413160', 'TEU40', 40, 'GP',
    23535.0, 22943.0, 29.04, 515, 'cartons',
    'LatAm Pharma Logistics', 'Rioplate Logistics SA', 'Rioplate Logistics SA', 'Marina Rocha',
    'DAP', 'Prepaid', 'Refrigerated fruit', 'NEU-SA',
    NULL, 'PARTIALLY_RECEIVED', 'CLEARED', 'MEDIUM', 0,
    'https://www.vesselfinder.com/vessels/details/9332975', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '862E'),
    (SELECT id FROM ports WHERE unlocode = 'NLRTM'),
    (SELECT id FROM ports WHERE unlocode = 'BRRIG'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-03-13T02:00Z', '2026-03-27T03:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MK19335534');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MS78387461', 'HBL916697848', 'MBL018451462', 'REF-2602-0003',
    'TEMU1012260', 'TEU40', 40, 'GP',
    24510.0, 24171.0, 20.93, 753, 'crates',
    'Santos Trading Export Ltda', 'Southern Cross Forwarding', 'Southern Cross Forwarding', 'Diego Martins',
    'CIF', 'Prepaid', 'Auto parts', 'Africa-SA',
    NULL, 'PARTIALLY_RECEIVED', 'IN_PROGRESS', 'MEDIUM', 0,
    'https://www.vesselfinder.com/vessels/details/9196864', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '999E'),
    (SELECT id FROM ports WHERE unlocode = 'ZADUR'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'LOADED',
    '2026-02-22T20:00Z', '2026-04-01T19:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MS78387461');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'CC81498611', 'HBL896383465', 'MBL787133150', 'REF-2602-0004',
    'TXGU2278241', 'TEU40HC', 45, 'HC',
    14321.0, 12969.0, 27.61, 1004, 'bags',
    'SP Consumer Goods', 'Patagonia Retail SA', 'Patagonia Retail SA', 'Diego Martins',
    'DAP', 'Collect', 'Green coffee beans', 'Americas West Coast',
    (SELECT id FROM ports WHERE unlocode = 'PABLB'), 'PENDING', 'NOT_STARTED', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9351141', 'Via Balboa.',
    (SELECT id FROM voyages WHERE voyage_number = '319S'),
    (SELECT id FROM ports WHERE unlocode = 'USLAX'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'CONFIRMED',
    '2026-02-17T12:00Z', '2026-04-02T22:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'CC81498611');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'HM22660194', 'HBL106513338', 'MBL726247317', 'REF-2601-0005',
    'MAEU6566707', 'TEU40HC', 45, 'HC',
    24418.0, 24114.0, 67.22, 553, 'bales',
    'Mercurio Commodities SA', 'Rioplate Logistics SA', 'Rioplate Logistics SA', 'Ana Souza',
    'CFR', 'Prepaid', 'Pulp bales', 'India-SA',
    NULL, 'COMPLETE', 'IN_PROGRESS', 'LOW', 2,
    'https://www.vesselfinder.com/vessels/details/9385001', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '902W'),
    (SELECT id FROM ports WHERE unlocode = 'INHZX'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-01-07T12:00Z', '2026-03-28T03:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'HM22660194');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'HM39219319', 'HBL978820812', 'MBL191361939', 'REF-2601-0006',
    'MSCU9805002', 'TEU40', 40, 'GP',
    16321.0, 15478.0, 35.98, 624, 'bags',
    'Brasil Foods Export SA', 'Cartagena Cargo SAS', 'Cartagena Cargo SAS', 'Kauê Santana',
    'FOB', 'Prepaid', 'Chemical resins', 'Pacific-SA',
    (SELECT id FROM ports WHERE unlocode = 'PABLB'), 'COMPLETE', 'NOT_STARTED', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9625530', 'Via Balboa.',
    (SELECT id FROM voyages WHERE voyage_number = '568E'),
    (SELECT id FROM ports WHERE unlocode = 'MXLZC'),
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'CONFIRMED',
    '2026-01-22T15:00Z', '2026-04-09T18:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'HM39219319');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MS77898694', 'HBL278498084', 'MBL124118244', 'REF-2602-0007',
    'OOLU2513545', 'REEFER40', 40, 'RF',
    9944.0, 9637.0, 32.68, 277, 'cartons',
    'Nova Energia Equipamentos', 'Tanger Industrial Supply', 'Tanger Industrial Supply', 'Marina Rocha',
    'CIF', 'Collect', 'Frozen poultry', 'Far East-SA',
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'), 'PARTIALLY_RECEIVED', 'NOT_STARTED', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9282261', 'Via Singapore.',
    (SELECT id FROM voyages WHERE voyage_number = '752E'),
    (SELECT id FROM ports WHERE unlocode = 'KRPUS'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'BOOKED',
    '2026-02-11T20:00Z', '2026-02-12T05:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MS77898694');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MS20099059', 'HBL262045053', 'MBL315869232', 'REF-2602-0008',
    'TRIU2805985', 'TEU20', 20, 'GP',
    15039.0, 13797.0, 20.94, 476, 'bags',
    'BlueWave Minerals', 'Andes Importaciones SRL', 'Andes Importaciones SRL', 'Marina Rocha',
    'CIF', 'Prepaid', 'Green coffee beans', 'Far East-SA',
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'), 'PARTIALLY_RECEIVED', 'CLEARED', 'MEDIUM', 1,
    'https://www.vesselfinder.com/vessels/details/9282261', 'Via Singapore.',
    (SELECT id FROM voyages WHERE voyage_number = '328N'),
    (SELECT id FROM ports WHERE unlocode = 'CNSHA'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-02-02T23:00Z', '2026-03-26T17:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MS20099059');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'HM96098221', 'HBL294019655', 'MBL698169340', 'REF-2602-0009',
    'FCIU6850146', 'TEU40HC', 40, 'HC',
    25117.0, 24184.0, 50.35, 678, 'pallets',
    'Santos Trading Export Ltda', 'Pacific Mercantile Inc.', 'Pacific Mercantile Inc.', 'Diego Martins',
    'DAP', 'Prepaid', 'Solar components', 'Far East-SA',
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'), 'PENDING', 'NOT_STARTED', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9637246', 'Via Singapore.',
    (SELECT id FROM voyages WHERE voyage_number = '512E'),
    (SELECT id FROM ports WHERE unlocode = 'KRPUS'),
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'BOOKED',
    '2026-02-20T01:00Z', '2026-02-22T11:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'HM96098221');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MK33357554', 'HBL436995777', 'MBL387214895', 'REF-2602-0010',
    'UETU9468041', 'TEU40HC', 45, 'HC',
    26923.0, 25775.0, 61.97, 1188, 'cartons',
    'Apex Chemicals Brasil', 'Norteuropa Procurement ApS', 'Norteuropa Procurement ApS', 'Marina Rocha',
    'EXW', 'Prepaid', 'Textiles', 'Americas Loop',
    NULL, 'COMPLETE', 'IN_PROGRESS', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9332987', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '299W'),
    (SELECT id FROM ports WHERE unlocode = 'USNYC'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'ARRIVED',
    '2026-02-20T08:00Z', '2026-03-29T17:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MK33357554');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'CC67062156', 'HBL172788957', 'MBL986872774', 'REF-2602-0011',
    'TCLU2870836', 'TEU20', 20, 'GP',
    17403.0, 16416.0, 20.44, 1116, 'bags',
    'Parana Timber Exports', 'Pacific Mercantile Inc.', 'Pacific Mercantile Inc.', 'Bruna Lima',
    'CIF', 'Prepaid', 'Green coffee beans', 'Americas West Coast',
    (SELECT id FROM ports WHERE unlocode = 'PABLB'), 'COMPLETE', 'IN_PROGRESS', 'LOW', 1,
    'https://www.vesselfinder.com/vessels/details/9351141', 'Via Balboa.',
    (SELECT id FROM voyages WHERE voyage_number = '182S'),
    (SELECT id FROM ports WHERE unlocode = 'USLAX'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'ARRIVED',
    '2026-02-15T04:00Z', '2026-03-23T14:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'CC67062156');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'CC65682626', 'HBL669096705', 'MBL466889373', 'REF-2601-0012',
    'TRHU5876037', 'TEU40', 40, 'GP',
    11250.0, 9634.0, 37.72, 955, 'bags',
    'BlueWave Minerals', 'Dubai Market Connect FZE', 'Dubai Market Connect FZE', 'Kauê Santana',
    'CFR', 'Prepaid', 'Green coffee beans', 'Med-SA',
    NULL, 'PARTIALLY_RECEIVED', 'HOLD', 'HIGH', 7,
    'https://www.vesselfinder.com/vessels/details/9951525', 'Shipment flagged for delay follow-up.',
    (SELECT id FROM voyages WHERE voyage_number = '286N'),
    (SELECT id FROM ports WHERE unlocode = 'FOSFM'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-01-27T02:00Z', '2026-03-28T13:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'CC65682626');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'HM60885459', 'HBL053100330', 'MBL923271937', 'REF-2603-0013',
    'OOLU6417088', 'TEU40HC', 40, 'HC',
    11839.0, 10112.0, 55.94, 257, 'pallets',
    'Oceanic Paper & Pulp', 'Asia Pacific Sourcing Pte Ltd', 'Asia Pacific Sourcing Pte Ltd', 'Diego Martins',
    'CFR', 'Collect', 'Solar components', 'Far East-SA',
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'), 'PARTIALLY_RECEIVED', 'IN_PROGRESS', 'MEDIUM', 1,
    'https://www.vesselfinder.com/vessels/details/9637246', 'Via Singapore.',
    (SELECT id FROM voyages WHERE voyage_number = '915N'),
    (SELECT id FROM ports WHERE unlocode = 'KRPUS'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-03-03T07:00Z', '2026-03-28T08:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'HM60885459');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MK55799273', 'HBL262849877', 'MBL694531473', 'REF-2601-0014',
    'MSCU6716576', 'REEFER40', 40, 'RF',
    20126.0, 19297.0, 33.04, 575, 'cartons',
    'Porto Norte Insumos', 'Cartagena Cargo SAS', 'Cartagena Cargo SAS', 'Ana Souza',
    'DAP', 'Prepaid', 'Refrigerated fruit', 'Africa-SA',
    NULL, 'PARTIALLY_RECEIVED', 'NOT_STARTED', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9333008', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '669N'),
    (SELECT id FROM ports WHERE unlocode = 'ZADUR'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'BOOKED',
    '2026-01-28T02:00Z', '2026-01-29T09:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MK55799273');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MK84514489', 'HBL436349578', 'MBL856855744', 'REF-2602-0015',
    'TXGU3777013', 'TEU40HC', 45, 'HC',
    19454.0, 18415.0, 24.51, 38, 'bundles',
    'Apex Chemicals Brasil', 'Hamburg Distribution GmbH', 'Hamburg Distribution GmbH', 'Ana Souza',
    'CFR', 'Prepaid', 'Timber products', 'Asia-SAEC',
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'), 'PARTIALLY_RECEIVED', 'IN_PROGRESS', 'CRITICAL', 3,
    'https://www.vesselfinder.com/vessels/details/9622203', 'Shipment flagged for delay follow-up.',
    (SELECT id FROM voyages WHERE voyage_number = '825S'),
    (SELECT id FROM ports WHERE unlocode = 'CNSHA'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-02-21T02:00Z', '2026-03-29T22:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MK84514489');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MK75884623', 'HBL520471167', 'MBL190229413', 'REF-2602-0016',
    'MAEU0947774', 'TEU40', 40, 'GP',
    18506.0, 17042.0, 47.91, 213, 'cartons',
    'SP Consumer Goods', 'Sakura Imports Co.', 'Sakura Imports Co.', 'Diego Martins',
    'CIF', 'Prepaid', 'Textiles', 'Neosamba',
    NULL, 'COMPLETE', 'IN_PROGRESS', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9526916', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '881S'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    (SELECT id FROM ports WHERE unlocode = 'BEANR'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'ARRIVED',
    '2026-02-16T20:00Z', '2026-03-25T00:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MK75884623');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'ON84087145', 'HBL403447134', 'MBL936183242', 'REF-2601-0017',
    'MAEU2067973', 'TEU40', 40, 'GP',
    26194.0, 24883.0, 38.2, 91, 'crates',
    'Brasil Foods Export SA', 'Asia Pacific Sourcing Pte Ltd', 'Asia Pacific Sourcing Pte Ltd', 'Felipe Costa',
    'DAP', 'Prepaid', 'Auto parts', 'Far East-SA',
    (SELECT id FROM ports WHERE unlocode = 'PABLB'), 'PARTIALLY_RECEIVED', 'NOT_STARTED', 'MEDIUM', 0,
    'https://www.vesselfinder.com/vessels/details/9588079', 'Via Balboa.',
    (SELECT id FROM voyages WHERE voyage_number = '542E'),
    (SELECT id FROM ports WHERE unlocode = 'CNSHA'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'CONFIRMED',
    '2026-01-04T03:00Z', '2026-04-07T09:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'ON84087145');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'LI12784407', 'HBL742967175', 'MBL655125674', 'REF-2603-0018',
    'TXGU4902784', 'REEFER40', 40, 'RF',
    14646.0, 13285.0, 34.17, 1030, 'cartons',
    'SP Consumer Goods', 'Rioplate Logistics SA', 'Rioplate Logistics SA', 'Bruna Lima',
    'FOB', 'Prepaid', 'Refrigerated fruit', 'Mercosur Shuttle',
    NULL, 'COMPLETE', 'IN_PROGRESS', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9571296', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '740W'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'GATE_IN',
    '2026-03-06T02:00Z', '2026-04-03T01:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'LI12784407');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MK75056916', 'HBL086131712', 'MBL748467737', 'REF-2601-0019',
    'MAEU0932481', 'TEU40HC', 45, 'HC',
    28044.0, 27440.0, 38.67, 1001, 'cartons',
    'Atlantic Coffee Exportadora', 'Montevideo Trading House', 'Montevideo Trading House', 'Marina Rocha',
    'DAP', 'Collect', 'Refrigerated fruit', 'Asia-SA',
    NULL, 'PARTIALLY_RECEIVED', 'CLEARED', 'MEDIUM', 0,
    'https://www.vesselfinder.com/vessels/details/9298698', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '453E'),
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'LOADED',
    '2026-01-10T13:00Z', '2026-04-01T23:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MK75056916');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'CC42051937', 'HBL662702895', 'MBL171870262', 'REF-2601-0020',
    'UETU6360578', 'TEU40', 40, 'GP',
    17916.0, 17151.0, 54.58, 898, 'cartons',
    'Global Rubber Brasil', 'Southern Cross Forwarding', 'Southern Cross Forwarding', 'Felipe Costa',
    'EXW', 'Prepaid', 'Refrigerated fruit', 'Med-SA',
    NULL, 'PARTIALLY_RECEIVED', 'IN_PROGRESS', 'MEDIUM', 0,
    'https://www.vesselfinder.com/vessels/details/9951525', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '200N'),
    (SELECT id FROM ports WHERE unlocode = 'ESALG'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'LOADED',
    '2026-01-25T16:00Z', '2026-04-01T16:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'CC42051937');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'HM17532195', 'HBL692221969', 'MBL379237474', 'REF-2601-0021',
    'OOLU5562380', 'TEU20', 20, 'GP',
    23469.0, 22551.0, 18.39, 797, 'pallets',
    'Andes Fresh Produce', 'Norteuropa Procurement ApS', 'Norteuropa Procurement ApS', 'Ana Souza',
    'FOB', 'Prepaid', 'Solar components', 'Pacific-SA',
    (SELECT id FROM ports WHERE unlocode = 'PABLB'), 'PARTIALLY_RECEIVED', 'NOT_STARTED', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9625530', 'Via Balboa.',
    (SELECT id FROM voyages WHERE voyage_number = '973W'),
    (SELECT id FROM ports WHERE unlocode = 'MXLZC'),
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'BOOKED',
    '2026-01-07T07:00Z', '2026-01-09T16:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'HM17532195');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'CC49625201', 'HBL743953394', 'MBL210470952', 'REF-2601-0022',
    'MSCU6409097', 'TEU40HC', 40, 'HC',
    19599.0, 19064.0, 39.82, 164, 'cartons',
    'Rio Sul Auto Parts', 'Tanger Industrial Supply', 'Tanger Industrial Supply', 'Ana Souza',
    'EXW', 'Collect', 'Refrigerated fruit', 'Med-SA',
    NULL, 'COMPLETE', 'CLEARED', 'LOW', 3,
    'https://www.vesselfinder.com/vessels/details/9951525', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '244S'),
    (SELECT id FROM ports WHERE unlocode = 'ESALG'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-01-23T07:00Z', '2026-03-27T14:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'CC49625201');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'ON82017516', 'HBL137098593', 'MBL174612004', 'REF-2601-0023',
    'MAEU7549659', 'TEU20', 20, 'GP',
    24534.0, 22974.0, 21.94, 582, 'bags',
    'Mercurio Commodities SA', 'Hamburg Distribution GmbH', 'Hamburg Distribution GmbH', 'Marina Rocha',
    'CIF', 'Prepaid', 'Chemical resins', 'LatAm Pacific',
    NULL, 'PARTIALLY_RECEIVED', 'CLEARED', 'MEDIUM', 3,
    'https://www.vesselfinder.com/vessels/details/9475636', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '133E'),
    (SELECT id FROM ports WHERE unlocode = 'PABLB'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-01-08T08:00Z', '2026-03-27T22:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'ON82017516');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MS59302863', 'HBL713900532', 'MBL931839335', 'REF-2602-0024',
    'FCIU5064311', 'TEU20', 20, 'GP',
    16296.0, 14791.0, 20.5, 366, 'cartons',
    'Green Harvest Trading', 'Andes Importaciones SRL', 'Andes Importaciones SRL', 'Felipe Costa',
    'CIF', 'Collect', 'Textiles', 'Far East-SA',
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'), 'PARTIALLY_RECEIVED', 'IN_PROGRESS', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9282261', 'Via Singapore.',
    (SELECT id FROM voyages WHERE voyage_number = '371N'),
    (SELECT id FROM ports WHERE unlocode = 'KRPUS'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'LOADED',
    '2026-02-27T08:00Z', '2026-03-30T14:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MS59302863');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MK70166480', 'HBL390847007', 'MBL661771159', 'REF-2601-0025',
    'TXGU5891787', 'TEU20', 20, 'GP',
    26566.0, 24790.0, 29.71, 890, 'bundles',
    'Sul Agro Comex Ltda', 'Benelux Commodities NV', 'Benelux Commodities NV', 'Felipe Costa',
    'EXW', 'Prepaid', 'Timber products', 'Med-SA',
    NULL, 'COMPLETE', 'IN_PROGRESS', 'CRITICAL', 6,
    'https://www.vesselfinder.com/vessels/details/9333008', 'Shipment flagged for delay follow-up.',
    (SELECT id FROM voyages WHERE voyage_number = '562S'),
    (SELECT id FROM ports WHERE unlocode = 'MAPTM'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-01-01T19:00Z', '2026-03-29T16:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MK70166480');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MK22762205', 'HBL111161528', 'MBL098851656', 'REF-2601-0026',
    'HLBU6545276', 'TEU40HC', 45, 'HC',
    19410.0, 18232.0, 59.79, 1150, 'cartons',
    'Rio Sul Auto Parts', 'Cartagena Cargo SAS', 'Cartagena Cargo SAS', 'Felipe Costa',
    'FOB', 'Prepaid', 'Refrigerated fruit', 'NEU-SA',
    NULL, 'COMPLETE', 'IN_PROGRESS', 'MEDIUM', 0,
    'https://www.vesselfinder.com/vessels/details/9332975', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '126E'),
    (SELECT id FROM ports WHERE unlocode = 'NLRTM'),
    (SELECT id FROM ports WHERE unlocode = 'BRRIG'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-01-31T09:00Z', '2026-03-25T18:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MK22762205');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MK34347841', 'HBL183667525', 'MBL459910229', 'REF-2601-0027',
    'CAXU2961206', 'TEU40HC', 40, 'HC',
    17781.0, 16093.0, 50.96, 1006, 'cartons',
    'Sul Agro Comex Ltda', 'Southern Cross Forwarding', 'Southern Cross Forwarding', 'Bruna Lima',
    'FOB', 'Prepaid', 'Frozen poultry', 'Asia-SA',
    NULL, 'COMPLETE', 'CLEARED', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9298698', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '639E'),
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'LOADED',
    '2026-01-21T21:00Z', '2026-03-30T18:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MK34347841');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'CC38351344', 'HBL762268388', 'MBL516060715', 'REF-2601-0028',
    'TXGU2445101', 'TEU40', 40, 'GP',
    20357.0, 19877.0, 38.15, 226, 'bags',
    'BlueWave Minerals', 'Chile Trade Partners SpA', 'Chile Trade Partners SpA', 'Bruna Lima',
    'DAP', 'Prepaid', 'Chemical resins', 'Med-SA',
    NULL, 'PENDING', 'NOT_STARTED', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9951525', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '349W'),
    (SELECT id FROM ports WHERE unlocode = 'FOSFM'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'BOOKED',
    '2026-01-23T03:00Z', '2026-01-25T18:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'CC38351344');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'ON55568701', 'HBL835523124', 'MBL329212779', 'REF-2602-0029',
    'TCLU5218185', 'TEU40HC', 40, 'HC',
    10931.0, 9996.0, 39.47, 87, 'cartons',
    'Andes Fresh Produce', 'Chile Trade Partners SpA', 'Chile Trade Partners SpA', 'Ana Souza',
    'CFR', 'Prepaid', 'Refrigerated fruit', 'Pacific-SA',
    (SELECT id FROM ports WHERE unlocode = 'PABLB'), 'PARTIALLY_RECEIVED', 'CLEARED', 'MEDIUM', 0,
    'https://www.vesselfinder.com/vessels/details/9566382', 'Via Balboa.',
    (SELECT id FROM voyages WHERE voyage_number = '158E'),
    (SELECT id FROM ports WHERE unlocode = 'JPYOK'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-02-03T16:00Z', '2026-03-26T20:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'ON55568701');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'HM61604364', 'HBL597820715', 'MBL182037788', 'REF-2602-0030',
    'TEMU9807935', 'REEFER40', 40, 'RF',
    10659.0, 9684.0, 20.01, 801, 'cartons',
    'ValeSteel Trading', 'Buenos Aires Wholesale SA', 'Buenos Aires Wholesale SA', 'Felipe Costa',
    'DAP', 'Prepaid', 'Refrigerated fruit', 'India-SA',
    NULL, 'COMPLETE', 'CLEARED', 'LOW', 1,
    'https://www.vesselfinder.com/vessels/details/9385013', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '390E'),
    (SELECT id FROM ports WHERE unlocode = 'LKCMB'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'ARRIVED',
    '2026-02-09T00:00Z', '2026-03-25T00:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'HM61604364');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'HM51638598', 'HBL652816850', 'MBL542357332', 'REF-2601-0031',
    'DFSU6291484', 'TEU40', 40, 'GP',
    9931.0, 8790.0, 34.31, 496, 'pallets',
    'Sul Agro Comex Ltda', 'Norteuropa Procurement ApS', 'Norteuropa Procurement ApS', 'Kauê Santana',
    'EXW', 'Prepaid', 'Solar components', 'Far East-SA',
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'), 'PARTIALLY_RECEIVED', 'IN_PROGRESS', 'LOW', 3,
    'https://www.vesselfinder.com/vessels/details/9637246', 'Via Singapore.',
    (SELECT id FROM voyages WHERE voyage_number = '554E'),
    (SELECT id FROM ports WHERE unlocode = 'KRPUS'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-01-11T19:00Z', '2026-03-28T23:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'HM51638598');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'CC59370777', 'HBL862392407', 'MBL581814124', 'REF-2603-0032',
    'DFSU9774681', 'TEU40HC', 45, 'HC',
    14674.0, 13607.0, 26.9, 662, 'bales',
    'Mercosul Machinery', 'Benelux Commodities NV', 'Benelux Commodities NV', 'Bruna Lima',
    'CIF', 'Prepaid', 'Pulp bales', 'Med-SA',
    NULL, 'COMPLETE', 'IN_PROGRESS', 'MEDIUM', 0,
    'https://www.vesselfinder.com/vessels/details/9882499', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '201E'),
    (SELECT id FROM ports WHERE unlocode = 'MAPTM'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'LOADED',
    '2026-03-06T05:00Z', '2026-03-31T19:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'CC59370777');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MK72962895', 'HBL289861434', 'MBL103697117', 'REF-2601-0033',
    'TEMU9010432', 'TEU40', 40, 'GP',
    14637.0, 12977.0, 50.42, 1082, 'cartons',
    'SP Consumer Goods', 'Andes Importaciones SRL', 'Andes Importaciones SRL', 'Ana Souza',
    'DAP', 'Collect', 'Refrigerated fruit', 'Neosamba',
    NULL, 'COMPLETE', 'IN_PROGRESS', 'MEDIUM', 1,
    'https://www.vesselfinder.com/vessels/details/9526916', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '469N'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    (SELECT id FROM ports WHERE unlocode = 'BEANR'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-01-10T18:00Z', '2026-03-29T02:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MK72962895');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'ON95318289', 'HBL319520585', 'MBL277221704', 'REF-2602-0034',
    'TRHU5405150', 'TEU40HC', 45, 'HC',
    22876.0, 22017.0, 21.6, 763, 'cartons',
    'Brasil Foods Export SA', 'Mediterranean Foods SARL', 'Mediterranean Foods SARL', 'Kauê Santana',
    'EXW', 'Prepaid', 'Frozen poultry', 'Pacific-SA',
    (SELECT id FROM ports WHERE unlocode = 'PABLB'), 'COMPLETE', 'CLEARED', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9566382', 'Via Balboa.',
    (SELECT id FROM voyages WHERE voyage_number = '547W'),
    (SELECT id FROM ports WHERE unlocode = 'JPYOK'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'LOADED',
    '2026-02-05T03:00Z', '2026-03-30T22:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'ON95318289');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'HM79696025', 'HBL845115447', 'MBL962757059', 'REF-2601-0035',
    'OOLU1616929', 'TEU40HC', 45, 'HC',
    23866.0, 22782.0, 65.32, 76, 'pallets',
    'Parana Timber Exports', 'Rioplate Logistics SA', 'Rioplate Logistics SA', 'Kauê Santana',
    'EXW', 'Prepaid', 'Solar components', 'Far East-SA',
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'), 'PARTIALLY_RECEIVED', 'IN_PROGRESS', 'LOW', 3,
    'https://www.vesselfinder.com/vessels/details/9637246', 'Via Singapore.',
    (SELECT id FROM voyages WHERE voyage_number = '719S'),
    (SELECT id FROM ports WHERE unlocode = 'KRPUS'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-01-25T01:00Z', '2026-03-28T23:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'HM79696025');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'ON28481124', 'HBL027868144', 'MBL739473121', 'REF-2602-0036',
    'FCIU5654318', 'TEU20', 20, 'GP',
    15085.0, 14501.0, 19.02, 1021, 'crates',
    'ValeSteel Trading', 'Qingdao Distribution Ltd.', 'Qingdao Distribution Ltd.', 'Kauê Santana',
    'DAP', 'Collect', 'Industrial machinery', 'LatAm Pacific',
    NULL, 'PARTIALLY_RECEIVED', 'IN_PROGRESS', 'HIGH', 2,
    'https://www.vesselfinder.com/vessels/details/9475636', 'Requires operational attention.',
    (SELECT id FROM voyages WHERE voyage_number = '126E'),
    (SELECT id FROM ports WHERE unlocode = 'PABLB'),
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-02-04T15:00Z', '2026-03-30T09:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'ON28481124');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'ON21571646', 'HBL912517785', 'MBL289226801', 'REF-2602-0037',
    'MAEU4678668', 'TEU40', 40, 'GP',
    18422.0, 16864.0, 43.27, 1039, 'crates',
    'Atlantic Coffee Exportadora', 'Norteuropa Procurement ApS', 'Norteuropa Procurement ApS', 'Marina Rocha',
    'DAP', 'Collect', 'Industrial machinery', 'LatAm Pacific',
    NULL, 'PARTIALLY_RECEIVED', 'CLEARED', 'MEDIUM', 3,
    'https://www.vesselfinder.com/vessels/details/9475636', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '756S'),
    (SELECT id FROM ports WHERE unlocode = 'PABLB'),
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-02-25T02:00Z', '2026-03-29T15:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'ON21571646');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MS91220369', 'HBL439690784', 'MBL473647102', 'REF-2602-0038',
    'HLBU9001091', 'REEFER40', 40, 'RF',
    21565.0, 20998.0, 55.32, 1064, 'cartons',
    'BlueWave Minerals', 'Dubai Market Connect FZE', 'Dubai Market Connect FZE', 'Bruna Lima',
    'CFR', 'Prepaid', 'Refrigerated fruit', 'Far East-SAEC',
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'), 'COMPLETE', 'IN_PROGRESS', 'LOW', 1,
    'https://www.vesselfinder.com/vessels/details/9963580', 'Via Singapore.',
    (SELECT id FROM voyages WHERE voyage_number = '675N'),
    (SELECT id FROM ports WHERE unlocode = 'CNSHA'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'ARRIVED',
    '2026-02-19T02:00Z', '2026-03-29T02:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MS91220369');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MS28908276', 'HBL259532787', 'MBL747016873', 'REF-2603-0039',
    'MAEU0469633', 'TEU40HC', 45, 'HC',
    20666.0, 18877.0, 34.39, 835, 'bales',
    'Porto Norte Insumos', 'Buenos Aires Wholesale SA', 'Buenos Aires Wholesale SA', 'Kauê Santana',
    'EXW', 'Prepaid', 'Pulp bales', 'Far East-SA',
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'), 'PARTIALLY_RECEIVED', 'CLEARED', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9282261', 'Via Singapore.',
    (SELECT id FROM voyages WHERE voyage_number = '474N'),
    (SELECT id FROM ports WHERE unlocode = 'KRPUS'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'LOADED',
    '2026-03-11T03:00Z', '2026-03-31T14:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MS28908276');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MK19869264', 'HBL121655852', 'MBL398680002', 'REF-2602-0040',
    'TRHU8784994', 'REEFER40', 40, 'RF',
    25202.0, 23812.0, 40.96, 1162, 'cartons',
    'Global Rubber Brasil', 'Pacific Mercantile Inc.', 'Pacific Mercantile Inc.', 'Bruna Lima',
    'FOB', 'Prepaid', 'Pharmaceutical supplies', 'Florida Express',
    NULL, 'COMPLETE', 'CLEARED', 'MEDIUM', 0,
    'https://www.vesselfinder.com/vessels/details/9348649', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '406W'),
    (SELECT id FROM ports WHERE unlocode = 'USMIA'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'LOADED',
    '2026-02-25T17:00Z', '2026-03-31T10:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MK19869264');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'HM94457912', 'HBL097289623', 'MBL091307562', 'REF-2601-0041',
    'MSCU9749932', 'REEFER40', 40, 'RF',
    23562.0, 22171.0, 33.14, 735, 'cartons',
    'Nova Energia Equipamentos', 'Sakura Imports Co.', 'Sakura Imports Co.', 'Diego Martins',
    'DAP', 'Prepaid', 'Pharmaceutical supplies', 'Far East-SA',
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'), 'COMPLETE', 'CLEARED', 'MEDIUM', 3,
    'https://www.vesselfinder.com/vessels/details/9637246', 'Via Singapore.',
    (SELECT id FROM voyages WHERE voyage_number = '792E'),
    (SELECT id FROM ports WHERE unlocode = 'KRPUS'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-01-15T13:00Z', '2026-03-28T21:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'HM94457912');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MK40181142', 'HBL844198665', 'MBL240415747', 'REF-2601-0042',
    'OOLU4375759', 'REEFER40', 40, 'RF',
    11783.0, 10637.0, 32.51, 977, 'cartons',
    'Nova Energia Equipamentos', 'Iberia Consumer Products SL', 'Iberia Consumer Products SL', 'Felipe Costa',
    'FOB', 'Collect', 'Pharmaceutical supplies', 'Americas Loop',
    NULL, 'PARTIALLY_RECEIVED', 'IN_PROGRESS', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9332987', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '202S'),
    (SELECT id FROM ports WHERE unlocode = 'USORF'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'LOADED',
    '2026-01-04T14:00Z', '2026-03-31T11:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MK40181142');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'HM97313825', 'HBL380262067', 'MBL240049915', 'REF-2601-0043',
    'TCLU4540197', 'TEU40HC', 45, 'HC',
    17324.0, 15527.0, 39.32, 536, 'bundles',
    'Andes Fresh Produce', 'Iberia Consumer Products SL', 'Iberia Consumer Products SL', 'Diego Martins',
    'FOB', 'Collect', 'Timber products', 'NEU-SA',
    NULL, 'COMPLETE', 'CLEARED', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9868364', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '835S'),
    (SELECT id FROM ports WHERE unlocode = 'NLRTM'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'ARRIVED',
    '2026-01-13T08:00Z', '2026-03-23T07:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'HM97313825');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'HM92000924', 'HBL122753046', 'MBL374549904', 'REF-2603-0044',
    'CAXU6685169', 'TEU40', 40, 'GP',
    15736.0, 14988.0, 65.74, 989, 'crates',
    'Apex Chemicals Brasil', 'Rioplate Logistics SA', 'Rioplate Logistics SA', 'Ana Souza',
    'DAP', 'Prepaid', 'Auto parts', 'Far East-SA',
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'), 'PARTIALLY_RECEIVED', 'CLEARED', 'MEDIUM', 3,
    'https://www.vesselfinder.com/vessels/details/9637246', 'Via Singapore.',
    (SELECT id FROM voyages WHERE voyage_number = '256W'),
    (SELECT id FROM ports WHERE unlocode = 'KRPUS'),
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-03-01T22:00Z', '2026-03-26T15:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'HM92000924');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MK39314711', 'HBL519108839', 'MBL822587139', 'REF-2603-0045',
    'TXGU3918786', 'REEFER40', 40, 'RF',
    16838.0, 16532.0, 54.53, 1194, 'cartons',
    'Sul Agro Comex Ltda', 'Pacific Mercantile Inc.', 'Pacific Mercantile Inc.', 'Bruna Lima',
    'EXW', 'Prepaid', 'Refrigerated fruit', 'Africa-SA',
    NULL, 'COMPLETE', 'IN_PROGRESS', 'CRITICAL', 5,
    'https://www.vesselfinder.com/vessels/details/9333008', 'Shipment flagged for delay follow-up.',
    (SELECT id FROM voyages WHERE voyage_number = '174N'),
    (SELECT id FROM ports WHERE unlocode = 'ZADUR'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-03-09T22:00Z', '2026-04-01T01:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MK39314711');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'HM73360324', 'HBL676618251', 'MBL286823004', 'REF-2601-0046',
    'MSCU4622652', 'TEU20', 20, 'GP',
    15252.0, 14815.0, 16.72, 78, 'bags',
    'SP Consumer Goods', 'Sakura Imports Co.', 'Sakura Imports Co.', 'Diego Martins',
    'CIF', 'Collect', 'Green coffee beans', 'Pacific-SA',
    (SELECT id FROM ports WHERE unlocode = 'PABLB'), 'COMPLETE', 'IN_PROGRESS', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9625530', 'Via Balboa.',
    (SELECT id FROM voyages WHERE voyage_number = '557N'),
    (SELECT id FROM ports WHERE unlocode = 'MXLZC'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-01-28T05:00Z', '2026-03-26T17:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'HM73360324');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MK39161379', 'HBL599534283', 'MBL644089268', 'REF-2601-0047',
    'TXGU0284395', 'TEU40', 40, 'GP',
    15884.0, 14969.0, 60.45, 305, 'cartons',
    'Brasil Foods Export SA', 'Buenos Aires Wholesale SA', 'Buenos Aires Wholesale SA', 'Ana Souza',
    'FOB', 'Collect', 'Home appliances', 'Neosamba',
    NULL, 'PARTIALLY_RECEIVED', 'CLEARED', 'MEDIUM', 2,
    'https://www.vesselfinder.com/vessels/details/9526916', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '925W'),
    (SELECT id FROM ports WHERE unlocode = 'DEHAM'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-01-27T20:00Z', '2026-03-30T11:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MK39161379');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MK62132722', 'HBL183046990', 'MBL139837354', 'REF-2603-0048',
    'TCLU4351758', 'REEFER40', 40, 'RF',
    25986.0, 24358.0, 34.04, 545, 'cartons',
    'Nova Energia Equipamentos', 'Mediterranean Foods SARL', 'Mediterranean Foods SARL', 'Ana Souza',
    'EXW', 'Prepaid', 'Frozen poultry', 'Africa-SA',
    NULL, 'COMPLETE', 'CLEARED', 'MEDIUM', 2,
    'https://www.vesselfinder.com/vessels/details/9333008', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '468W'),
    (SELECT id FROM ports WHERE unlocode = 'ZADUR'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-03-01T13:00Z', '2026-03-30T10:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MK62132722');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MK59755172', 'HBL489964235', 'MBL369378547', 'REF-2601-0049',
    'HLBU6093200', 'TEU40HC', 40, 'HC',
    22013.0, 21432.0, 59.65, 483, 'bales',
    'Andes Fresh Produce', 'Asia Pacific Sourcing Pte Ltd', 'Asia Pacific Sourcing Pte Ltd', 'Ana Souza',
    'FOB', 'Prepaid', 'Pulp bales', 'Americas Loop',
    NULL, 'PENDING', 'IN_PROGRESS', 'HIGH', 2,
    'https://www.vesselfinder.com/vessels/details/9342176', 'Requires operational attention.',
    (SELECT id FROM voyages WHERE voyage_number = '169S'),
    (SELECT id FROM ports WHERE unlocode = 'USNYC'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-01-31T07:00Z', '2026-03-31T19:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MK59755172');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'ON95594482', 'HBL860084840', 'MBL862118233', 'REF-2602-0050',
    'DFSU9770080', 'TEU20', 20, 'GP',
    9538.0, 9047.0, 21.89, 782, 'crates',
    'Mercosul Machinery', 'Southern Cross Forwarding', 'Southern Cross Forwarding', 'Felipe Costa',
    'FOB', 'Collect', 'Industrial machinery', 'Pacific-SA',
    (SELECT id FROM ports WHERE unlocode = 'PABLB'), 'PARTIALLY_RECEIVED', 'CLEARED', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9566382', 'Via Balboa.',
    (SELECT id FROM voyages WHERE voyage_number = '665W'),
    (SELECT id FROM ports WHERE unlocode = 'JPUKB'),
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'LOADED',
    '2026-02-11T00:00Z', '2026-04-01T12:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'ON95594482');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'LI96557984', 'HBL397401489', 'MBL025039261', 'REF-2601-0051',
    'TXGU5918039', 'TEU40HC', 45, 'HC',
    28409.0, 27401.0, 17.85, 905, 'cartons',
    'ValeSteel Trading', 'Chile Trade Partners SpA', 'Chile Trade Partners SpA', 'Marina Rocha',
    'CFR', 'Collect', 'Pharmaceutical supplies', 'Mercosur Shuttle',
    NULL, 'COMPLETE', 'NOT_STARTED', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9571296', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '337N'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    (SELECT id FROM ports WHERE unlocode = 'UYMVD'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'CONFIRMED',
    '2026-01-04T00:00Z', '2026-04-09T09:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'LI96557984');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'CC95110541', 'HBL629619431', 'MBL142626551', 'REF-2601-0052',
    'OOLU7735864', 'TEU40HC', 40, 'HC',
    11549.0, 10027.0, 46.7, 674, 'cartons',
    'Santos Trading Export Ltda', 'Norteuropa Procurement ApS', 'Norteuropa Procurement ApS', 'Bruna Lima',
    'FOB', 'Prepaid', 'Frozen poultry', 'SA-Asia',
    NULL, 'PENDING', 'HOLD', 'HIGH', 1,
    'https://www.vesselfinder.com/vessels/details/9679907', 'Requires operational attention.',
    (SELECT id FROM voyages WHERE voyage_number = '984W'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-01-30T04:00Z', '2026-03-30T01:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'CC95110541');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MK85099723', 'HBL244472126', 'MBL464715437', 'REF-2603-0053',
    'TRIU3982624', 'TEU20', 20, 'GP',
    19007.0, 17472.0, 31.1, 421, 'cartons',
    'Green Harvest Trading', 'Mediterranean Foods SARL', 'Mediterranean Foods SARL', 'Bruna Lima',
    'CIF', 'Prepaid', 'Refrigerated fruit', 'Africa-SA',
    NULL, 'PARTIALLY_RECEIVED', 'IN_PROGRESS', 'CRITICAL', 2,
    'https://www.vesselfinder.com/vessels/details/9333008', 'Requires operational attention.',
    (SELECT id FROM voyages WHERE voyage_number = '590E'),
    (SELECT id FROM ports WHERE unlocode = 'ZADUR'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-03-04T08:00Z', '2026-03-30T06:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MK85099723');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'HM42537638', 'HBL398280737', 'MBL416724342', 'REF-2601-0054',
    'HLBU6465191', 'TEU40HC', 45, 'HC',
    17450.0, 15825.0, 47.47, 1163, 'crates',
    'Santa Maria Foods', 'Euro Trade Solutions BV', 'Euro Trade Solutions BV', 'Bruna Lima',
    'CFR', 'Prepaid', 'Industrial machinery', 'Far East-SA',
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'), 'PARTIALLY_RECEIVED', 'IN_PROGRESS', 'HIGH', 7,
    'https://www.vesselfinder.com/vessels/details/9637246', 'Shipment flagged for delay follow-up.',
    (SELECT id FROM voyages WHERE voyage_number = '760W'),
    (SELECT id FROM ports WHERE unlocode = 'KRPUS'),
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-01-14T18:00Z', '2026-03-30T00:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'HM42537638');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'CC78541804', 'HBL567982507', 'MBL146112545', 'REF-2601-0055',
    'BMOU8899379', 'TEU40HC', 40, 'HC',
    12271.0, 11925.0, 60.01, 233, 'bales',
    'Nova Energia Equipamentos', 'Pacific Mercantile Inc.', 'Pacific Mercantile Inc.', 'Bruna Lima',
    'CIF', 'Collect', 'Pulp bales', 'Asia SAEC 2',
    NULL, 'PARTIALLY_RECEIVED', 'NOT_STARTED', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9996680', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '793S'),
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'CONFIRMED',
    '2026-01-31T21:00Z', '2026-04-01T05:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'CC78541804');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'HM67226329', 'HBL093796603', 'MBL341918255', 'REF-2601-0056',
    'TEMU3622662', 'TEU40HC', 40, 'HC',
    19900.0, 18880.0, 59.81, 363, 'cartons',
    'Andes Fresh Produce', 'Hamburg Distribution GmbH', 'Hamburg Distribution GmbH', 'Felipe Costa',
    'DAP', 'Collect', 'Home appliances', 'NEU-SA',
    NULL, 'COMPLETE', 'NOT_STARTED', 'MEDIUM', 0,
    'https://www.vesselfinder.com/vessels/details/9868364', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '933E'),
    (SELECT id FROM ports WHERE unlocode = 'BEANR'),
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'CONFIRMED',
    '2026-01-13T20:00Z', '2026-04-08T03:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'HM67226329');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MK50987410', 'HBL929662782', 'MBL827422579', 'REF-2602-0057',
    'MAEU2528669', 'TEU20', 20, 'GP',
    13980.0, 13511.0, 24.61, 461, 'bales',
    'Oceanic Paper & Pulp', 'Andes Importaciones SRL', 'Andes Importaciones SRL', 'Bruna Lima',
    'CFR', 'Prepaid', 'Pulp bales', 'Asia-SAEC',
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'), 'COMPLETE', 'CLEARED', 'MEDIUM', 0,
    'https://www.vesselfinder.com/vessels/details/9622203', 'Via Singapore.',
    (SELECT id FROM voyages WHERE voyage_number = '399N'),
    (SELECT id FROM ports WHERE unlocode = 'CNSHA'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'LOADED',
    '2026-02-28T06:00Z', '2026-04-01T05:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MK50987410');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'CC76479831', 'HBL580783613', 'MBL407451713', 'REF-2601-0058',
    'FCIU8191480', 'TEU40HC', 45, 'HC',
    29441.0, 28566.0, 46.63, 354, 'crates',
    'Porto Norte Insumos', 'Buenos Aires Wholesale SA', 'Buenos Aires Wholesale SA', 'Ana Souza',
    'CFR', 'Collect', 'Industrial machinery', 'Med-SA',
    NULL, 'COMPLETE', 'NOT_STARTED', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9882499', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '431E'),
    (SELECT id FROM ports WHERE unlocode = 'LBBEY'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'CONFIRMED',
    '2026-01-22T07:00Z', '2026-04-10T05:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'CC76479831');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MK47221734', 'HBL672946671', 'MBL168457759', 'REF-2601-0059',
    'CAXU2232539', 'TEU40HC', 45, 'HC',
    22579.0, 21699.0, 32.9, 1193, 'cartons',
    'Sul Agro Comex Ltda', 'Qingdao Distribution Ltd.', 'Qingdao Distribution Ltd.', 'Ana Souza',
    'CFR', 'Collect', 'Frozen poultry', 'Asia-SA',
    NULL, 'PARTIALLY_RECEIVED', 'NOT_STARTED', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9298698', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '233W'),
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'CONFIRMED',
    '2026-01-31T10:00Z', '2026-04-07T10:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MK47221734');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'CC71684020', 'HBL324241541', 'MBL520643611', 'REF-2602-0060',
    'MSCU1277064', 'TEU20', 20, 'GP',
    12451.0, 11287.0, 26.8, 988, 'cartons',
    'Nova Energia Equipamentos', 'Dubai Market Connect FZE', 'Dubai Market Connect FZE', 'Kauê Santana',
    'EXW', 'Prepaid', 'Textiles', 'SA-Asia',
    NULL, 'PENDING', 'NOT_STARTED', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9679907', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '172S'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'BOOKED',
    '2026-02-28T18:00Z', '2026-03-01T05:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'CC71684020');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'ON50165370', 'HBL092753269', 'MBL731121965', 'REF-2602-0061',
    'CMAU6221894', 'TEU40HC', 40, 'HC',
    28387.0, 27044.0, 25.0, 812, 'pallets',
    'LatAm Pharma Logistics', 'Benelux Commodities NV', 'Benelux Commodities NV', 'Marina Rocha',
    'CIF', 'Prepaid', 'Solar components', 'Far East-SA',
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'), 'PENDING', 'NOT_STARTED', 'MEDIUM', 0,
    'https://www.vesselfinder.com/vessels/details/9937323', 'Via Singapore.',
    (SELECT id FROM voyages WHERE voyage_number = '402N'),
    (SELECT id FROM ports WHERE unlocode = 'CNYTN'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'CONFIRMED',
    '2026-02-09T16:00Z', '2026-04-03T13:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'ON50165370');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'CC89715904', 'HBL965827109', 'MBL104300684', 'REF-2601-0062',
    'MAEU8055265', 'TEU40HC', 45, 'HC',
    28754.0, 28038.0, 66.64, 328, 'cartons',
    'BlueWave Minerals', 'Sakura Imports Co.', 'Sakura Imports Co.', 'Ana Souza',
    'EXW', 'Collect', 'Textiles', 'Med-SA',
    NULL, 'PARTIALLY_RECEIVED', 'IN_PROGRESS', 'MEDIUM', 0,
    'https://www.vesselfinder.com/vessels/details/9951525', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '649E'),
    (SELECT id FROM ports WHERE unlocode = 'FOSFM'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'GATE_IN',
    '2026-01-25T10:00Z', '2026-04-06T09:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'CC89715904');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'HM42029368', 'HBL904210202', 'MBL758842524', 'REF-2602-0063',
    'MSCU3161793', 'TEU40HC', 45, 'HC',
    24048.0, 23426.0, 28.52, 220, 'cartons',
    'Santos Trading Export Ltda', 'Tanger Industrial Supply', 'Tanger Industrial Supply', 'Bruna Lima',
    'CIF', 'Collect', 'Refrigerated fruit', 'India-SA',
    NULL, 'PARTIALLY_RECEIVED', 'CLEARED', 'MEDIUM', 0,
    'https://www.vesselfinder.com/vessels/details/9385013', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '687N'),
    (SELECT id FROM ports WHERE unlocode = 'LKCMB'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'LOADED',
    '2026-02-10T20:00Z', '2026-04-01T08:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'HM42029368');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'ON16171536', 'HBL635365419', 'MBL518023806', 'REF-2602-0064',
    'TEMU5622086', 'TEU20', 20, 'GP',
    16448.0, 15198.0, 21.26, 1020, 'bales',
    'Andes Fresh Produce', 'Norteuropa Procurement ApS', 'Norteuropa Procurement ApS', 'Felipe Costa',
    'DAP', 'Prepaid', 'Pulp bales', 'Pacific-SA',
    (SELECT id FROM ports WHERE unlocode = 'PABLB'), 'PARTIALLY_RECEIVED', 'NOT_STARTED', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9566382', 'Via Balboa.',
    (SELECT id FROM voyages WHERE voyage_number = '891S'),
    (SELECT id FROM ports WHERE unlocode = 'JPYOK'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'BOOKED',
    '2026-02-04T10:00Z', '2026-02-05T20:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'ON16171536');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MS82684966', 'HBL383670241', 'MBL038657191', 'REF-2512-0065',
    'HLBU0506415', 'TEU20', 20, 'GP',
    17411.0, 16633.0, 16.27, 425, 'bales',
    'Atlantic Coffee Exportadora', 'Montevideo Trading House', 'Montevideo Trading House', 'Kauê Santana',
    'CIF', 'Prepaid', 'Pulp bales', 'Far East-SAEC',
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'), 'COMPLETE', 'CLEARED', 'MEDIUM', 0,
    'https://www.vesselfinder.com/vessels/details/9963580', 'Via Singapore.',
    (SELECT id FROM voyages WHERE voyage_number = '239N'),
    (SELECT id FROM ports WHERE unlocode = 'CNSHA'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2025-12-31T18:00Z', '2026-03-25T17:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MS82684966');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'HM26205010', 'HBL112490352', 'MBL135680442', 'REF-2601-0066',
    'FCIU5374744', 'TEU40HC', 40, 'HC',
    21626.0, 20564.0, 51.37, 182, 'cartons',
    'Santa Maria Foods', 'Qingdao Distribution Ltd.', 'Qingdao Distribution Ltd.', 'Diego Martins',
    'FOB', 'Prepaid', 'Pharmaceutical supplies', 'NEU-SA',
    NULL, 'PARTIALLY_RECEIVED', 'IN_PROGRESS', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9868364', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '696E'),
    (SELECT id FROM ports WHERE unlocode = 'NLRTM'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-01-06T05:00Z', '2026-03-31T19:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'HM26205010');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MK22345163', 'HBL966706625', 'MBL324472099', 'REF-2601-0067',
    'TRIU5107020', 'TEU40HC', 45, 'HC',
    16496.0, 15338.0, 25.57, 1086, 'crates',
    'Apex Chemicals Brasil', 'Norteuropa Procurement ApS', 'Norteuropa Procurement ApS', 'Bruna Lima',
    'EXW', 'Collect', 'Auto parts', 'Asia-SAEC',
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'), 'COMPLETE', 'IN_PROGRESS', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9622203', 'Via Singapore.',
    (SELECT id FROM voyages WHERE voyage_number = '355N'),
    (SELECT id FROM ports WHERE unlocode = 'CNSHA'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'ARRIVED',
    '2026-01-23T19:00Z', '2026-03-22T22:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MK22345163');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'ON85920407', 'HBL930407859', 'MBL321334803', 'REF-2602-0068',
    'FCIU5642050', 'TEU40HC', 45, 'HC',
    28129.0, 26719.0, 31.38, 1014, 'pallets',
    'Santa Maria Foods', 'Buenos Aires Wholesale SA', 'Buenos Aires Wholesale SA', 'Marina Rocha',
    'FOB', 'Prepaid', 'Solar components', 'LatAm Pacific',
    NULL, 'COMPLETE', 'IN_PROGRESS', 'MEDIUM', 3,
    'https://www.vesselfinder.com/vessels/details/9475636', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '138N'),
    (SELECT id FROM ports WHERE unlocode = 'PABLB'),
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-02-23T05:00Z', '2026-03-30T21:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'ON85920407');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MS10091737', 'HBL002921851', 'MBL569154522', 'REF-2601-0069',
    'HLBU9325254', 'REEFER40', 40, 'RF',
    12672.0, 12009.0, 53.63, 1183, 'cartons',
    'Global Rubber Brasil', 'Tanger Industrial Supply', 'Tanger Industrial Supply', 'Marina Rocha',
    'CIF', 'Collect', 'Pharmaceutical supplies', 'Far East-SA',
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'), 'PARTIALLY_RECEIVED', 'NOT_STARTED', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9327798', 'Via Singapore.',
    (SELECT id FROM voyages WHERE voyage_number = '500N'),
    (SELECT id FROM ports WHERE unlocode = 'CNSHA'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'BOOKED',
    '2026-01-04T14:00Z', '2026-01-08T00:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MS10091737');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'HM76596831', 'HBL157043315', 'MBL410067626', 'REF-2601-0070',
    'CMAU5135528', 'REEFER40', 40, 'RF',
    9594.0, 8691.0, 16.06, 544, 'cartons',
    'Santa Maria Foods', 'Buenos Aires Wholesale SA', 'Buenos Aires Wholesale SA', 'Diego Martins',
    'CFR', 'Prepaid', 'Frozen poultry', 'Far East-SA',
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'), 'COMPLETE', 'IN_PROGRESS', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9637246', 'Via Singapore.',
    (SELECT id FROM voyages WHERE voyage_number = '207N'),
    (SELECT id FROM ports WHERE unlocode = 'KRPUS'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'ARRIVED',
    '2026-01-28T09:00Z', '2026-03-22T21:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'HM76596831');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MS22496299', 'HBL962015336', 'MBL884443106', 'REF-2603-0071',
    'HLBU9970622', 'TEU20', 20, 'GP',
    9343.0, 8620.0, 17.9, 607, 'cartons',
    'SP Consumer Goods', 'Dubai Market Connect FZE', 'Dubai Market Connect FZE', 'Marina Rocha',
    'FOB', 'Prepaid', 'Home appliances', 'Africa-SA',
    NULL, 'PENDING', 'NOT_STARTED', 'MEDIUM', 0,
    'https://www.vesselfinder.com/vessels/details/9196864', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '183W'),
    (SELECT id FROM ports WHERE unlocode = 'ZADUR'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'CONFIRMED',
    '2026-03-11T01:00Z', '2026-03-31T06:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MS22496299');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MK45568288', 'HBL270477434', 'MBL902115869', 'REF-2601-0072',
    'MAEU8625650', 'TEU40HC', 45, 'HC',
    28452.0, 26983.0, 62.72, 1180, 'cartons',
    'SP Consumer Goods', 'Rioplate Logistics SA', 'Rioplate Logistics SA', 'Bruna Lima',
    'FOB', 'Prepaid', 'Textiles', 'Americas Loop',
    NULL, 'PARTIALLY_RECEIVED', 'CLEARED', 'MEDIUM', 3,
    'https://www.vesselfinder.com/vessels/details/9342176', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '793S'),
    (SELECT id FROM ports WHERE unlocode = 'USNYC'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-01-09T23:00Z', '2026-03-29T23:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MK45568288');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'HM15727862', 'HBL511686854', 'MBL870998616', 'REF-2601-0073',
    'TRHU1632942', 'TEU40', 40, 'GP',
    24411.0, 22940.0, 47.27, 86, 'crates',
    'Atlantic Coffee Exportadora', 'Chile Trade Partners SpA', 'Chile Trade Partners SpA', 'Diego Martins',
    'EXW', 'Prepaid', 'Auto parts', 'NEU-SA',
    NULL, 'COMPLETE', 'IN_PROGRESS', 'MEDIUM', 0,
    'https://www.vesselfinder.com/vessels/details/9868364', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '276E'),
    (SELECT id FROM ports WHERE unlocode = 'DEHAM'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'GATE_IN',
    '2026-01-02T09:00Z', '2026-04-01T20:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'HM15727862');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MS41887353', 'HBL447132475', 'MBL469137367', 'REF-2602-0074',
    'MSCU7450541', 'TEU40HC', 45, 'HC',
    18316.0, 17605.0, 59.36, 520, 'pallets',
    'Oceanic Paper & Pulp', 'Rioplate Logistics SA', 'Rioplate Logistics SA', 'Diego Martins',
    'FOB', 'Prepaid', 'Solar components', 'Far East-SA',
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'), 'PENDING', 'NOT_STARTED', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9327798', 'Via Singapore.',
    (SELECT id FROM voyages WHERE voyage_number = '133W'),
    (SELECT id FROM ports WHERE unlocode = 'CNSHA'),
    (SELECT id FROM ports WHERE unlocode = 'UYMVD'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'BOOKED',
    '2026-02-25T10:00Z', '2026-02-26T16:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MS41887353');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'HM77796914', 'HBL727985524', 'MBL140602933', 'REF-2601-0075',
    'TRHU1035799', 'TEU40HC', 40, 'HC',
    28434.0, 27509.0, 52.78, 573, 'bundles',
    'Nova Energia Equipamentos', 'Southern Cross Forwarding', 'Southern Cross Forwarding', 'Ana Souza',
    'CFR', 'Prepaid', 'Timber products', 'Pacific-SA',
    (SELECT id FROM ports WHERE unlocode = 'PABLB'), 'PARTIALLY_RECEIVED', 'IN_PROGRESS', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9625530', 'Via Balboa.',
    (SELECT id FROM voyages WHERE voyage_number = '533W'),
    (SELECT id FROM ports WHERE unlocode = 'MXLZC'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'LOADED',
    '2026-01-23T20:00Z', '2026-04-01T08:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'HM77796914');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MK79878835', 'HBL589944171', 'MBL545444328', 'REF-2602-0076',
    'CAXU9691083', 'TEU40HC', 40, 'HC',
    27604.0, 25854.0, 67.72, 398, 'cartons',
    'Brasil Foods Export SA', 'Cartagena Cargo SAS', 'Cartagena Cargo SAS', 'Bruna Lima',
    'DAP', 'Collect', 'Textiles', 'Asia-SA',
    NULL, 'COMPLETE', 'CLEARED', 'MEDIUM', 2,
    'https://www.vesselfinder.com/vessels/details/9298698', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '451S'),
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-02-04T15:00Z', '2026-03-29T06:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MK79878835');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MK50291467', 'HBL248251353', 'MBL465549471', 'REF-2603-0077',
    'CAXU2824179', 'TEU40HC', 40, 'HC',
    18276.0, 17130.0, 17.87, 318, 'cartons',
    'Brasil Foods Export SA', 'Chile Trade Partners SpA', 'Chile Trade Partners SpA', 'Diego Martins',
    'CFR', 'Prepaid', 'Home appliances', 'Americas Loop',
    NULL, 'COMPLETE', 'IN_PROGRESS', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9342176', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '240S'),
    (SELECT id FROM ports WHERE unlocode = 'USNYC'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'ARRIVED',
    '2026-03-07T21:00Z', '2026-03-27T18:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MK50291467');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MK34207075', 'HBL074329271', 'MBL296666760', 'REF-2601-0078',
    'TXGU1752504', 'TEU40HC', 40, 'HC',
    15914.0, 14194.0, 61.48, 649, 'cartons',
    'SP Consumer Goods', 'Mediterranean Foods SARL', 'Mediterranean Foods SARL', 'Ana Souza',
    'EXW', 'Collect', 'Refrigerated fruit', 'Florida Express',
    NULL, 'COMPLETE', 'NOT_STARTED', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9348649', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '232N'),
    (SELECT id FROM ports WHERE unlocode = 'USMIA'),
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'CONFIRMED',
    '2026-01-29T12:00Z', '2026-04-07T13:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MK34207075');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MK50024028', 'HBL257532603', 'MBL323098215', 'REF-2603-0079',
    'OOLU7898563', 'TEU20', 20, 'GP',
    22156.0, 20947.0, 21.46, 1130, 'cartons',
    'Santa Maria Foods', 'Southern Cross Forwarding', 'Southern Cross Forwarding', 'Diego Martins',
    'CIF', 'Prepaid', 'Frozen poultry', 'Florida Express',
    NULL, 'COMPLETE', 'NOT_STARTED', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9348649', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '358W'),
    (SELECT id FROM ports WHERE unlocode = 'USMIA'),
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'CONFIRMED',
    '2026-03-10T17:00Z', '2026-04-02T14:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MK50024028');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'HM23553285', 'HBL189141173', 'MBL934835642', 'REF-2601-0080',
    'DFSU8499266', 'TEU40HC', 45, 'HC',
    14459.0, 12678.0, 53.35, 534, 'cartons',
    'SP Consumer Goods', 'Dubai Market Connect FZE', 'Dubai Market Connect FZE', 'Marina Rocha',
    'EXW', 'Prepaid', 'Textiles', 'Pacific-SA',
    (SELECT id FROM ports WHERE unlocode = 'PABLB'), 'PARTIALLY_RECEIVED', 'IN_PROGRESS', 'MEDIUM', 0,
    'https://www.vesselfinder.com/vessels/details/9625530', 'Via Balboa.',
    (SELECT id FROM voyages WHERE voyage_number = '759S'),
    (SELECT id FROM ports WHERE unlocode = 'MXLZC'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'LOADED',
    '2026-01-15T02:00Z', '2026-04-02T03:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'HM23553285');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MK15294153', 'HBL002255794', 'MBL155216674', 'REF-2602-0081',
    'CAXU4556115', 'TEU20', 20, 'GP',
    27857.0, 26569.0, 28.64, 907, 'crates',
    'Global Rubber Brasil', 'Sakura Imports Co.', 'Sakura Imports Co.', 'Ana Souza',
    'CFR', 'Collect', 'Auto parts', 'Asia-SA',
    NULL, 'PENDING', 'NOT_STARTED', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9298698', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '285W'),
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'BOOKED',
    '2026-02-24T09:00Z', '2026-02-25T23:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MK15294153');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'LI65201639', 'HBL993588916', 'MBL375628900', 'REF-2602-0082',
    'TEMU0145083', 'TEU40HC', 45, 'HC',
    19700.0, 19239.0, 39.33, 1183, 'bags',
    'Mercosul Machinery', 'Dubai Market Connect FZE', 'Dubai Market Connect FZE', 'Bruna Lima',
    'EXW', 'Prepaid', 'Green coffee beans', 'Mercosur Shuttle',
    NULL, 'PARTIALLY_RECEIVED', 'CLEARED', 'MEDIUM', 1,
    'https://www.vesselfinder.com/vessels/details/9571296', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '558N'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    (SELECT id FROM ports WHERE unlocode = 'UYMVD'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-02-24T10:00Z', '2026-03-30T00:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'LI65201639');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'HM64148754', 'HBL948837550', 'MBL375899757', 'REF-2602-0083',
    'TRHU0880190', 'TEU20', 20, 'GP',
    16832.0, 15281.0, 18.33, 257, 'bags',
    'Mercurio Commodities SA', 'Patagonia Retail SA', 'Patagonia Retail SA', 'Kauê Santana',
    'CIF', 'Prepaid', 'Green coffee beans', 'Far East-SA',
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'), 'PARTIALLY_RECEIVED', 'CLEARED', 'MEDIUM', 1,
    'https://www.vesselfinder.com/vessels/details/9637246', 'Via Singapore.',
    (SELECT id FROM voyages WHERE voyage_number = '972W'),
    (SELECT id FROM ports WHERE unlocode = 'KRPUS'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-02-02T01:00Z', '2026-03-31T11:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'HM64148754');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MK65629268', 'HBL270808697', 'MBL829221699', 'REF-2602-0084',
    'MSCU9177987', 'TEU40HC', 40, 'HC',
    15621.0, 14910.0, 26.11, 605, 'bags',
    'Mercosul Machinery', 'Montevideo Trading House', 'Montevideo Trading House', 'Bruna Lima',
    'CIF', 'Collect', 'Green coffee beans', 'Med-SA',
    NULL, 'COMPLETE', 'HOLD', 'CRITICAL', 5,
    'https://www.vesselfinder.com/vessels/details/9332975', 'Shipment flagged for delay follow-up.',
    (SELECT id FROM voyages WHERE voyage_number = '803E'),
    (SELECT id FROM ports WHERE unlocode = 'MAPTM'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-02-14T04:00Z', '2026-03-31T06:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MK65629268');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'HM95480242', 'HBL684608106', 'MBL200342442', 'REF-2603-0085',
    'HLBU1587999', 'REEFER40', 40, 'RF',
    21199.0, 20046.0, 65.71, 650, 'cartons',
    'Rio Sul Auto Parts', 'Mediterranean Foods SARL', 'Mediterranean Foods SARL', 'Diego Martins',
    'FOB', 'Collect', 'Refrigerated fruit', 'India-SA',
    NULL, 'COMPLETE', 'IN_PROGRESS', 'MEDIUM', 3,
    'https://www.vesselfinder.com/vessels/details/9385013', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '665N'),
    (SELECT id FROM ports WHERE unlocode = 'LKCMB'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-03-13T01:00Z', '2026-03-28T18:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'HM95480242');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MK95065651', 'HBL472122816', 'MBL064842294', 'REF-2602-0086',
    'BMOU5989334', 'TEU40HC', 45, 'HC',
    28074.0, 26602.0, 44.16, 159, 'bales',
    'LatAm Pharma Logistics', 'Qingdao Distribution Ltd.', 'Qingdao Distribution Ltd.', 'Ana Souza',
    'DAP', 'Prepaid', 'Pulp bales', 'Asia-SA',
    NULL, 'COMPLETE', 'CLEARED', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9298698', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '902E'),
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'ARRIVED',
    '2026-02-26T11:00Z', '2026-03-28T21:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MK95065651');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'HM65825346', 'HBL555439878', 'MBL015739930', 'REF-2602-0087',
    'FCIU8615336', 'REEFER40', 40, 'RF',
    18213.0, 16579.0, 25.2, 1062, 'cartons',
    'Santa Maria Foods', 'Hamburg Distribution GmbH', 'Hamburg Distribution GmbH', 'Bruna Lima',
    'CIF', 'Collect', 'Frozen poultry', 'NEU-SA',
    NULL, 'PARTIALLY_RECEIVED', 'NOT_STARTED', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9868364', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '184N'),
    (SELECT id FROM ports WHERE unlocode = 'BEANR'),
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'BOOKED',
    '2026-02-12T19:00Z', '2026-02-15T21:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'HM65825346');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'CC99970857', 'HBL120750110', 'MBL248193466', 'REF-2603-0088',
    'DFSU3613864', 'TEU20', 20, 'GP',
    10878.0, 9846.0, 30.78, 227, 'bales',
    'Santa Maria Foods', 'Sakura Imports Co.', 'Sakura Imports Co.', 'Felipe Costa',
    'DAP', 'Prepaid', 'Pulp bales', 'Med-SA',
    NULL, 'COMPLETE', 'CLEARED', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9882499', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '396E'),
    (SELECT id FROM ports WHERE unlocode = 'MAPTM'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'ARRIVED',
    '2026-03-15T00:00Z', '2026-03-29T04:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'CC99970857');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MS14136476', 'HBL424508821', 'MBL009968814', 'REF-2601-0089',
    'TEMU2832685', 'TEU40HC', 45, 'HC',
    27114.0, 25494.0, 44.13, 605, 'cartons',
    'Oceanic Paper & Pulp', 'Mediterranean Foods SARL', 'Mediterranean Foods SARL', 'Bruna Lima',
    'CFR', 'Prepaid', 'Textiles', 'Asia-SA',
    NULL, 'PARTIALLY_RECEIVED', 'IN_PROGRESS', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9606314', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '787N'),
    (SELECT id FROM ports WHERE unlocode = 'CNSHA'),
    (SELECT id FROM ports WHERE unlocode = 'UYMVD'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'LOADED',
    '2026-01-11T08:00Z', '2026-04-02T07:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MS14136476');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MK57934299', 'HBL577594929', 'MBL955620533', 'REF-2602-0090',
    'CMAU3149137', 'TEU40HC', 45, 'HC',
    16587.0, 15184.0, 25.41, 166, 'pallets',
    'Parana Timber Exports', 'Buenos Aires Wholesale SA', 'Buenos Aires Wholesale SA', 'Marina Rocha',
    'FOB', 'Collect', 'Solar components', 'Americas Loop',
    NULL, 'PARTIALLY_RECEIVED', 'IN_PROGRESS', 'LOW', 1,
    'https://www.vesselfinder.com/vessels/details/9332987', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '844N'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM ports WHERE unlocode = 'USORF'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-02-02T21:00Z', '2026-03-28T04:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MK57934299');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'HM89193333', 'HBL256394278', 'MBL816580392', 'REF-2602-0091',
    'TCLU0848875', 'REEFER40', 40, 'RF',
    17162.0, 16106.0, 27.4, 433, 'cartons',
    'Atlantic Coffee Exportadora', 'Qingdao Distribution Ltd.', 'Qingdao Distribution Ltd.', 'Marina Rocha',
    'EXW', 'Prepaid', 'Pharmaceutical supplies', 'India-SA',
    NULL, 'COMPLETE', 'IN_PROGRESS', 'LOW', 2,
    'https://www.vesselfinder.com/vessels/details/9385013', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '630E'),
    (SELECT id FROM ports WHERE unlocode = 'LKCMB'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-02-22T18:00Z', '2026-03-29T14:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'HM89193333');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MS79977486', 'HBL665918463', 'MBL865577908', 'REF-2603-0092',
    'DFSU4498929', 'TEU40HC', 40, 'HC',
    28227.0, 26458.0, 58.3, 481, 'bags',
    'Andes Fresh Produce', 'Asia Pacific Sourcing Pte Ltd', 'Asia Pacific Sourcing Pte Ltd', 'Marina Rocha',
    'FOB', 'Prepaid', 'Green coffee beans', 'Far East-SA',
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'), 'PENDING', 'NOT_STARTED', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9282261', 'Via Singapore.',
    (SELECT id FROM voyages WHERE voyage_number = '524E'),
    (SELECT id FROM ports WHERE unlocode = 'KRPUS'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'BOOKED',
    '2026-03-07T20:00Z', '2026-03-09T08:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MS79977486');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'ON31642106', 'HBL922809202', 'MBL428699041', 'REF-2601-0093',
    'DFSU6030290', 'TEU40', 40, 'GP',
    23359.0, 21838.0, 46.3, 480, 'crates',
    'Andes Fresh Produce', 'Cartagena Cargo SAS', 'Cartagena Cargo SAS', 'Bruna Lima',
    'CIF', 'Prepaid', 'Industrial machinery', 'Far East-SA',
    (SELECT id FROM ports WHERE unlocode = 'PABLB'), 'COMPLETE', 'IN_PROGRESS', 'MEDIUM', 3,
    'https://www.vesselfinder.com/vessels/details/9588079', 'Via Balboa.',
    (SELECT id FROM voyages WHERE voyage_number = '487W'),
    (SELECT id FROM ports WHERE unlocode = 'CNYTN'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-01-15T07:00Z', '2026-03-30T04:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'ON31642106');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'LI66343932', 'HBL908811729', 'MBL806865086', 'REF-2512-0094',
    'CAXU2912794', 'REEFER40', 40, 'RF',
    14693.0, 14382.0, 52.58, 24, 'cartons',
    'Global Rubber Brasil', 'Montevideo Trading House', 'Montevideo Trading House', 'Felipe Costa',
    'DAP', 'Prepaid', 'Refrigerated fruit', 'Mercosur Shuttle',
    NULL, 'COMPLETE', 'CLEARED', 'LOW', 1,
    'https://www.vesselfinder.com/vessels/details/9571296', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '691N'),
    (SELECT id FROM ports WHERE unlocode = 'BRPNG'),
    (SELECT id FROM ports WHERE unlocode = 'UYMVD'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2025-12-31T13:00Z', '2026-03-28T14:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'LI66343932');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'CC58623028', 'HBL695946368', 'MBL872588744', 'REF-2602-0095',
    'MAEU2694485', 'TEU40HC', 40, 'HC',
    15527.0, 14186.0, 36.23, 703, 'cartons',
    'Oceanic Paper & Pulp', 'Montevideo Trading House', 'Montevideo Trading House', 'Ana Souza',
    'CIF', 'Prepaid', 'Pharmaceutical supplies', 'Med-SA',
    NULL, 'COMPLETE', 'CLEARED', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9951525', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '920N'),
    (SELECT id FROM ports WHERE unlocode = 'FOSFM'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'ARRIVED',
    '2026-02-27T15:00Z', '2026-03-29T07:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'CC58623028');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'HM90563579', 'HBL906698490', 'MBL277038430', 'REF-2602-0096',
    'TRHU1143514', 'TEU40', 40, 'GP',
    13659.0, 13182.0, 67.83, 828, 'bundles',
    'Mercosul Machinery', 'Pacific Mercantile Inc.', 'Pacific Mercantile Inc.', 'Ana Souza',
    'EXW', 'Collect', 'Timber products', 'India-SA',
    NULL, 'COMPLETE', 'NOT_STARTED', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9385013', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '461W'),
    (SELECT id FROM ports WHERE unlocode = 'LKCMB'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'CONFIRMED',
    '2026-02-15T17:00Z', '2026-04-02T11:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'HM90563579');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MK83765077', 'HBL937690426', 'MBL818184518', 'REF-2601-0097',
    'OOLU8955436', 'TEU20', 20, 'GP',
    24293.0, 23755.0, 19.38, 793, 'bags',
    'SP Consumer Goods', 'Chile Trade Partners SpA', 'Chile Trade Partners SpA', 'Felipe Costa',
    'CIF', 'Prepaid', 'Green coffee beans', 'Americas Loop',
    NULL, 'PARTIALLY_RECEIVED', 'NOT_STARTED', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9332987', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '748S'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM ports WHERE unlocode = 'USORF'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'BOOKED',
    '2026-01-20T00:00Z', '2026-01-23T01:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MK83765077');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MK45346197', 'HBL275539423', 'MBL733343333', 'REF-2603-0098',
    'TCLU4165183', 'REEFER40', 40, 'RF',
    24582.0, 23908.0, 67.17, 1056, 'cartons',
    'Oceanic Paper & Pulp', 'Benelux Commodities NV', 'Benelux Commodities NV', 'Kauê Santana',
    'CIF', 'Collect', 'Frozen poultry', 'Africa-SA',
    NULL, 'COMPLETE', 'CLEARED', 'MEDIUM', 3,
    'https://www.vesselfinder.com/vessels/details/9333008', 'Direct service.',
    (SELECT id FROM voyages WHERE voyage_number = '386W'),
    (SELECT id FROM ports WHERE unlocode = 'ZADUR'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-03-02T10:00Z', '2026-03-31T06:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MK45346197');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MS82580645', 'HBL875003691', 'MBL559428434', 'REF-2602-0099',
    'MSCU8562715', 'TEU40HC', 45, 'HC',
    15272.0, 13496.0, 29.31, 492, 'bundles',
    'Green Harvest Trading', 'Tanger Industrial Supply', 'Tanger Industrial Supply', 'Felipe Costa',
    'EXW', 'Prepaid', 'Timber products', 'Far East-SAEC',
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'), 'PARTIALLY_RECEIVED', 'IN_PROGRESS', 'MEDIUM', 1,
    'https://www.vesselfinder.com/vessels/details/9963580', 'Via Singapore.',
    (SELECT id FROM voyages WHERE voyage_number = '668W'),
    (SELECT id FROM ports WHERE unlocode = 'CNSHA'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'IN_TRANSIT',
    '2026-02-21T21:00Z', '2026-03-28T12:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MS82580645');

INSERT INTO shipments (id, booking, house_bl, master_bl, customer_reference,
    container_number, container_type, container_size_ft, container_iso_code,
    gross_weight_kg, net_weight_kg, volume_cbm, packages, package_type,
    shipper, consignee, notify_party, operator_name,
    incoterm, freight_term, cargo_description, service_lane,
    transshipment_port_id, document_status, customs_status, risk_level, delay_days,
    vessel_source_url, notes,
    voyage_id, origin_port_id, destination_port_id, tenant_id, status,
    created_at, updated_at)
SELECT gen_random_uuid(),
    'MS33703827', 'HBL448906120', 'MBL347175455', 'REF-2603-0100',
    'TRHU7288578', 'TEU40HC', 45, 'HC',
    22652.0, 20974.0, 42.05, 925, 'cartons',
    'SP Consumer Goods', 'Southern Cross Forwarding', 'Southern Cross Forwarding', 'Felipe Costa',
    'DAP', 'Prepaid', 'Frozen poultry', 'Far East-SAEC',
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'), 'PARTIALLY_RECEIVED', 'NOT_STARTED', 'LOW', 0,
    'https://www.vesselfinder.com/vessels/details/9963580', 'Via Singapore.',
    (SELECT id FROM voyages WHERE voyage_number = '180W'),
    (SELECT id FROM ports WHERE unlocode = 'KRPUS'),
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'CONFIRMED',
    '2026-03-14T23:00Z', '2026-03-30T12:00Z'
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MS33703827');

-- ==================== EVENTS ====================
INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'HM39958838'),
    'GATE_IN', 'Colombo', 'Booking Confirmed - Colombo',
    '2026-02-04T09:00Z', '2026-02-04T18:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'HM39958838' AND e.type = 'GATE_IN'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MK19335534'),
    'DEPARTED', 'At Sea', 'Departed - At Sea',
    '2026-03-26T14:00Z', '2026-03-27T03:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MK19335534' AND e.type = 'DEPARTED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MS78387461'),
    'LOADED', 'Durban', 'Loaded on Vessel - Durban',
    '2026-04-01T08:00Z', '2026-04-01T19:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MS78387461' AND e.type = 'LOADED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'CC81498611'),
    'GATE_IN', 'Los Angeles', 'Space Confirmed - Los Angeles',
    '2026-04-02T11:00Z', '2026-04-02T22:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'CC81498611' AND e.type = 'GATE_IN'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'HM22660194'),
    'DEPARTED', 'At Sea', 'Departed - At Sea',
    '2026-03-27T21:00Z', '2026-03-28T03:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'HM22660194' AND e.type = 'DEPARTED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'HM39219319'),
    'GATE_IN', 'Lazaro Cardenas', 'Space Confirmed - Lazaro Cardenas',
    '2026-04-09T07:00Z', '2026-04-09T18:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'HM39219319' AND e.type = 'GATE_IN'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MS77898694'),
    'GATE_IN', 'Busan', 'Booking Confirmed - Busan',
    '2026-02-11T20:00Z', '2026-02-12T05:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MS77898694' AND e.type = 'GATE_IN'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MS20099059'),
    'TRANSSHIPMENT', 'Singapore', 'Transshipment - Singapore',
    '2026-03-26T09:00Z', '2026-03-26T17:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MS20099059' AND e.type = 'TRANSSHIPMENT'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'HM96098221'),
    'GATE_IN', 'Busan', 'Booking Confirmed - Busan',
    '2026-02-22T01:00Z', '2026-02-22T11:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'HM96098221' AND e.type = 'GATE_IN'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MK33357554'),
    'ARRIVED', 'Paranagua', 'Arrived at Destination - Paranagua',
    '2026-03-29T14:00Z', '2026-03-29T17:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MK33357554' AND e.type = 'ARRIVED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'CC67062156'),
    'ARRIVED', 'Santos', 'Arrived at Destination - Santos',
    '2026-03-23T06:00Z', '2026-03-23T14:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'CC67062156' AND e.type = 'ARRIVED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'CC65682626'),
    'DEPARTED', 'At Sea', 'Vessel Delay - At Sea',
    '2026-03-28T12:00Z', '2026-03-28T13:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'CC65682626' AND e.type = 'DEPARTED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'HM60885459'),
    'DEPARTED', 'At Sea', 'Departed - At Sea',
    '2026-03-28T00:00Z', '2026-03-28T08:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'HM60885459' AND e.type = 'DEPARTED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MK55799273'),
    'GATE_IN', 'Durban', 'Booking Confirmed - Durban',
    '2026-01-29T02:00Z', '2026-01-29T09:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MK55799273' AND e.type = 'GATE_IN'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MK84514489'),
    'DEPARTED', 'At Sea', 'Vessel Delay - At Sea',
    '2026-03-29T12:00Z', '2026-03-29T22:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MK84514489' AND e.type = 'DEPARTED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MK75884623'),
    'ARRIVED', 'Antwerp', 'Arrived at Destination - Antwerp',
    '2026-03-24T10:00Z', '2026-03-25T00:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MK75884623' AND e.type = 'ARRIVED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'ON84087145'),
    'GATE_IN', 'Shanghai', 'Space Confirmed - Shanghai',
    '2026-04-06T16:00Z', '2026-04-07T09:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'ON84087145' AND e.type = 'GATE_IN'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'LI12784407'),
    'GATE_IN', 'Santos', 'Gate In - Santos',
    '2026-04-02T23:00Z', '2026-04-03T01:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'LI12784407' AND e.type = 'GATE_IN'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MK75056916'),
    'LOADED', 'Singapore', 'Loaded on Vessel - Singapore',
    '2026-04-01T13:00Z', '2026-04-01T23:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MK75056916' AND e.type = 'LOADED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'CC42051937'),
    'LOADED', 'Algeciras', 'Loaded on Vessel - Algeciras',
    '2026-04-01T08:00Z', '2026-04-01T16:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'CC42051937' AND e.type = 'LOADED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'HM17532195'),
    'GATE_IN', 'Lazaro Cardenas', 'Booking Confirmed - Lazaro Cardenas',
    '2026-01-09T07:00Z', '2026-01-09T16:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'HM17532195' AND e.type = 'GATE_IN'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'CC49625201'),
    'DEPARTED', 'At Sea', 'Departed - At Sea',
    '2026-03-27T04:00Z', '2026-03-27T14:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'CC49625201' AND e.type = 'DEPARTED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'ON82017516'),
    'DEPARTED', 'At Sea', 'Departed - At Sea',
    '2026-03-27T18:00Z', '2026-03-27T22:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'ON82017516' AND e.type = 'DEPARTED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MS59302863'),
    'LOADED', 'Busan', 'Loaded on Vessel - Busan',
    '2026-03-30T02:00Z', '2026-03-30T14:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MS59302863' AND e.type = 'LOADED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MK22762205'),
    'DEPARTED', 'At Sea', 'Departed - At Sea',
    '2026-03-25T09:00Z', '2026-03-25T18:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MK22762205' AND e.type = 'DEPARTED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MK34347841'),
    'LOADED', 'Singapore', 'Loaded on Vessel - Singapore',
    '2026-03-30T14:00Z', '2026-03-30T18:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MK34347841' AND e.type = 'LOADED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'CC38351344'),
    'GATE_IN', 'Fos-sur-Mer', 'Booking Confirmed - Fos-sur-Mer',
    '2026-01-25T03:00Z', '2026-01-25T18:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'CC38351344' AND e.type = 'GATE_IN'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'ON55568701'),
    'DEPARTED', 'At Sea', 'Departed - At Sea',
    '2026-03-26T03:00Z', '2026-03-26T20:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'ON55568701' AND e.type = 'DEPARTED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'HM61604364'),
    'ARRIVED', 'Santos', 'Arrived at Destination - Santos',
    '2026-03-24T13:00Z', '2026-03-25T00:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'HM61604364' AND e.type = 'ARRIVED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'HM51638598'),
    'TRANSSHIPMENT', 'Singapore', 'Transshipment - Singapore',
    '2026-03-28T08:00Z', '2026-03-28T23:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'HM51638598' AND e.type = 'TRANSSHIPMENT'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'CC59370777'),
    'LOADED', 'Tanger Med', 'Loaded on Vessel - Tanger Med',
    '2026-03-31T06:00Z', '2026-03-31T19:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'CC59370777' AND e.type = 'LOADED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MK72962895'),
    'DEPARTED', 'At Sea', 'Departed - At Sea',
    '2026-03-28T12:00Z', '2026-03-29T02:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MK72962895' AND e.type = 'DEPARTED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'ON95318289'),
    'LOADED', 'Yokohama', 'Loaded on Vessel - Yokohama',
    '2026-03-30T20:00Z', '2026-03-30T22:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'ON95318289' AND e.type = 'LOADED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'HM79696025'),
    'DEPARTED', 'At Sea', 'Departed - At Sea',
    '2026-03-28T11:00Z', '2026-03-28T23:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'HM79696025' AND e.type = 'DEPARTED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'ON21571646'),
    'DEPARTED', 'At Sea', 'Departed - At Sea',
    '2026-03-29T10:00Z', '2026-03-29T15:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'ON21571646' AND e.type = 'DEPARTED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MS91220369'),
    'ARRIVED', 'Santos', 'Arrived at Destination - Santos',
    '2026-03-28T14:00Z', '2026-03-29T02:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MS91220369' AND e.type = 'ARRIVED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MS28908276'),
    'LOADED', 'Busan', 'Loaded on Vessel - Busan',
    '2026-03-31T09:00Z', '2026-03-31T14:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MS28908276' AND e.type = 'LOADED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MK19869264'),
    'LOADED', 'Miami', 'Loaded on Vessel - Miami',
    '2026-03-30T17:00Z', '2026-03-31T10:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MK19869264' AND e.type = 'LOADED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'HM94457912'),
    'TRANSSHIPMENT', 'Singapore', 'Transshipment - Singapore',
    '2026-03-28T15:00Z', '2026-03-28T21:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'HM94457912' AND e.type = 'TRANSSHIPMENT'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MK40181142'),
    'LOADED', 'Norfolk', 'Loaded on Vessel - Norfolk',
    '2026-03-31T09:00Z', '2026-03-31T11:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MK40181142' AND e.type = 'LOADED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'HM97313825'),
    'ARRIVED', 'Paranagua', 'Arrived at Destination - Paranagua',
    '2026-03-22T20:00Z', '2026-03-23T07:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'HM97313825' AND e.type = 'ARRIVED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'HM92000924'),
    'DEPARTED', 'At Sea', 'Departed - At Sea',
    '2026-03-26T03:00Z', '2026-03-26T15:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'HM92000924' AND e.type = 'DEPARTED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'HM73360324'),
    'TRANSSHIPMENT', 'Balboa', 'Transshipment - Balboa',
    '2026-03-26T09:00Z', '2026-03-26T17:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'HM73360324' AND e.type = 'TRANSSHIPMENT'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MK39161379'),
    'DEPARTED', 'At Sea', 'Departed - At Sea',
    '2026-03-30T03:00Z', '2026-03-30T11:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MK39161379' AND e.type = 'DEPARTED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MK62132722'),
    'DEPARTED', 'At Sea', 'Departed - At Sea',
    '2026-03-29T19:00Z', '2026-03-30T10:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MK62132722' AND e.type = 'DEPARTED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'ON95594482'),
    'LOADED', 'Kobe', 'Loaded on Vessel - Kobe',
    '2026-04-01T09:00Z', '2026-04-01T12:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'ON95594482' AND e.type = 'LOADED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'LI96557984'),
    'GATE_IN', 'Paranagua', 'Space Confirmed - Paranagua',
    '2026-04-09T06:00Z', '2026-04-09T09:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'LI96557984' AND e.type = 'GATE_IN'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'CC78541804'),
    'GATE_IN', 'Singapore', 'Space Confirmed - Singapore',
    '2026-04-01T03:00Z', '2026-04-01T05:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'CC78541804' AND e.type = 'GATE_IN'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'HM67226329'),
    'GATE_IN', 'Antwerp', 'Space Confirmed - Antwerp',
    '2026-04-07T23:00Z', '2026-04-08T03:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'HM67226329' AND e.type = 'GATE_IN'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MK50987410'),
    'LOADED', 'Shanghai', 'Loaded on Vessel - Shanghai',
    '2026-03-31T13:00Z', '2026-04-01T05:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MK50987410' AND e.type = 'LOADED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'CC76479831'),
    'GATE_IN', 'Beirut', 'Space Confirmed - Beirut',
    '2026-04-09T17:00Z', '2026-04-10T05:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'CC76479831' AND e.type = 'GATE_IN'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MK47221734'),
    'GATE_IN', 'Singapore', 'Space Confirmed - Singapore',
    '2026-04-07T01:00Z', '2026-04-07T10:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MK47221734' AND e.type = 'GATE_IN'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'CC71684020'),
    'GATE_IN', 'Santos', 'Booking Confirmed - Santos',
    '2026-02-28T18:00Z', '2026-03-01T05:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'CC71684020' AND e.type = 'GATE_IN'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'ON50165370'),
    'GATE_IN', 'Yantian', 'Space Confirmed - Yantian',
    '2026-04-02T20:00Z', '2026-04-03T13:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'ON50165370' AND e.type = 'GATE_IN'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'CC89715904'),
    'GATE_IN', 'Fos-sur-Mer', 'Gate In - Fos-sur-Mer',
    '2026-04-06T06:00Z', '2026-04-06T09:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'CC89715904' AND e.type = 'GATE_IN'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'HM42029368'),
    'LOADED', 'Colombo', 'Loaded on Vessel - Colombo',
    '2026-04-01T02:00Z', '2026-04-01T08:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'HM42029368' AND e.type = 'LOADED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'ON16171536'),
    'GATE_IN', 'Yokohama', 'Booking Confirmed - Yokohama',
    '2026-02-05T10:00Z', '2026-02-05T20:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'ON16171536' AND e.type = 'GATE_IN'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MS82684966'),
    'TRANSSHIPMENT', 'Singapore', 'Transshipment - Singapore',
    '2026-03-25T02:00Z', '2026-03-25T17:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MS82684966' AND e.type = 'TRANSSHIPMENT'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'HM26205010'),
    'DEPARTED', 'At Sea', 'Departed - At Sea',
    '2026-03-31T12:00Z', '2026-03-31T19:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'HM26205010' AND e.type = 'DEPARTED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MK22345163'),
    'ARRIVED', 'Paranagua', 'Arrived at Destination - Paranagua',
    '2026-03-22T09:00Z', '2026-03-22T22:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MK22345163' AND e.type = 'ARRIVED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'ON85920407'),
    'DEPARTED', 'At Sea', 'Departed - At Sea',
    '2026-03-30T14:00Z', '2026-03-30T21:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'ON85920407' AND e.type = 'DEPARTED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MS10091737'),
    'GATE_IN', 'Shanghai', 'Booking Confirmed - Shanghai',
    '2026-01-07T14:00Z', '2026-01-08T00:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MS10091737' AND e.type = 'GATE_IN'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'HM76596831'),
    'ARRIVED', 'Santos', 'Arrived at Destination - Santos',
    '2026-03-22T18:00Z', '2026-03-22T21:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'HM76596831' AND e.type = 'ARRIVED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MS22496299'),
    'GATE_IN', 'Durban', 'Space Confirmed - Durban',
    '2026-03-31T02:00Z', '2026-03-31T06:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MS22496299' AND e.type = 'GATE_IN'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MK45568288'),
    'DEPARTED', 'At Sea', 'Departed - At Sea',
    '2026-03-29T21:00Z', '2026-03-29T23:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MK45568288' AND e.type = 'DEPARTED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'HM15727862'),
    'GATE_IN', 'Hamburg', 'Gate In - Hamburg',
    '2026-04-01T07:00Z', '2026-04-01T20:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'HM15727862' AND e.type = 'GATE_IN'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MS41887353'),
    'GATE_IN', 'Shanghai', 'Booking Confirmed - Shanghai',
    '2026-02-26T10:00Z', '2026-02-26T16:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MS41887353' AND e.type = 'GATE_IN'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'HM77796914'),
    'LOADED', 'Lazaro Cardenas', 'Loaded on Vessel - Lazaro Cardenas',
    '2026-03-31T23:00Z', '2026-04-01T08:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'HM77796914' AND e.type = 'LOADED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MK79878835'),
    'DEPARTED', 'At Sea', 'Departed - At Sea',
    '2026-03-29T04:00Z', '2026-03-29T06:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MK79878835' AND e.type = 'DEPARTED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MK50291467'),
    'ARRIVED', 'Paranagua', 'Arrived at Destination - Paranagua',
    '2026-03-27T01:00Z', '2026-03-27T18:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MK50291467' AND e.type = 'ARRIVED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MK34207075'),
    'GATE_IN', 'Miami', 'Space Confirmed - Miami',
    '2026-04-07T05:00Z', '2026-04-07T13:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MK34207075' AND e.type = 'GATE_IN'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MK50024028'),
    'GATE_IN', 'Miami', 'Space Confirmed - Miami',
    '2026-04-02T10:00Z', '2026-04-02T14:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MK50024028' AND e.type = 'GATE_IN'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'HM23553285'),
    'LOADED', 'Lazaro Cardenas', 'Loaded on Vessel - Lazaro Cardenas',
    '2026-04-01T10:00Z', '2026-04-02T03:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'HM23553285' AND e.type = 'LOADED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MK15294153'),
    'GATE_IN', 'Singapore', 'Booking Confirmed - Singapore',
    '2026-02-25T09:00Z', '2026-02-25T23:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MK15294153' AND e.type = 'GATE_IN'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'LI65201639'),
    'DEPARTED', 'At Sea', 'Departed - At Sea',
    '2026-03-29T11:00Z', '2026-03-30T00:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'LI65201639' AND e.type = 'DEPARTED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'HM64148754'),
    'DEPARTED', 'At Sea', 'Departed - At Sea',
    '2026-03-31T07:00Z', '2026-03-31T11:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'HM64148754' AND e.type = 'DEPARTED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MK65629268'),
    'DEPARTED', 'At Sea', 'Vessel Delay - At Sea',
    '2026-03-30T12:00Z', '2026-03-31T06:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MK65629268' AND e.type = 'DEPARTED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'HM95480242'),
    'DEPARTED', 'At Sea', 'Departed - At Sea',
    '2026-03-28T05:00Z', '2026-03-28T18:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'HM95480242' AND e.type = 'DEPARTED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MK95065651'),
    'ARRIVED', 'Santos', 'Arrived at Destination - Santos',
    '2026-03-28T11:00Z', '2026-03-28T21:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MK95065651' AND e.type = 'ARRIVED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'HM65825346'),
    'GATE_IN', 'Antwerp', 'Booking Confirmed - Antwerp',
    '2026-02-15T19:00Z', '2026-02-15T21:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'HM65825346' AND e.type = 'GATE_IN'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'CC99970857'),
    'ARRIVED', 'Paranagua', 'Arrived at Destination - Paranagua',
    '2026-03-29T02:00Z', '2026-03-29T04:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'CC99970857' AND e.type = 'ARRIVED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MS14136476'),
    'LOADED', 'Shanghai', 'Loaded on Vessel - Shanghai',
    '2026-04-01T13:00Z', '2026-04-02T07:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MS14136476' AND e.type = 'LOADED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MK57934299'),
    'DEPARTED', 'At Sea', 'Departed - At Sea',
    '2026-03-27T19:00Z', '2026-03-28T04:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MK57934299' AND e.type = 'DEPARTED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'HM89193333'),
    'DEPARTED', 'At Sea', 'Departed - At Sea',
    '2026-03-29T11:00Z', '2026-03-29T14:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'HM89193333' AND e.type = 'DEPARTED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MS79977486'),
    'GATE_IN', 'Busan', 'Booking Confirmed - Busan',
    '2026-03-08T20:00Z', '2026-03-09T08:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MS79977486' AND e.type = 'GATE_IN'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'ON31642106'),
    'DEPARTED', 'At Sea', 'Departed - At Sea',
    '2026-03-29T17:00Z', '2026-03-30T04:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'ON31642106' AND e.type = 'DEPARTED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'LI66343932'),
    'DEPARTED', 'At Sea', 'Departed - At Sea',
    '2026-03-28T05:00Z', '2026-03-28T14:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'LI66343932' AND e.type = 'DEPARTED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'CC58623028'),
    'ARRIVED', 'Santos', 'Arrived at Destination - Santos',
    '2026-03-29T03:00Z', '2026-03-29T07:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'CC58623028' AND e.type = 'ARRIVED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'HM90563579'),
    'GATE_IN', 'Colombo', 'Space Confirmed - Colombo',
    '2026-04-02T10:00Z', '2026-04-02T11:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'HM90563579' AND e.type = 'GATE_IN'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MK83765077'),
    'GATE_IN', 'Santos', 'Booking Confirmed - Santos',
    '2026-01-23T00:00Z', '2026-01-23T01:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MK83765077' AND e.type = 'GATE_IN'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MK45346197'),
    'DEPARTED', 'At Sea', 'Departed - At Sea',
    '2026-03-30T21:00Z', '2026-03-31T06:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MK45346197' AND e.type = 'DEPARTED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MS82580645'),
    'DEPARTED', 'At Sea', 'Departed - At Sea',
    '2026-03-28T04:00Z', '2026-03-28T12:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MS82580645' AND e.type = 'DEPARTED'
);

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(),
    (SELECT id FROM shipments WHERE booking = 'MS33703827'),
    'GATE_IN', 'Busan', 'Space Confirmed - Busan',
    '2026-03-30T00:00Z', '2026-03-30T12:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM events e JOIN shipments s ON e.shipment_id = s.id
    WHERE s.booking = 'MS33703827' AND e.type = 'GATE_IN'
);

