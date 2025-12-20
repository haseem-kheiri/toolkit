CREATE SCHEMA IF NOT EXISTS cluster;

CREATE TABLE cluster.obj_heartbeat (
  cluster_name   TEXT        NOT NULL,
  session_id     TEXT        NOT NULL,
  recorded_at    TIMESTAMPTZ NOT NULL,
  metadata       TEXT        NOT NULL,

  PRIMARY KEY (cluster_name, session_id)
);

CREATE INDEX idx_obj_heartbeat_cluster_time
ON cluster.obj_heartbeat (cluster_name, recorded_at);

