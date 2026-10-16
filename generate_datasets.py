import csv
import random
import os

random.seed(42)

os.makedirs("app/src/main/assets/datasets", exist_ok=True)

# 1. Generate rooms.csv (120 rooms)
buildings = [
    ("Engineering Complex", "ENG", 25),
    ("Science Hall", "SCI", 22),
    ("Business Tower", "BUS", 20),
    ("Computing Center", "CMP", 20),
    ("Arts Pavilion", "ART", 18),
    ("University Library", "LIB", 15)
]

room_types = [
    ("Classroom", 0.35, (25, 60)),
    ("Lecture Hall", 0.15, (80, 260)),
    ("Computer Lab", 0.15, (24, 45)),
    ("Science Lab", 0.10, (20, 35)),
    ("Seminar Room", 0.15, (15, 30)),
    ("Conference Room", 0.05, (12, 25)),
    ("Study Lounge", 0.05, (20, 50))
]

rooms = []
for b_name, b_code, count in buildings:
    for i in range(1, count + 1):
        floor = ((i - 1) % 4) + 1
        room_num = f"{floor}{i:02d}"
        room_id = f"{b_code}-{room_num}"
        
        r_rand = random.random()
        cumulative = 0
        selected_type = "Classroom"
        cap_range = (25, 60)
        for rt, prob, crange in room_types:
            cumulative += prob
            if r_rand <= cumulative:
                selected_type = rt
                cap_range = crange
                break
        
        if "Lab" in selected_type and b_code == "CMP":
            selected_type = "Computer Lab"
            cap_range = (25, 50)
        elif "Lab" in selected_type and b_code == "SCI":
            selected_type = "Science Lab"
            cap_range = (20, 40)
        elif b_code == "LIB" and random.random() < 0.4:
            selected_type = "Study Lounge"
            cap_range = (20, 40)

        capacity = random.randint(cap_range[0], cap_range[1])
        has_projector = selected_type in ["Lecture Hall", "Classroom", "Computer Lab", "Conference Room"] or random.random() < 0.75
        has_computers = selected_type == "Computer Lab" or (b_code == "CMP" and random.random() < 0.6)
        is_accessible = random.random() < 0.85 or floor == 1
        has_audio = selected_type == "Lecture Hall" or capacity >= 60 or random.random() < 0.4
        has_whiteboard = selected_type != "Study Lounge" or random.random() < 0.8
        
        rooms.append({
            "room_id": room_id,
            "building": b_name,
            "floor": floor,
            "room_type": selected_type,
            "capacity": capacity,
            "has_projector": str(has_projector).lower(),
            "has_computers": str(has_computers).lower(),
            "is_accessible": str(is_accessible).lower(),
            "has_audio_system": str(has_audio).lower(),
            "has_whiteboard": str(has_whiteboard).lower()
        })

assert len(rooms) == 120, f"Expected 120 rooms, got {len(rooms)}"

with open("app/src/main/assets/datasets/rooms.csv", "w", newline="") as f:
    writer = csv.DictWriter(f, fieldnames=list(rooms[0].keys()))
    writer.writeheader()
    writer.writerows(rooms)

# 2. Generate class_schedule.csv (5,000 class records across semester)
courses_pool = [
    ("CS-101", "Introduction to Computing", "Computer Science", ["CMP", "ENG"]),
    ("CS-201", "Data Structures & Algorithms", "Computer Science", ["CMP", "ENG"]),
    ("CS-310", "Database Management Systems", "Computer Science", ["CMP"]),
    ("CS-450", "Machine Learning & AI", "Computer Science", ["CMP", "ENG"]),
    ("ENG-110", "Engineering Mechanics", "Engineering", ["ENG"]),
    ("ENG-220", "Thermodynamics", "Engineering", ["ENG", "SCI"]),
    ("ENG-330", "Circuit Analysis", "Engineering", ["ENG"]),
    ("SCI-101", "General Chemistry I", "Sciences", ["SCI"]),
    ("SCI-102", "General Physics I", "Sciences", ["SCI", "ENG"]),
    ("SCI-205", "Organic Chemistry", "Sciences", ["SCI"]),
    ("SCI-301", "Molecular Biology", "Sciences", ["SCI"]),
    ("BUS-100", "Principles of Management", "Business", ["BUS"]),
    ("BUS-210", "Financial Accounting", "Business", ["BUS"]),
    ("BUS-320", "Business Analytics", "Business", ["BUS", "CMP"]),
    ("BUS-440", "Strategic Management", "Business", ["BUS"]),
    ("ART-105", "Art History Survey", "Arts", ["ART"]),
    ("ART-215", "Digital Media Design", "Arts", ["ART", "CMP"]),
    ("ART-300", "Visual Storytelling", "Arts", ["ART"]),
    ("MAT-150", "Calculus I", "Mathematics", ["ENG", "SCI", "BUS"]),
    ("MAT-250", "Linear Algebra", "Mathematics", ["ENG", "CMP", "SCI"]),
    ("STA-200", "Applied Statistics", "Mathematics", ["BUS", "CMP", "SCI"]),
]

