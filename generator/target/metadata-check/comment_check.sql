SELECT COUNT(*) AS with_comment FROM information_schema.columns WHERE TABLE_SCHEMA='hr_management' AND COLUMN_COMMENT<>'';
SELECT COUNT(*) AS total FROM information_schema.columns WHERE TABLE_SCHEMA='hr_management';
