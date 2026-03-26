-- V10: Seed de portos reais com dados UNLOCODE
-- Fonte: https://unece.org/trade/cefact/UNLOCODE-Download

INSERT INTO ports (id, unlocode, name, country, timezone, latitude, longitude, created_at) VALUES
-- Brasil
(gen_random_uuid(), 'BRSSZ', 'Santos', 'BR', 'America/Sao_Paulo', -23.9536, -46.3326, NOW()),
(gen_random_uuid(), 'BRPNG', 'Paranagua', 'BR', 'America/Sao_Paulo', -25.5151, -48.5225, NOW()),
(gen_random_uuid(), 'BRRIG', 'Rio Grande', 'BR', 'America/Sao_Paulo', -32.0350, -52.0986, NOW()),
(gen_random_uuid(), 'BRIOA', 'Itapoa', 'BR', 'America/Sao_Paulo', -26.1169, -48.6153, NOW()),
(gen_random_uuid(), 'BRNVT', 'Navegantes', 'BR', 'America/Sao_Paulo', -26.8983, -48.6544, NOW()),
(gen_random_uuid(), 'BRVIX', 'Vitoria', 'BR', 'America/Sao_Paulo', -20.3155, -40.2922, NOW()),
(gen_random_uuid(), 'BRRIO', 'Rio de Janeiro', 'BR', 'America/Sao_Paulo', -22.8839, -43.1729, NOW()),
(gen_random_uuid(), 'BRSSA', 'Salvador', 'BR', 'America/Bahia', -12.9714, -38.5124, NOW()),
(gen_random_uuid(), 'BRPEC', 'Pecem', 'BR', 'America/Fortaleza', -3.5329, -38.7907, NOW()),
(gen_random_uuid(), 'BRSUA', 'Suape', 'BR', 'America/Recife', -8.3941, -34.9616, NOW()),
(gen_random_uuid(), 'BRMAO', 'Manaus', 'BR', 'America/Manaus', -3.1190, -60.0217, NOW()),
-- Argentina / Uruguai
(gen_random_uuid(), 'ARBUE', 'Buenos Aires', 'AR', 'America/Argentina/Buenos_Aires', -34.5997, -58.3733, NOW()),
(gen_random_uuid(), 'UYMVD', 'Montevideo', 'UY', 'America/Montevideo', -34.8941, -56.2122, NOW()),
-- Europa
(gen_random_uuid(), 'NLRTM', 'Rotterdam', 'NL', 'Europe/Amsterdam', 51.9060, 4.4870, NOW()),
(gen_random_uuid(), 'DEHAM', 'Hamburg', 'DE', 'Europe/Berlin', 53.5459, 9.9669, NOW()),
(gen_random_uuid(), 'BEANR', 'Antwerp', 'BE', 'Europe/Brussels', 51.2329, 4.3856, NOW()),
(gen_random_uuid(), 'GBFXT', 'Felixstowe', 'GB', 'Europe/London', 51.9536, 1.3057, NOW()),
(gen_random_uuid(), 'ESVLC', 'Valencia', 'ES', 'Europe/Madrid', 39.4486, -0.3244, NOW()),
(gen_random_uuid(), 'ITGOA', 'Genoa', 'IT', 'Europe/Rome', 44.4094, 8.9268, NOW()),
(gen_random_uuid(), 'FRLEH', 'Le Havre', 'FR', 'Europe/Paris', 49.4808, 0.1073, NOW()),
(gen_random_uuid(), 'PTLEI', 'Leixoes', 'PT', 'Europe/Lisbon', 41.1852, -8.7007, NOW()),
(gen_random_uuid(), 'PTSIE', 'Sines', 'PT', 'Europe/Lisbon', 37.9511, -8.8689, NOW()),
-- Asia
(gen_random_uuid(), 'CNSHA', 'Shanghai', 'CN', 'Asia/Shanghai', 31.3465, 121.6096, NOW()),
(gen_random_uuid(), 'CNNGB', 'Ningbo', 'CN', 'Asia/Shanghai', 29.8683, 121.5440, NOW()),
(gen_random_uuid(), 'CNYTN', 'Yantian', 'CN', 'Asia/Shanghai', 22.5783, 114.2808, NOW()),
(gen_random_uuid(), 'SGSIN', 'Singapore', 'SG', 'Asia/Singapore', 1.2650, 103.8226, NOW()),
(gen_random_uuid(), 'KRPUS', 'Busan', 'KR', 'Asia/Seoul', 35.0799, 129.0571, NOW()),
(gen_random_uuid(), 'JPYOK', 'Yokohama', 'JP', 'Asia/Tokyo', 35.4437, 139.6500, NOW()),
(gen_random_uuid(), 'TWKHH', 'Kaohsiung', 'TW', 'Asia/Taipei', 22.6106, 120.2869, NOW()),
(gen_random_uuid(), 'LKCMB', 'Colombo', 'LK', 'Asia/Colombo', 6.9545, 79.8448, NOW()),
(gen_random_uuid(), 'INMUN', 'Mundra', 'IN', 'Asia/Kolkata', 22.7393, 69.7144, NOW()),
(gen_random_uuid(), 'INNSA', 'Nhava Sheva', 'IN', 'Asia/Kolkata', 18.9500, 72.9500, NOW()),
(gen_random_uuid(), 'MYTPP', 'Tanjung Pelepas', 'MY', 'Asia/Kuala_Lumpur', 1.3631, 103.5551, NOW()),
-- Oriente Medio / Africa
(gen_random_uuid(), 'AEJEA', 'Jebel Ali', 'AE', 'Asia/Dubai', 25.0063, 55.0589, NOW()),
(gen_random_uuid(), 'EGPSD', 'Port Said', 'EG', 'Africa/Cairo', 31.2653, 32.3019, NOW()),
(gen_random_uuid(), 'ZADUR', 'Durban', 'ZA', 'Africa/Johannesburg', -29.8587, 31.0218, NOW()),
(gen_random_uuid(), 'MAPTM', 'Tanger Med', 'MA', 'Africa/Casablanca', 35.8847, -5.5000, NOW()),
-- America do Norte
(gen_random_uuid(), 'USLAX', 'Los Angeles', 'US', 'America/Los_Angeles', 33.7361, -118.2631, NOW()),
(gen_random_uuid(), 'USLGB', 'Long Beach', 'US', 'America/Los_Angeles', 33.7544, -118.2167, NOW()),
(gen_random_uuid(), 'USNYC', 'New York', 'US', 'America/New_York', 40.6680, -74.0384, NOW()),
(gen_random_uuid(), 'USSAV', 'Savannah', 'US', 'America/New_York', 32.0809, -81.0912, NOW()),
(gen_random_uuid(), 'USHOU', 'Houston', 'US', 'America/Chicago', 29.7260, -95.2691, NOW()),
-- Oceania
(gen_random_uuid(), 'AUMEL', 'Melbourne', 'AU', 'Australia/Melbourne', -37.8256, 144.9416, NOW()),
(gen_random_uuid(), 'AUSYD', 'Sydney', 'AU', 'Australia/Sydney', -33.8523, 151.2108, NOW());