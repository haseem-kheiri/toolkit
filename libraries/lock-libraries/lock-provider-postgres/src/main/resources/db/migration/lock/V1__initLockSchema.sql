CREATE SCHEMA IF NOT EXISTS lock;

CREATE TABLE lock.obj_lock_lease (
    lock_name     TEXT        NOT NULL,
    execution_id  TEXT        NOT NULL,
    expires_at    TIMESTAMPTZ NOT NULL,

    CONSTRAINT pk_obj_lock_lease PRIMARY KEY (lock_name)
);

CREATE INDEX idx_obj_lock_lease_expires_at
ON lock.obj_lock_lease (expires_at);
