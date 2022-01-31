
CREATE TABLE IF NOT EXISTS notes (
  id uuid NOT NULL,
  text TEXT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NULL,
  PRIMARY KEY (id)
);
