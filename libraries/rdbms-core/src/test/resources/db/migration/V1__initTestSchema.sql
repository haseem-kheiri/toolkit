CREATE SCHEMA IF NOT EXISTS test;

CREATE TABLE test.persons (
  id BIGSERIAL PRIMARY KEY,
  first_name VARCHAR(100) NOT NULL,
  last_name VARCHAR(100) NOT NULL,
  age INTEGER CHECK (age >= 0),
  CONSTRAINT unique_full_name UNIQUE (first_name, last_name)
);

CREATE INDEX idx_persons_first_name ON test.persons (first_name);
CREATE INDEX idx_persons_last_name ON test.persons (last_name);
CREATE INDEX idx_persons_age ON test.persons (age);