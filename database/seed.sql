-- eSHCAT Seed Data
-- Demo / synthetic data only. Validate with LGU before any deployment.

PRAGMA foreign_keys = ON;

-- Departments
INSERT INTO departments (id, name, description, location, contact_number, email, office_hours) VALUES
    (1, 'Civil Registry', 'Handles civil registration documents such as birth, marriage, and death certificates.', 'Municipal Hall, Catarman', '(055) 555-0101', 'civilregistry@eshcat.local', 'Mon-Fri 8:00 AM - 5:00 PM'),
    (2, 'Business Permits', 'Issues business permits and licenses for local enterprises.', 'Municipal Hall, Catarman', '(055) 555-0102', 'bplo@eshcat.local', 'Mon-Fri 8:00 AM - 5:00 PM'),
    (3, 'Municipal Treasurer', 'Handles tax and assessment inquiries and payments.', 'Municipal Hall, Catarman', '(055) 555-0103', 'treasurer@eshcat.local', 'Mon-Fri 8:00 AM - 5:00 PM'),
    (4, 'City / Municipal Engineering', 'Manages infrastructure and public works concerns.', 'Municipal Hall, Catarman', '(055) 555-0104', 'engineering@eshcat.local', 'Mon-Fri 8:00 AM - 5:00 PM'),
    (5, 'Municipal Health Office', 'Provides public health services and programs.', 'Rural Health Unit, Catarman', '(055) 555-0105', 'health@eshcat.local', 'Mon-Fri 8:00 AM - 5:00 PM'),
    (6, 'Social Welfare', 'Renders social welfare and development services.', 'Municipal Hall, Catarman', '(055) 555-0106', 'mswdo@eshcat.local', 'Mon-Fri 8:00 AM - 5:00 PM'),
    (7, 'Barangay Affairs', 'Coordinates barangay-level services and clearances.', 'Municipal Hall, Catarman', '(055) 555-0107', 'barangay@eshcat.local', 'Mon-Fri 8:00 AM - 5:00 PM');

-- Services
INSERT INTO services (id, department_id, name, short_description, description, estimated_processing, is_online) VALUES
    (1, 1, 'E-Death Certificate', 'Request information and submit the prototype application online.', 'Application for a copy of a death certificate from the Civil Registry. This is the primary hackathon demonstration service.', '5-7 working days', 1),
    (2, 1, 'Birth Certificate', 'Apply for a copy of a birth certificate.', 'Request a certified copy of a birth certificate from the Civil Registry.', '5-7 working days', 1),
    (3, 1, 'Marriage Certificate', 'Apply for a copy of a marriage certificate.', 'Request a certified copy of a marriage certificate from the Civil Registry.', '5-7 working days', 0),
    (4, 2, 'Business Permit', 'Apply for a new or renewed business permit.', 'Application for a Mayor''s Permit for new or renewing businesses.', '3-5 working days', 0),
    (5, 3, 'Real Property Tax Inquiry', 'File an inquiry about real property tax.', 'Submit a request for real property tax assessment information.', '2-3 working days', 0),
    (6, 7, 'Barangay Clearance', 'Request a barangay clearance.', 'Submit a request for a barangay clearance certificate.', '1-3 working days', 0);

-- Service Requirements
INSERT INTO service_requirements (service_id, requirement) VALUES
    (1, 'Deceased person''s full name and date of death'),
    (1, 'Applicant relationship to the deceased'),
    (1, 'Valid government-issued ID of the applicant'),
    (1, 'Filled-out application form'),
    (2, 'Full name and date of birth as they appear in records'),
    (2, 'Valid government-issued ID of the applicant'),
    (3, 'Full names of spouses and date of marriage'),
    (3, 'Valid government-issued ID of the applicant'),
    (4, 'Business name and address'),
    (4, 'Valid government-issued ID of the owner'),
    (4, 'Barangay clearance'),
    (5, 'Property owner''s name or tax declaration number'),
    (6, 'Valid government-issued ID'),
    (6, 'Proof of residency');

