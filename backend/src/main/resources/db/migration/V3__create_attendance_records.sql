CREATE TABLE attendance_records (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    date DATE NOT NULL,
    clock_in TIME NOT NULL,
    clock_out TIME,
    break_minutes INTEGER NOT NULL DEFAULT 0,
    note VARCHAR(500),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(employee_id, date)
);

CREATE INDEX idx_attendance_employee_date ON attendance_records(employee_id, date);
