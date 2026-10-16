CAMPUSSPACE AI - OFFICIAL DATASET DOCUMENTATION & RULES
========================================================================

1. DATASET FILES & SCHEMAS:

A. rooms.csv (120 records):
   - room_id (string, Primary Key): Unique room code, e.g., 'ENG-101', 'SCI-204', 'CMP-301'.
   - building (string): Campus facility name ('Engineering Complex', 'Science Hall', 'Business Tower', 'Computing Center', 'Arts Pavilion', 'University Library').
   - floor (integer): 1 to 4.
   - room_type (string): 'Classroom', 'Lecture Hall', 'Computer Lab', 'Science Lab', 'Seminar Room', 'Conference Room', 'Study Lounge'.
   - capacity (integer): Maximum legal seating capacity (15 to 280).
   - has_projector (boolean): True if room features high-definition projection.
   - has_computers (boolean): True if equipped with dedicated student desktop workstations.
   - is_accessible (boolean): ADA compliant wheelchair accessibility.
   - has_audio_system (boolean): Integrated microphone and PA amplifier.
   - has_whiteboard (boolean): Magnetic dry-erase whiteboard / chalkboard.

B. class_schedule.csv (5,000 records):
   - schedule_id (string, Primary Key): Unique scheduled class entry ID.
   - course_code (string): e.g. 'CS-101', 'MAT-150', 'BUS-210'.
   - course_name (string): Official university course title.
   - department (string): Academic department offering the course.
   - room_id (string, Foreign Key -> rooms.room_id): Room assigned for the lecture/lab.
   - day_of_week (string): 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'.
   - start_time (string, HH:MM): Class commencement.
   - end_time (string, HH:MM): Class conclusion.
   - enrolled_students (integer): Number of officially registered students.

C. room_requests.csv (5,000 records):
   - request_id (string, Primary Key): Unique request identifier.
   - requester_role (string): 'Student', 'Club Coordinator', 'Faculty', 'Facilities Admin'.
   - organization_name (string): Name of student organization, club, department, or office.
   - title (string): Purpose/title of requested session.
   - requested_date (string, YYYY-MM-DD): Target calendar date.
   - day_of_week (string): Corresponding day of week.
   - start_time (string, HH:MM): Target session start.
   - end_time (string, HH:MM): Target session end.
   - expected_attendance (integer): Anticipated turnout specified by applicant.
   - required_room_type (string): Minimum room type qualification.
   - requires_projector (boolean): Mandatory projector requirement.
   - requires_computers (boolean): Mandatory computer lab requirement.
   - requires_accessible (boolean): Mandatory ADA accessibility requirement.
   - priority (integer): Priority level 1 (lowest) to 5 (highest, e.g. institutional exams/safety).
   - status (string): 'Approved', 'Pending', 'Waitlisted', 'Rejected'.
   * CAVEAT: Requests do not contain pre-assigned room_id! The allocation engine evaluates and assigns rooms.

D. events.csv (1,800 records):
   - event_id (string, Primary Key): Unique historical event ID.
   - event_name (string): Event name.
   - event_type (string): 'Academic Workshop', 'Club Meeting', 'Career Fair', 'Seminar', 'Study Group', 'Hackathon/Contest', 'Faculty Assembly'.
   - date (string, YYYY-MM-DD): Date event occurred.
   - day_of_week (string): Day of the week.
   - start_hour (integer): 24-hour clock hour (e.g. 9 for 09:00, 14 for 14:00).
   - duration_hours (float): Duration in hours (e.g. 1.5, 2.0).
   - room_id (string, Foreign Key -> rooms.room_id): Room where event occurred.
   - expected_attendance (integer): Headcount predicted before event.
   - actual_attendance (integer, Supervised ML Ground Truth Target): Verified turnout.
   - host_organization (string): Host entity.

2. SCHEDULING CONSTRAINTS & HALF-OPEN INTERVAL RULES:
   - Interval Representation: Time intervals are half-open: [start, end).
   - Overlap Rule: Two intervals [s1, e1) and [s2, e2) conflict iff max(s1, s2) < min(e1, e2).
     Adjacent bookings [10:00, 11:30) and [11:30, 13:00) DO NOT conflict.

3. ML ATTENDANCE PREDICTION TARGET:
   - Target variable: 'actual_attendance' from events.csv.
   - Valid input features: expected_attendance, start_hour, duration_hours, event_type, day_of_week.
   - Leakage prevention: Never include actual_attendance or post-event metrics in features.
   - Normalization statistics (mean, std) must be derived purely on training split (70%) and applied to validation (15%) and test (15%) splits.

4. RESPONSIBLE USE & SYNTHETIC DATA NOTICE:
   - This dataset represents synthetic educational benchmark data designed for campus optimization research.
   - Results demonstrate algorithmic efficacy and should be paired with verified institutional feeds in production.