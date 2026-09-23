CREATE SEQUENCE IF NOT EXISTS student_business_id_seq START WITH 1;
CREATE SEQUENCE IF NOT EXISTS team_lead_business_id_seq START WITH 1;
CREATE SEQUENCE IF NOT EXISTS staff_business_id_seq START WITH 1;
CREATE SEQUENCE IF NOT EXISTS college_staff_business_id_seq START WITH 1;

SELECT setval('student_business_id_seq', COALESCE((SELECT MAX(CAST(substring(student_id FROM '[0-9]+$') AS BIGINT)) FROM students WHERE student_id ~ '[0-9]+$'), 0), true);
SELECT setval('team_lead_business_id_seq', COALESCE((SELECT MAX(CAST(substring(team_lead_id FROM '[0-9]+$') AS BIGINT)) FROM team_lead WHERE team_lead_id ~ '[0-9]+$'), 0), true);
SELECT setval('staff_business_id_seq', COALESCE((SELECT MAX(CAST(substring(staff_id FROM '[0-9]+$') AS BIGINT)) FROM office_staff WHERE staff_id ~ '[0-9]+$'), 0), true);
SELECT setval('college_staff_business_id_seq', COALESCE((SELECT MAX(id) FROM college_staff), 0), true);

CREATE INDEX IF NOT EXISTS idx_students_email ON students(email);
CREATE INDEX IF NOT EXISTS idx_students_student_id ON students(student_id);
CREATE INDEX IF NOT EXISTS idx_students_mobile_number ON students(mobile_number);
CREATE INDEX IF NOT EXISTS idx_team_lead_email ON team_lead(email);
CREATE INDEX IF NOT EXISTS idx_team_lead_team_lead_id ON team_lead(team_lead_id);
CREATE INDEX IF NOT EXISTS idx_office_staff_email ON office_staff(email);
CREATE INDEX IF NOT EXISTS idx_office_staff_staff_id ON office_staff(staff_id);
CREATE INDEX IF NOT EXISTS idx_college_staff_email ON college_staff(email);
CREATE INDEX IF NOT EXISTS idx_otps_email ON otps(email);
CREATE INDEX IF NOT EXISTS idx_otps_expiry_time ON otps(expiry_time);
CREATE INDEX IF NOT EXISTS idx_student_course_registration_student_id ON student_course_registration(student_id);
CREATE INDEX IF NOT EXISTS idx_student_course_registration_created_at ON student_course_registration(created_at);
CREATE INDEX IF NOT EXISTS idx_razorpay_order_id ON razorpay_payments(razorpay_order_id);
CREATE INDEX IF NOT EXISTS idx_razorpay_payment_id ON razorpay_payments(razorpay_payment_id);

CREATE INDEX IF NOT EXISTS idx_team_lead_created_by_admin_id ON team_lead(created_by_admin_id);
CREATE INDEX IF NOT EXISTS idx_razorpay_payment_registration_id ON razorpay_payments(registration_id);
CREATE INDEX IF NOT EXISTS idx_razorpay_created_at ON razorpay_payments(created_at);
CREATE INDEX IF NOT EXISTS idx_student_attendance_attendance_date ON student_attendance(attendance_date);
