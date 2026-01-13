CREATE SCHEMA IF NOT EXISTS cache_inv_bus;

CREATE TABLE cache_inv_bus.obj_evict_event (
  id_evict_event BIGSERIAL PRIMARY KEY,
  cache_name     TEXT        NOT NULL,
  cache_key      TEXT        NOT NULL,
  recorded_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Optimized for polling by cache_name and time
CREATE INDEX idx_obj_evict_event_cache_time
ON cache_inv_bus.obj_evict_event (cache_name, recorded_at);

-- Supports cleanup and replay
CREATE INDEX idx_obj_evict_event_time
ON cache_inv_bus.obj_evict_event (recorded_at);
