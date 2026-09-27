-- Script de configuration initiale PostgreSQL pour JobTrack
DO
$$
BEGIN
   IF NOT EXISTS (
      SELECT FROM pg_catalog.pg_roles
      WHERE rolname = 'jobtrack_user'
   ) THEN
      CREATE USER jobtrack_user WITH PASSWORD 'jobtrack_dev_password';
   END IF;
END
$$;

-- Note: database creation cannot be run in a transaction block
