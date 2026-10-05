import json
import sys

catalog_path = sys.argv[1]

with open(catalog_path, 'r') as f:
    data = json.load(f)

# Ensure exams_results module exists
modules = data.get('modules', [])
if not any(m['key'] == 'exams_results' for m in modules):
    modules.append({
        "key": "exams_results",
        "name": "Exams & Results"
    })

permissions = data.get('permissions', [])
new_permissions = [
    {
      "key": "student.exams.timetable.view",
      "module": "exams_results",
      "resource": "student_exams",
      "action": "view_timetable",
      "name": "View Exam Timetable",
      "description": "Allows viewing of the exam timetable and schedule",
      "supportedScopes": ["own", "linked"],
      "sensitive": False,
      "status": "active"
    },
    {
      "key": "student.exams.admit_card.view",
      "module": "exams_results",
      "resource": "student_exams",
      "action": "view_admit_card",
      "name": "View Admit Card",
      "description": "Allows viewing and downloading of the exam admit card",
      "supportedScopes": ["own", "linked"],
      "sensitive": False,
      "status": "active"
    },
    {
      "key": "student.exams.results.view",
      "module": "exams_results",
      "resource": "student_exams",
      "action": "view_results",
      "name": "View Exam Results",
      "description": "Allows viewing of exam marks and downloading report cards",
      "supportedScopes": ["own", "linked"],
      "sensitive": True,
      "status": "active"
    },
    {
      "key": "student.exams.syllabus.view",
      "module": "exams_results",
      "resource": "student_exams",
      "action": "view_syllabus",
      "name": "View Exam Syllabus",
      "description": "Allows viewing of the exam syllabus",
      "supportedScopes": ["own", "linked", "class", "section"],
      "sensitive": False,
      "status": "active"
    }
]

# Check and add permissions if they don't exist
for np in new_permissions:
    if not any(p['key'] == np['key'] for p in permissions):
        permissions.append(np)

with open(catalog_path, 'w') as f:
    json.dump(data, f, indent=2)

print("auth-catalog.json updated successfully.")