-- Service Form Fields (for configurable engine)
INSERT INTO service_form_fields (service_id, label, field_name, field_type, required, options) VALUES
    (1, 'Full Name', 'fullName', 'text', 1, NULL),
    (1, 'Email Address', 'email', 'email', 1, NULL),
    (1, 'Mobile Number', 'mobile', 'tel', 0, NULL),
    (1, 'Address', 'address', 'text', 1, NULL),
    (1, 'Deceased Full Name', 'deceasedName', 'text', 1, NULL),
    (1, 'Date of Death', 'dateOfDeath', 'date', 1, NULL),
    (1, 'Relationship to Deceased', 'relationship', 'select', 1, '["Spouse","Parent","Child","Sibling","Other"]'),
    (1, 'Purpose', 'purpose', 'select', 1, '["Legal affairs","Insurance claim","Employment","Personal copy","Other"]'),
    (1, 'Preferred Contact Method', 'contactMethod', 'select', 1, '["Email","Mobile","Phone"]'),
    (1, 'Additional Details', 'additionalDetails', 'textarea', 0, NULL);

-- Staff users (password: change_me_123 -- hashed via werkzeug in seed script when run through app)
INSERT INTO staff_users (id, name, email, password_hash, role, department_id, is_active) VALUES
    (1, 'Maria Santos', 'admin@eshcat.local', 'SEED_ME', 'Administrator', 1, 1),
    (2, 'Juan Dela Cruz', 'staff@eshcat.local', 'SEED_ME', 'Staff', 1, 1),
    (3, 'Ana Reyes', 'head@eshcat.local', 'SEED_ME', 'Department Head', 2, 1);

-- Announcements
INSERT INTO announcements (id, title, date_text, department, description, is_pinned) VALUES
    (1, 'Scheduled Municipal Service Advisory', 'September 23, 2026', 'Municipal Government', 'All offices will be closed on the second Saturday of the month for inventory and maintenance. Regular operations resume the following Monday.', 1),
    (2, 'Civil Registry Summer Service Schedule', 'September 15, 2026', 'Civil Registry', 'The Civil Registry accepts applications Monday to Friday from 8:00 AM to 5:00 PM. Walk-in applicants are prioritized before 10:00 AM.', 0),
    (3, 'eSHCAT Prototype Demo', 'September 20, 2026', 'Information Office', 'The eSHCAT prototype is now live for demonstration. Explore services and try the E-Death Certificate application workflow.', 1);

-- Agency directory
INSERT INTO agency_directory (id, name, department, location, contact, email, office_hours, services) VALUES
    (1, 'Civil Registry Office', 'Civil Registry', 'Municipal Hall, Catarman', '(055) 555-0101', 'civilregistry@eshcat.local', 'Mon-Fri 8:00 AM - 5:00 PM', 'Birth, Death, Marriage certificates'),
    (2, 'Business Permits and Licensing Office', 'Business Permits', 'Municipal Hall, Catarman', '(055) 555-0102', 'bplo@eshcat.local', 'Mon-Fri 8:00 AM - 5:00 PM', 'Business permits and licenses'),
    (3, 'Municipal Treasurer''s Office', 'Municipal Treasurer', 'Municipal Hall, Catarman', '(055) 555-0103', 'treasurer@eshcat.local', 'Mon-Fri 8:00 AM - 5:00 PM', 'Tax and assessment inquiries'),
    (4, 'Municipal Engineering Office', 'Engineering', 'Municipal Hall, Catarman', '(055) 555-0104', 'engineering@eshcat.local', 'Mon-Fri 8:00 AM - 5:00 PM', 'Public works and infrastructure'),
    (5, 'Rural Health Unit', 'Health', 'Rural Health Unit, Catarman', '(055) 555-0105', 'health@eshcat.local', 'Mon-Fri 8:00 AM - 5:00 PM', 'Public health services');

-- Sample application for demo
INSERT INTO applications (id, reference_number, service_id, department_id, full_name, email, mobile, address, form_data, status, created_at, updated_at) VALUES
    (1, 'CAT-DC-DEMO1234', 1, 1, 'Juan Dela Cruz', 'juan@example.com', '09171234567', 'Catarman, Northern Samar', '{"deceasedName":"Pedro Dela Cruz","dateOfDeath":"2026-09-01","relationship":"Child","purpose":"Legal affairs","contactMethod":"Email"}', 'Under Review', datetime('now', '-2 days'), datetime('now', '-1 day'));

INSERT INTO application_history (application_id, staff_id, old_status, new_status, remarks, created_at) VALUES
    (1, NULL, 'Submitted', 'Received', 'Application received.', datetime('now', '-2 days')),
    (1, 2, 'Received', 'Under Review', 'Validating submitted details.', datetime('now', '-1 day'));