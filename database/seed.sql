-- eSHCAT Seed Data
-- Synced with Municipality of Catarman E-Services (https://catarman.gov.ph/)
-- Offices/contacts from the Governance and Contact pages; services mirror the
-- official categories (Cedula, Civil Registry, Business, Building, Assessor, Zoning).

PRAGMA foreign_keys = ON;

-- Departments (real offices)
INSERT INTO departments (id, name, description, location, contact_number, email, office_hours) VALUES
    (1, 'Local Civil Registry Office (LCRO)', 'Records birth, marriage, and death and issues certified copies of civil registry documents.', 'Municipal Hall, Catarman, Northern Samar', '(055) 500-0712', 'lgu.catarman@gmail.com', 'Mon-Fri 8:00 AM - 5:00 PM'),
    (2, 'Business Permit and Licensing Office (BPLO)', 'Issues, renews, and retires business permits and provides certified true copies.', 'Municipal Hall, Catarman, Northern Samar', '0920-295-0169', 'bplo.lgucatarman@gmail.com', 'Mon-Fri 8:00 AM - 5:00 PM'),
    (3, 'Municipal Treasurer''s Office (MTO)', 'Collects local taxes and fees including the community tax certificate (cedula).', 'Municipal Hall, Catarman, Northern Samar', '(055) 500-1453', 'mto.lgu.catarman@gmail.com', 'Mon-Fri 8:00 AM - 5:00 PM'),
    (4, 'Office of the Building Official (OBO)', 'Issues building, electrical, and related permits and certificates of occupancy.', 'Municipal Hall, Catarman, Northern Samar', '(055) 500-0713', 'engineering.lgucatarman@gmail.com', 'Mon-Fri 8:00 AM - 5:00 PM'),
    (5, 'Municipal Assessor''s Office (MASSO)', 'Assesses real property, processes transfers of ownership, and issues tax declarations.', 'Municipal Hall, Catarman, Northern Samar', '(055) 500-2042', 'massocatarmanns@gmail.com', 'Mon-Fri 8:00 AM - 5:00 PM'),
    (6, 'Municipal Planning and Development Office (MPDO)', 'Handles zoning certifications, locational clearances, and land-use planning.', 'Municipal Hall, Catarman, Northern Samar', '(055) 500-1192', 'mpdocatarmannsamar@gmail.com', 'Mon-Fri 8:00 AM - 5:00 PM'),
    (7, 'Office of the Municipal Mayor', 'Leadership and general administration of the Municipality of Catarman.', 'Municipal Hall, Catarman, Northern Samar', '(055) 500-0712', 'lgu.catarman@gmail.com', 'Mon-Fri 8:00 AM - 5:00 PM'),
    (8, 'Municipal Health Office (MHO)', 'Provides public health services, programs, and medical assistance.', 'Rural Health Unit, Catarman, Northern Samar', '(055) 500-9615', 'mho.lgu.catarman@gmail.com', 'Mon-Fri 8:00 AM - 5:00 PM'),
    (9, 'Municipal Social Welfare and Development Office (MSWDO)', 'Renders social welfare and development services to individuals and families.', 'Municipal Hall, Catarman, Northern Samar', '0928-479-4710', 'minnielldurens@yahoo.com', 'Mon-Fri 8:00 AM - 5:00 PM'),
    (10, 'Municipal Environment and Natural Resources Office (MENRO)', 'Manages environmental protection, solid waste, and natural resources programs.', 'Municipal Hall, Catarman, Northern Samar', '(055) 500-1735', 'menrocatarman@gmail.com', 'Mon-Fri 8:00 AM - 5:00 PM');

INSERT INTO departments (id, name, description, location, contact_number, email, office_hours) VALUES
    (11, 'Commission on Elections (COMELEC)', 'Handles voter registration, voter certifications, and election-day assistance.', 'Municipal Hall, Catarman, Northern Samar', '(055) 500-0712', 'comelec.catarman@gmail.com', 'Election day and office hours vary');

-- Services (mirror the official E-Services categories)
INSERT INTO services (id, department_id, name, short_description, description, estimated_processing, is_online) VALUES
    (1, 1, 'Death Certificate', 'Apply for a certified copy of a death certificate.', 'Request a copy of a death certificate from the Local Civil Registry Office (LCRO) stating the date and place of death and the personal circumstances of the deceased.', '5-7 working days', 1),
    (2, 1, 'Birth Certificate', 'Apply for a certified copy of a birth certificate.', 'Request a certified copy of a birth certificate from the Local Civil Registry Office (LCRO).', '5-7 working days', 1),
    (3, 1, 'Marriage Certificate', 'Apply for a certified copy of a marriage certificate.', 'Request a certified copy of a marriage certificate from the Local Civil Registry Office (LCRO).', '5-7 working days', 1),
    (4, 1, 'Certificate Correction (RA 9048 / RA 10172)', 'Apply for correction of clerical errors or of sex or date of birth.', 'Petition for correction of clerical or typographical errors (RA 9048) or of sex or date of birth (RA 10172) on birth, marriage, or death certificates.', '10-15 working days', 1),
    (5, 3, 'Community Tax Certificate (Cedula)', 'Apply for issuance of a community tax certificate (cedula).', 'The cedula or Community Tax Certificate is issued by the Municipal Treasurer''s Office to individuals and corporations as proof of payment of the community tax.', '1-2 working days', 1),
    (6, 2, 'New / Renew Business Permit', 'Apply for a new or renewed business permit.', 'Business Permit and Licensing Office (BPLO) service for a new Mayor''s Permit or the annual renewal of an existing business permit.', '3-5 working days', 1),
    (7, 2, 'Retirement of Business', 'Officially close or retire a business.', 'Straightforward process to officially close a business with the Business Permit and Licensing Office (BPLO).', '3-5 working days', 0),
    (8, 2, 'Certified True Copy of Business Permit', 'Obtain an official copy of a business permit.', 'Request a certified true copy of a business permit for legal and administrative purposes.', '1-2 working days', 1),
    (9, 4, 'Building Permit', 'Apply for a new building permit.', 'Apply online with the Office of the Building Official (OBO) to ensure your project meets local building codes and regulations.', '10-15 working days', 1),
    (10, 4, 'Certificate of Occupancy', 'Obtain a certificate of occupancy.', 'Confirm that a finished building meets all safety and compliance standards with the Office of the Building Official (OBO).', '5-7 working days', 0),
    (11, 4, 'Electrical Permit', 'Submit an electrical permit application.', 'Ensure your electrical work meets all safety and code requirements with the Office of the Building Official (OBO).', '5-7 working days', 0),
    (12, 5, 'Transfer of Ownership of Real Property (TR)', 'Transfer ownership of real property.', 'Seamless and legally compliant transition of real property with the Municipal Assessor''s Office (MASSO).', '10-15 working days', 1),
    (13, 5, 'Certified True Copy of Tax Declaration', 'Request an official copy of your tax declaration.', 'Official documentation verifying the details of your property''s assessment for taxation purposes.', '2-3 working days', 1),
    (14, 5, 'Certification of Improvement / No Improvement', 'Request certification of property improvements.', 'Official statement regarding the status of improvements made to a property.', '2-3 working days', 0),
    (15, 6, 'Locational Clearance', 'Apply for a locational clearance.', 'Certify that a proposed development or construction project complies with zoning laws and land-use regulations.', '5-7 working days', 1),
    (16, 6, 'Zoning Certification', 'Verify a property''s compliance with zoning regulations.', 'Confirm that a property adheres to local zoning regulations with the Municipal Planning and Development Office (MPDO).', '3-5 working days', 0);

INSERT INTO services (id, department_id, name, short_description, description, estimated_processing, is_online) VALUES
    (17, 11, 'Voter''s Certification', 'Request a certification of voter registration.', 'Submit your information for COMELEC verification and receive a reference number for tracking.', '3-5 working days', 1),
    (18, 11, 'Transfer of Voter Registration', 'Request transfer to a new voting precinct or barangay.', 'Submit your current and new registration details for COMELEC verification and transfer processing.', '7-10 working days', 1);

-- Service Requirements
INSERT INTO service_requirements (service_id, requirement) VALUES
    (1, 'Deceased person''s full name and date of death'),
    (1, 'Valid government-issued ID of the applicant'),
    (1, 'Filled-out application form'),
    (2, 'Full name and date of birth as they appear in records'),
    (2, 'Valid government-issued ID of the applicant'),
    (3, 'Full names of spouses and date of marriage'),
    (3, 'Valid government-issued ID of the applicant'),
    (4, 'Document with the clerical error (original certificate)'),
    (4, 'Valid government-issued ID of the petitioner'),
    (4, 'Reason/correction details to be certified'),
    (5, 'No documentary requirements for individuals'),
    (5, 'Business/corporation papers for corporate cedula'),
    (6, 'Business name and address'),
    (6, 'Valid government-issued ID of the owner'),
    (6, 'Barangay clearance'),
    (6, 'Previous business permit (for renewal)'),
    (7, 'Original business permit'),
    (7, 'Statement of account / tax clearances'),
    (8, 'Copy of the business permit to be certified'),
    (8, 'Authorization letter (for representatives)'),
    (9, 'Deed of sale or proof of land ownership'),
    (9, 'Vicinity map and lot plan'),
    (9, 'Design specifications / architectural plans'),
    (9, 'Barangay clearance'),
    (10, 'Building permit and as-built plans'),
    (10, 'Certificate of final electrical inspection'),
    (11, 'Electrical plans / diagram'),
    (11, 'Certificate of Professional Registration of Electrician'),
    (12, 'TCT/CCT of the property'),
    (12, 'Deed of sale (notarized)'),
    (12, 'Tax declaration of the property'),
    (13, 'Tax declaration number of the property'),
    (13, 'Valid government-issued ID'),
    (14, 'Tax declaration number of the property'),
    (14, 'Valid government-issued ID'),
    (15, 'Proof of ownership (TCT/CCT or tax declaration)'),
    (15, 'Vicinity map / locational sketch'),
    (15, 'Valid government-issued ID'),
    (16, 'Tax declaration of the property'),
    (16, 'Valid government-issued ID');

INSERT INTO service_requirements (service_id, requirement) VALUES
    (17, 'Valid government-issued ID'),
    (17, 'Voter registration details or precinct number'),
    (17, 'Correct full name and date of birth'),
    (18, 'Valid government-issued ID'),
    (18, 'Current voter registration record or Voter''s ID'),
    (18, 'Proof of new residence or barangay certification'),
    (18, 'Previous precinct or barangay information'),
    (18, 'Authorization letter if filed by a representative');

-- Service Form Fields (configurable form engine)
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
    (1, 'Additional Details', 'additionalDetails', 'textarea', 0, NULL),
    (2, 'Full Name', 'fullName', 'text', 1, NULL),
    (2, 'Email Address', 'email', 'email', 1, NULL),
    (2, 'Mobile Number', 'mobile', 'tel', 0, NULL),
    (2, 'Address', 'address', 'text', 1, NULL),
    (2, 'Full Name of Person Whose Birth Certificate is Requested', 'registrantName', 'text', 1, NULL),
    (2, 'Full Name of Person Who Will Claim / Pick Up the Certificate', 'claimantName', 'text', 1, NULL),
    (2, 'Date of Birth', 'dateOfBirth', 'date', 1, NULL),
    (2, 'Place of Birth', 'placeOfBirth', 'text', 1, NULL),
    (2, 'Father''s Full Name', 'fatherName', 'text', 1, NULL),
    (2, 'Mother''s Maiden Name', 'motherMaidenName', 'text', 1, NULL),
    (2, 'Requester''s Relationship to the Person', 'requesterRelationship', 'select', 1, '["Self","Parent","Spouse","Child","Sibling","Relative","Authorized Representative","Other"]'),
    (2, 'Valid Government-Issued ID', 'validIdType', 'select', 1, '["Philippine Passport","Driver''s License","UMID / SSS / GSIS ID","PhilHealth ID","TIN ID","Postal ID","National ID / PhilSys ID","Voter''s ID","Senior Citizen ID","Other Government-Issued ID"]'),
    (2, 'Purpose', 'purpose', 'select', 1, '["Legal affairs","Employment","School","Passport","Personal copy","Other"]'),
    (3, 'Full Name', 'fullName', 'text', 1, NULL),
    (3, 'Email Address', 'email', 'email', 1, NULL),
    (3, 'Mobile Number', 'mobile', 'tel', 0, NULL),
    (3, 'Address', 'address', 'text', 1, NULL),
    (3, 'Spouse Full Name', 'spouseName', 'text', 1, NULL),
    (3, 'Date of Marriage', 'dateOfMarriage', 'date', 1, NULL),
    (3, 'Place of Marriage', 'placeOfMarriage', 'text', 1, NULL),
    (3, 'Purpose', 'purpose', 'select', 1, '["Legal affairs","Passport","Personal copy","Other"]'),
    (4, 'Full Name', 'fullName', 'text', 1, NULL),
    (4, 'Email Address', 'email', 'email', 1, NULL),
    (4, 'Mobile Number', 'mobile', 'tel', 0, NULL),
    (4, 'Address', 'address', 'text', 1, NULL),
    (4, 'Document Type', 'documentType', 'select', 1, '["Birth Certificate","Marriage Certificate","Death Certificate"]'),
    (4, 'Correction Type', 'correctionType', 'select', 1, '["RA 9048 - Clerical Error (Birth)","RA 9048 - Clerical Error (Marriage)","RA 9048 - Clerical Error (Death)","RA 10172 - Sex or Date of Birth"]'),
    (4, 'Nature of Correction', 'natureOfCorrection', 'textarea', 1, NULL),
    (4, 'Petitioner''s Relationship', 'petitionerRelationship', 'select', 1, '["Owner","Spouse","Son","Daughter","Mother","Father","Other"]'),
    (5, 'Full Name', 'fullName', 'text', 1, NULL),
    (5, 'Email Address', 'email', 'email', 1, NULL),
    (5, 'Mobile Number', 'mobile', 'tel', 0, NULL),
    (5, 'Address', 'address', 'text', 1, NULL),
    (5, 'Tax Group Type', 'taxGroupType', 'select', 1, '["CEDULA for Individual","CEDULA for Corporation"]'),
    (5, 'Total Income / Gross Receipts', 'incomeDeclaration', 'number', 0, NULL),
    (6, 'Full Name', 'fullName', 'text', 1, NULL),
    (6, 'Email Address', 'email', 'email', 1, NULL),
    (6, 'Mobile Number', 'mobile', 'tel', 0, NULL),
    (6, 'Address', 'address', 'text', 1, NULL),
    (6, 'Business Name', 'businessName', 'text', 1, NULL),
    (6, 'Business Address', 'businessAddress', 'text', 1, NULL),
    (6, 'Permit Type', 'permitType', 'select', 1, '["New","Renewal"]'),
    (6, 'Line of Business', 'lineOfBusiness', 'text', 1, NULL),
    (6, 'Capital / Gross Sales', 'capital', 'number', 0, NULL),
    (7, 'Full Name', 'fullName', 'text', 1, NULL),
    (7, 'Email Address', 'email', 'email', 1, NULL),
    (7, 'Mobile Number', 'mobile', 'tel', 0, NULL),
    (7, 'Address', 'address', 'text', 1, NULL),
    (7, 'Business Name', 'businessName', 'text', 1, NULL),
    (7, 'Reason for Retirement', 'reason', 'textarea', 1, NULL),
    (8, 'Full Name', 'fullName', 'text', 1, NULL),
    (8, 'Email Address', 'email', 'email', 1, NULL),
    (8, 'Mobile Number', 'mobile', 'tel', 0, NULL),
    (8, 'Address', 'address', 'text', 1, NULL),
    (8, 'Business Name', 'businessName', 'text', 1, NULL),
    (8, 'Purpose', 'purpose', 'textarea', 1, NULL),
    (9, 'Full Name', 'fullName', 'text', 1, NULL),
    (9, 'Email Address', 'email', 'email', 1, NULL),
    (9, 'Mobile Number', 'mobile', 'tel', 0, NULL),
    (9, 'Address', 'address', 'text', 1, NULL),
    (9, 'Project Location', 'projectLocation', 'text', 1, NULL),
    (9, 'Project Description', 'projectDescription', 'textarea', 1, NULL),
    (9, 'Building Type', 'buildingType', 'select', 1, '["Residential","Commercial","Institutional","Mixed Use"]'),
    (9, 'Estimated Construction Cost (PHP)', 'estimatedCost', 'number', 0, NULL),
    (10, 'Full Name', 'fullName', 'text', 1, NULL),
    (10, 'Email Address', 'email', 'email', 1, NULL),
    (10, 'Mobile Number', 'mobile', 'tel', 0, NULL),
    (10, 'Address', 'address', 'text', 1, NULL),
    (10, 'Project Name / Location', 'projectLocation', 'text', 1, NULL),
    (10, 'Occupancy Type', 'occupancyType', 'select', 1, '["Residential","Commercial","Industrial","Institutional"]'),
    (11, 'Full Name', 'fullName', 'text', 1, NULL),
    (11, 'Email Address', 'email', 'email', 1, NULL),
    (11, 'Mobile Number', 'mobile', 'tel', 0, NULL),
    (11, 'Address', 'address', 'text', 1, NULL),
    (11, 'Project Location', 'projectLocation', 'text', 1, NULL),
    (11, 'Installed Load (kW)', 'installedLoad', 'number', 0, NULL),
    (12, 'Full Name', 'fullName', 'text', 1, NULL),
    (12, 'Email Address', 'email', 'email', 1, NULL),
    (12, 'Mobile Number', 'mobile', 'tel', 0, NULL),
    (12, 'Address', 'address', 'text', 1, NULL),
    (12, 'Property Owner Name', 'propertyOwner', 'text', 1, NULL),
    (12, 'TCT / CCT Number', 'tctCctNumber', 'text', 1, NULL),
    (12, 'Tax Declaration Number', 'taxDeclarationNo', 'text', 1, NULL),
    (12, 'Property Location', 'propertyLocation', 'text', 1, NULL),
    (13, 'Full Name', 'fullName', 'text', 1, NULL),
    (13, 'Email Address', 'email', 'email', 1, NULL),
    (13, 'Mobile Number', 'mobile', 'tel', 0, NULL),
    (13, 'Address', 'address', 'text', 1, NULL),
    (13, 'Tax Declaration Number', 'taxDeclarationNo', 'text', 1, NULL),
    (13, 'Property Owner Name', 'propertyOwner', 'text', 1, NULL),
    (14, 'Full Name', 'fullName', 'text', 1, NULL),
    (14, 'Email Address', 'email', 'email', 1, NULL),
    (14, 'Mobile Number', 'mobile', 'tel', 0, NULL),
    (14, 'Address', 'address', 'text', 1, NULL),
    (14, 'Tax Declaration Number', 'taxDeclarationNo', 'text', 1, NULL),
    (14, 'Property Location', 'propertyLocation', 'text', 1, NULL),
    (15, 'Full Name', 'fullName', 'text', 1, NULL),
    (15, 'Email Address', 'email', 'email', 1, NULL),
    (15, 'Mobile Number', 'mobile', 'tel', 0, NULL),
    (15, 'Address', 'address', 'text', 1, NULL),
    (15, 'Project Name', 'projectName', 'text', 1, NULL),
    (15, 'Project Location', 'projectLocation', 'text', 1, NULL),
    (15, 'Lot Area (sqm)', 'lotArea', 'number', 0, NULL),
    (16, 'Full Name', 'fullName', 'text', 1, NULL),
    (16, 'Email Address', 'email', 'email', 1, NULL),
    (16, 'Mobile Number', 'mobile', 'tel', 0, NULL),
    (16, 'Address', 'address', 'text', 1, NULL),
    (16, 'Property Owner Name', 'propertyOwner', 'text', 1, NULL),
    (16, 'Property Location', 'propertyLocation', 'text', 1, NULL);

INSERT INTO service_form_fields (service_id, label, field_name, field_type, required, options) VALUES
    (17, 'Full Name', 'fullName', 'text', 1, NULL),
    (17, 'Email Address', 'email', 'email', 1, NULL),
    (17, 'Mobile Number', 'mobile', 'tel', 0, NULL),
    (17, 'Complete Address', 'address', 'text', 1, NULL),
    (17, 'Date of Birth', 'dateOfBirth', 'date', 1, NULL),
    (17, 'Precinct Number', 'precinctNumber', 'text', 1, NULL),
    (17, 'Barangay', 'barangay', 'text', 1, NULL),
    (17, 'Valid Government-Issued ID', 'validIdType', 'select', 1, '["National ID / PhilSys ID","Driver''s License","Philippine Passport","UMID / SSS / GSIS ID","Voter''s ID","Other Government-Issued ID"]'),
    (17, 'Purpose of Certification', 'purpose', 'select', 1, '["Employment","School","Government transaction","Personal record","Other"]'),
    (18, 'Full Name', 'fullName', 'text', 1, NULL),
    (18, 'Email Address', 'email', 'email', 1, NULL),
    (18, 'Mobile Number', 'mobile', 'tel', 0, NULL),
    (18, 'Current Address', 'address', 'text', 1, NULL),
    (18, 'Date of Birth', 'dateOfBirth', 'date', 1, NULL),
    (18, 'Current Precinct Number', 'currentPrecinct', 'text', 1, NULL),
    (18, 'Current Barangay', 'currentBarangay', 'text', 1, NULL),
    (18, 'New Address', 'newAddress', 'text', 1, NULL),
    (18, 'New Barangay', 'newBarangay', 'text', 1, NULL),
    (18, 'Valid Government-Issued ID', 'validIdType', 'select', 1, '["National ID / PhilSys ID","Driver''s License","Philippine Passport","UMID / SSS / GSIS ID","Voter''s ID","Other Government-Issued ID"]'),
    (18, 'Filing as', 'filingAs', 'select', 1, '["Registered voter","Authorized representative"]');

-- Staff users (password: change_me_123 -- hashed via werkzeug when seeded through the app)
INSERT INTO staff_users (id, name, email, password_hash, role, department_id, is_active) VALUES
    (1, 'Maria Santos', 'admin@eshcat.local', 'SEED_ME', 'Administrator', 1, 1),
    (2, 'Juan Dela Cruz', 'staff@eshcat.local', 'SEED_ME', 'Staff', 1, 1),
    (3, 'Ana Reyes', 'head@eshcat.local', 'SEED_ME', 'Department Head', 2, 1),
    (4, 'LCRO Service Staff', 'staff.lcro@eshcat.local', 'SEED_ME', 'Staff', 1, 1),
    (5, 'BPLO Service Staff', 'staff.bplo@eshcat.local', 'SEED_ME', 'Staff', 2, 1),
    (6, 'MTO Service Staff', 'staff.mto@eshcat.local', 'SEED_ME', 'Staff', 3, 1),
    (7, 'OBO Service Staff', 'staff.obo@eshcat.local', 'SEED_ME', 'Staff', 4, 1),
    (8, 'MASSO Service Staff', 'staff.masso@eshcat.local', 'SEED_ME', 'Staff', 5, 1),
    (9, 'MPDO Service Staff', 'staff.mpdo@eshcat.local', 'SEED_ME', 'Staff', 6, 1),
    (10, 'Mayor Office Staff', 'staff.mayor@eshcat.local', 'SEED_ME', 'Staff', 7, 1),
    (11, 'MHO Service Staff', 'staff.mho@eshcat.local', 'SEED_ME', 'Staff', 8, 1),
    (12, 'MSWDO Service Staff', 'staff.mswdo@eshcat.local', 'SEED_ME', 'Staff', 9, 1),
    (13, 'MENRO Service Staff', 'staff.menro@eshcat.local', 'SEED_ME', 'Staff', 10, 1),
    (14, 'COMELEC Service Staff', 'staff.comelec@eshcat.local', 'SEED_ME', 'Staff', 11, 1),
    (15, 'COMELEC Department Head', 'head.comelec@eshcat.local', 'SEED_ME', 'Department Head', 11, 1);

INSERT INTO comelec_settings (id, election_day_active, public_lookup_enabled, queue_room, announcement)
VALUES (1, 0, 0, 'COMELEC Room 1', 'Election-day queue is currently inactive.');

-- Civil portal resident (password: change_me_123 -- hashed via werkzeug when seeded through the app)
INSERT INTO civil_users (id, username, password_hash, full_name, email) VALUES
    (1, 'resident', 'SEED_ME', 'Ramon Resident', 'resident@example.com');

-- Announcements
INSERT INTO announcements (id, title, date_text, department, description, is_pinned) VALUES
    (1, 'eSHCAT Online Services Now in Testing Phase', 'September 23, 2026', 'Municipal Government', 'Municipality of Catarman E-Services is now in a testing phase. Data entered may be reset and is not guaranteed to be permanently stored. We value your feedback as we finalize the system.', 1),
    (2, 'Civil Registry Accepts Applications Daily', 'September 15, 2026', 'Local Civil Registry Office (LCRO)', 'Birth, marriage, and death certificate requests are accepted Monday to Friday, 8:00 AM to 5:00 PM. Walk-in applicants are prioritized before 10:00 AM.', 1),
    (3, 'Business Permit Renewal Reminder', 'September 20, 2026', 'Business Permit and Licensing Office (BPLO)', 'Business owners may file new or renewal business permit applications online or at the BPLO. Prepare your barangay clearance and previous permit for renewal.', 0);

-- Agency directory (real offices per catarman.gov.ph)
INSERT INTO agency_directory (id, name, department, location, contact, email, office_hours, services) VALUES
    (1, 'Office of the Municipal Mayor', 'Local Government Unit', 'Municipal Hall, Catarman, Northern Samar', '(055) 500-0712 / 0915-836-8555', 'lgu.catarman@gmail.com', 'Mon-Fri 8:00 AM - 5:00 PM', 'General administration and leadership'),
    (2, 'Local Civil Registry Office (LCRO)', 'Civil Registration', 'Municipal Hall, Catarman, Northern Samar', '(055) 500-0712', 'lgu.catarman@gmail.com', 'Mon-Fri 8:00 AM - 5:00 PM', 'Birth, Marriage, Death certificates; RA 9048 / RA 10172 corrections'),
    (3, 'Business Permit and Licensing Office (BPLO)', 'Business Permits', 'Municipal Hall, Catarman, Northern Samar', '0920-295-0169', 'bplo.lgucatarman@gmail.com', 'Mon-Fri 8:00 AM - 5:00 PM', 'New/renew business permit, retirement, certified true copies'),
    (4, 'Municipal Treasurer''s Office (MTO)', 'Local Taxation', 'Municipal Hall, Catarman, Northern Samar', '(055) 500-1453', 'mto.lgu.catarman@gmail.com', 'Mon-Fri 8:00 AM - 5:00 PM', 'Community tax certificate (cedula), tax payments'),
    (5, 'Office of the Building Official (OBO)', 'Building and Construction', 'Municipal Hall, Catarman, Northern Samar', '(055) 500-0713', 'engineering.lgucatarman@gmail.com', 'Mon-Fri 8:00 AM - 5:00 PM', 'Building permit, certificate of occupancy, electrical permit'),
    (6, 'Municipal Assessor''s Office (MASSO)', 'Real Property Assessment', 'Municipal Hall, Catarman, Northern Samar', '(055) 500-2042', 'massocatarmanns@gmail.com', 'Mon-Fri 8:00 AM - 5:00 PM', 'Transfer of ownership, tax declaration, certifications'),
    (7, 'Municipal Planning and Development Office (MPDO)', 'Planning and Zoning', 'Municipal Hall, Catarman, Northern Samar', '(055) 500-1192', 'mpdocatarmannsamar@gmail.com', 'Mon-Fri 8:00 AM - 5:00 PM', 'Locational clearance, zoning certification'),
    (8, 'Municipal Health Office (MHO)', 'Public Health', 'Rural Health Unit, Catarman, Northern Samar', '(055) 500-9615', 'mho.lgu.catarman@gmail.com', 'Mon-Fri 8:00 AM - 5:00 PM', 'Public health services and programs'),
    (9, 'Municipal Social Welfare and Development Office (MSWDO)', 'Social Welfare', 'Municipal Hall, Catarman, Northern Samar', '0928-479-4710', 'minnielldurens@yahoo.com', 'Mon-Fri 8:00 AM - 5:00 PM', 'Social welfare and development services'),
    (10, 'Municipal Environment and Natural Resources Office (MENRO)', 'Environment', 'Municipal Hall, Catarman, Northern Samar', '(055) 500-1735', 'menrocatarman@gmail.com', 'Mon-Fri 8:00 AM - 5:00 PM', 'Environmental protection and solid waste management'),
    (11, 'Municipal Disaster Risk Reduction and Management Office (MDRRMO)', 'Disaster Management', 'Municipal Hall, Catarman, Northern Samar', '0906-357-0985', 'catarman.mdrrmo@yahoo.com', 'Mon-Fri 8:00 AM - 5:00 PM', 'Disaster preparedness and response'),
    (12, 'Municipal Tourism Office (MTOU)', 'Tourism', 'Municipal Hall, Catarman, Northern Samar', '0915-406-1346', 'turismosacatarman@gmail.com', 'Mon-Fri 8:00 AM - 5:00 PM', 'Tourism promotion and information');

-- Sample application for demo (Death Certificate)
INSERT INTO applications (id, reference_number, service_id, department_id, full_name, email, mobile, address, form_data, status, created_at, updated_at) VALUES
    (1, 'CAT-DC-DEMO1234', 1, 1, 'Juan Dela Cruz', 'juan@example.com', '09171234567', 'Catarman, Northern Samar', '{"deceasedName":"Pedro Dela Cruz","dateOfDeath":"2026-09-01","relationship":"Child","purpose":"Legal affairs","contactMethod":"Email"}', 'Under Review', datetime('now', '-2 days'), datetime('now', '-1 day'));

INSERT INTO application_history (application_id, staff_id, old_status, new_status, remarks, created_at) VALUES
    (1, NULL, 'Submitted', 'Received', 'Application received.', datetime('now', '-2 days')),
    (1, 2, 'Received', 'Under Review', 'Validating submitted details.', datetime('now', '-1 day'));