time_slots = [
    ("08:00", "09:30"), ("09:30", "11:00"), ("11:00", "12:30"),
    ("12:30", "14:00"), ("14:00", "15:30"), ("15:30", "17:00"),
    ("17:00", "18:30"), ("18:30", "20:00")
]

days_of_week = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"]
semesters = ["Fall-A", "Fall-B"]

class_schedules = []
sched_id = 1

# Generate up to 5000 valid semester course schedule blocks
for sem in semesters:
    for d in days_of_week:
        for r in rooms:
            if r["room_type"] not in ["Classroom", "Lecture Hall", "Computer Lab", "Science Lab", "Seminar Room"]:
                continue
            # Pick 2-4 slots for this room on this day
            num_slots = random.randint(2, 4)
            chosen_slots = random.sample(time_slots, num_slots)
            for slot in chosen_slots:
                if len(class_schedules) >= 5000:
                    break
                c_code, c_name, dept, _ = random.choice(courses_pool)
                enrolled = max(10, int(r["capacity"] * random.uniform(0.55, 0.98)))
                class_schedules.append({
                    "schedule_id": f"SCH-{sched_id:05d}",
                    "course_code": c_code,
                    "course_name": c_name,
                    "department": dept,
                    "room_id": r["room_id"],
                    "day_of_week": d,
                    "start_time": slot[0],
                    "end_time": slot[1],
                    "enrolled_students": enrolled
                })
                sched_id += 1
        if len(class_schedules) >= 5000:
            break
    if len(class_schedules) >= 5000:
        break

# If slightly under 5000, top up consistently
while len(class_schedules) < 5000:
    r = random.choice(rooms)
    d = random.choice(days_of_week)
    slot = random.choice(time_slots)
    c_code, c_name, dept, _ = random.choice(courses_pool)
    enrolled = max(10, int(r["capacity"] * random.uniform(0.55, 0.95)))
    class_schedules.append({
        "schedule_id": f"SCH-{sched_id:05d}",
        "course_code": c_code,
        "course_name": c_name,
        "department": dept,
        "room_id": r["room_id"],
        "day_of_week": d,
        "start_time": slot[0],
        "end_time": slot[1],
        "enrolled_students": enrolled
    })
    sched_id += 1

with open("app/src/main/assets/datasets/class_schedule.csv", "w", newline="") as f:
    writer = csv.DictWriter(f, fieldnames=list(class_schedules[0].keys()))
    writer.writeheader()
    writer.writerows(class_schedules)

# 3. Generate room_requests.csv (5,000 requests)
roles = ["Student", "Club Coordinator", "Faculty", "Facilities Admin"]
role_weights = [0.35, 0.30, 0.25, 0.10]

org_pool = {
    "Student": ["Study Guild", "Peer Tutoring Group", "Capstone Team Alpha", "Senior Project Group", "Graduate Scholars"],
    "Club Coordinator": ["ACM Student Chapter", "Robotics Society", "Finance & Investment Club", "Women in STEM", "Debate Union", "AI Builders", "Design Collective"],
    "Faculty": ["Dept of Computer Science", "Engineering Faculty Council", "Business School Advisory", "Life Sciences Faculty", "Academic Senate"],
    "Facilities Admin": ["Campus Maintenance Operations", "Registrar Scheduling Bureau", "IT Infrastructure Services", "Safety & Emergency Management"]
}

req_titles = {
    "Student": ["Group Exam Review", "Senior Capstone Working Session", "Algorithm Practice Lab", "Reading Group Meetup", "Team Presentation Rehearsal"],
    "Club Coordinator": ["General Body Meeting", "Technical Workshop & Coding Jam", "Alumni Networking Night", "Guest Speaker Panel", "Hackathon Preparation", "Annual Showcase"],
    "Faculty": ["Curriculum Review Board", "Guest Colloquium Lecture", "Departmental Research Symposium", "Faculty Committee Meeting", "Thesis Defense"],
    "Facilities Admin": ["Audio-Visual Systems Upgrade", "HVAC Maintenance Window", "Fire Safety Compliance Review", "Accessibility Inspection", "Term Seating Reconfiguration"]
}

request_slots = [
    ("08:00", "09:30"), ("09:00", "11:00"), ("10:00", "11:30"),
    ("11:30", "13:00"), ("13:00", "14:30"), ("14:00", "16:00"),
    ("15:30", "17:00"), ("16:00", "18:00"), ("17:30", "19:30"),
    ("18:00", "20:00"), ("19:00", "21:00")
]

room_requests = []
all_days = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"]

