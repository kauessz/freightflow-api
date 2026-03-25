-- V10: Seed de portos reais com dados UNLOCODE
-- Fonte: https://unece.org/trade/cefact/UNLOCODE-Download

INSERT INTO ports (unlocode, name, country, timezone, latitude, longitude) VALUES
-- Brasil
('BRSSZ', 'Santos',            'BR', 'America/Sao_Paulo',    -23.9536, -46.3326),
('BRPNG', 'Paranagua',         'BR', 'America/Sao_Paulo',    -25.5151, -48.5225),
('BRRIG', 'Rio Grande',        'BR', 'America/Sao_Paulo',    -32.0350, -52.0986),
('BRIOA', 'Itapoa',            'BR', 'America/Sao_Paulo',    -26.1169, -48.6153),
('BRNVT', 'Navegantes',        'BR', 'America/Sao_Paulo',    -26.8983, -48.6544),
('BRVIX', 'Vitoria',           'BR', 'America/Sao_Paulo',    -20.3155, -40.2922),
('BRRIO', 'Rio de Janeiro',    'BR', 'America/Sao_Paulo',    -22.8839, -43.1729),
('BRSSA', 'Salvador',          'BR', 'America/Bahia',        -12.9714, -38.5124),
('BRPEC', 'Pecem',             'BR', 'America/Fortaleza',     -3.5329, -38.7907),
('BRSUA', 'Suape',             'BR', 'America/Recife',        -8.3941, -34.9616),
('BRMAO', 'Manaus',            'BR', 'America/Manaus',        -3.1190, -60.0217),
-- Argentina / Uruguai
('ARBUE', 'Buenos Aires',      'AR', 'America/Argentina/Buenos_Aires', -34.5997, -58.3733),
('UYMVD', 'Montevideo',        'UY', 'America/Montevideo',   -34.8941, -56.2122),
-- Europa
('NLRTM', 'Rotterdam',         'NL', 'Europe/Amsterdam',      51.9060,   4.4870),
('DEHAM', 'Hamburg',            'DE', 'Europe/Berlin',         53.5459,   9.9669),
('BEANR', 'Antwerp',           'BE', 'Europe/Brussels',       51.2329,   4.3856),
('GBFXT', 'Felixstowe',        'GB', 'Europe/London',         51.9536,   1.3057),
('ESVLC', 'Valencia',          'ES', 'Europe/Madrid',         39.4486,  -0.3244),
('ITGOA', 'Genoa',             'IT', 'Europe/Rome',           44.4094,   8.9268),
('FRLEH', 'Le Havre',          'FR', 'Europe/Paris',          49.4808,   0.1073),
('PTLEI', 'Leixoes',           'PT', 'Europe/Lisbon',         41.1852,  -8.7007),
('PTSIE', 'Sines',             'PT', 'Europe/Lisbon',         37.9511,  -8.8689),
-- Asia
('CNSHA', 'Shanghai',          'CN', 'Asia/Shanghai',         31.3465, 121.6096),
('CNNGB', 'Ningbo',            'CN', 'Asia/Shanghai',         29.8683, 121.5440),
('CNYTN', 'Yantian',           'CN', 'Asia/Shanghai',         22.5783, 114.2808),
('SGSIN', 'Singapore',         'SG', 'Asia/Singapore',         1.2650, 103.8226),
('KRPUS', 'Busan',             'KR', 'Asia/Seoul',            35.0799, 129.0571),
('JPYOK', 'Yokohama',          'JP', 'Asia/Tokyo',            35.4437, 139.6500),
('TWKHH', 'Kaohsiung',         'TW', 'Asia/Taipei',           22.6106, 120.2869),
('LKCMB', 'Colombo',           'LK', 'Asia/Colombo',           6.9545,  79.8448),
('INMUN', 'Mundra',            'IN', 'Asia/Kolkata',          22.7393,  69.7144),
('INNSA', 'Nhava Sheva',       'IN', 'Asia/Kolkata',          18.9500,  72.9500),
('MYTPP', 'Tanjung Pelepas',   'MY', 'Asia/Kuala_Lumpur',      1.3631, 103.5551),
-- Oriente Medio / Africa
('AEJEA', 'Jebel Ali',         'AE', 'Asia/Dubai',           25.0063,  55.0589),
('EGPSD', 'Port Said',         'EG', 'Africa/Cairo',         31.2653,  32.3019),
('ZADUR', 'Durban',            'ZA', 'Africa/Johannesburg',  -29.8587,  31.0218),
('MAPTM', 'Tanger Med',        'MA', 'Africa/Casablanca',    35.8847,  -5.5000),
-- America do Norte
('USLAX', 'Los Angeles',       'US', 'America/Los_Angeles',   33.7361, -118.2631),
('USLGB', 'Long Beach',        'US', 'America/Los_Angeles',   33.7544, -118.2167),
('USNYC', 'New York',          'US', 'America/New_York',      40.6680, -74.0384),
('USSAV', 'Savannah',          'US', 'America/New_York',      32.0809, -81.0912),
('USHOU', 'Houston',           'US', 'America/Chicago',       29.7260, -95.2691),
-- Oceania
('AUMEL', 'Melbourne',         'AU', 'Australia/Melbourne',  -37.8256, 144.9416),
('AUSYD', 'Sydney',            'AU', 'Australia/Sydney',     -33.8523, 151.2108);