for r_id in range(1, 5001):
    role = random.choices(roles, weights=role_weights, k=1)[0]
    org = random.choice(org_pool[role])
    title = random.choice(req_titles[role])
    day = random.choice(all_days)
    month = random.randint(9, 12)
    day_num = random.randint(1, 28)
    req_date = f"2026-{month:02d}-{day_num:02d}"
    slot = random.choice(request_slots)
    
    if role == "Faculty":
        expected_att = random.randint(15, 90)
        prio = random.choice([3, 4, 5])
        req_type = random.choice(["Seminar Room", "Conference Room", "Lecture Hall", "Classroom"])
    elif role == "Club Coordinator":
        expected_att = random.randint(25, 160)
        prio = random.choice([2, 3, 4])
        req_type = random.choice(["Lecture Hall", "Classroom", "Computer Lab", "Study Lounge"])
    elif role == "Facilities Admin":
        expected_att = random.randint(5, 30)
        prio = random.choice([4, 5])
        req_type = random.choice(["Classroom", "Computer Lab", "Science Lab", "Conference Room"])
    else: # Student
        expected_att = random.randint(6, 40)
        prio = random.choice([1, 2, 3])
        req_type = random.choice(["Study Lounge", "Seminar Room", "Classroom", "Computer Lab"])

    need_proj = random.random() < 0.65
    need_comp = req_type == "Computer Lab" or (random.random() < 0.25)
    need_acc = random.random() < 0.35
    status_choice = random.choices(["Approved", "Pending", "Waitlisted", "Rejected"], weights=[0.58, 0.22, 0.12, 0.08], k=1)[0]
    
    room_requests.append({
        "request_id": f"REQ-{r_id:05d}",
        "requester_role": role,
        "organization_name": org,
        "title": title,
        "requested_date": req_date,
        "day_of_week": day,
        "start_time": slot[0],
        "end_time": slot[1],
        "expected_attendance": expected_att,
        "required_room_type": req_type,
        "requires_projector": str(need_proj).lower(),
        "requires_computers": str(need_comp).lower(),
        "requires_accessible": str(need_acc).lower(),
        "priority": prio,
        "status": status_choice
    })

with open("app/src/main/assets/datasets/room_requests.csv", "w", newline="") as f:
    writer = csv.DictWriter(f, fieldnames=list(room_requests[0].keys()))
    writer.writeheader()
    writer.writerows(room_requests)

# 4. Generate events.csv (1,800 historical verified records)
event_categories = [
    ("Academic Workshop", 0.22, 0.88),
    ("Club Meeting", 0.28, 0.72),
    ("Career Fair", 0.08, 0.95),
    ("Seminar", 0.18, 0.82),
    ("Study Group", 0.12, 0.85),
    ("Hackathon/Contest", 0.07, 0.92),
    ("Faculty Assembly", 0.05, 0.89)
]

events = []
for ev_id in range(1, 1801):
    r_type_sel = random.random()
    cum = 0
    e_type = "Club Meeting"
    att_factor = 0.80
    for name, prob, factor in event_categories:
        cum += prob
        if r_type_sel <= cum:
            e_type = name
            att_factor = factor
            break

    target_room = random.choice(rooms)
    cap = target_room["capacity"]
    
    expected_att = random.randint(max(10, int(cap * 0.3)), int(cap * 0.95))
    start_hour = random.choice([9, 10, 11, 13, 14, 15, 16, 17, 18, 19])
    duration = random.choice([1.0, 1.5, 2.0, 2.5, 3.0])
    day = random.choice(["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"])
    is_weekend = 1 if day in ["Saturday", "Sunday"] else 0
    
    hour_effect = 1.05 if (13 <= start_hour <= 17) else (0.92 if start_hour >= 18 else 0.96)
    duration_effect = 1.0 - (duration - 1.5) * 0.03
    type_bonus = 1.08 if e_type in ["Career Fair", "Hackathon/Contest"] else (0.94 if e_type == "Club Meeting" else 1.0)
    weekend_effect = 1.10 if (is_weekend and e_type in ["Hackathon/Contest", "Club Meeting"]) else (0.85 if is_weekend else 1.0)
    
    true_ratio = att_factor * hour_effect * duration_effect * type_bonus * weekend_effect
    noise = random.gauss(0, 4.5)
    raw_actual = int(round(expected_att * true_ratio + noise))
    actual_att = max(5, min(int(cap * 1.05), raw_actual))
    
    month = random.randint(1, 12)
    day_m = random.randint(1, 28)
    e_date = f"2026-{month:02d}-{day_m:02d}"
    
    events.append({
        "event_id": f"EVT-{ev_id:05d}",
        "event_name": f"{e_type} #{ev_id}",
        "event_type": e_type,
        "date": e_date,
        "day_of_week": day,
        "start_hour": start_hour,
        "duration_hours": duration,
        "room_id": target_room["room_id"],
        "expected_attendance": expected_att,
        "actual_attendance": actual_att,
        "host_organization": random.choice(org_pool["Club Coordinator"] + org_pool["Faculty"])
    })

with open("app/src/main/assets/datasets/events.csv", "w", newline="") as f:
    writer = csv.DictWriter(f, fieldnames=list(events[0].keys()))
    writer.writeheader()
    writer.writerows(events)

# 5. README.txt
readme_content = """CAMPUSSPACE AI - OFFICIAL DATASET DOCUMENTATION & RULES
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
"""

with open("app/src/main/assets/datasets/README.txt", "w") as f:
    f.write(readme_content.strip())

print("Datasets generated successfully!")
