#!/bin/bash
set -e
echo "1. Init Draft..."
curl -s -X POST http://localhost:8080/api/v1/onboarding/init \
-H "Content-Type: application/json" \
-d '{"schoolName": "Test Academy", "schoolCode": "TESTACAD", "boardType": "CBSE", "principalEmail": "admin@testacad.com", "adminPhone": "9876543210"}' > draft_init.json

SCHOOL_ID=$(cat draft_init.json | grep -o '"schoolId":[0-9]*' | cut -d: -f2)
echo "Got School ID: $SCHOOL_ID"

echo "2. Save Step 2..."
curl -s -X PUT http://localhost:8080/api/v1/onboarding/draft/$SCHOOL_ID/step/2 \
-H "Content-Type: application/json" \
-d '{"signedBy": "John Doe", "designation": "Principal", "agreementDate": "2024-05-20", "documents": []}' > step2.json

echo "3. Activate School..."
curl -s -X POST http://localhost:8080/api/v1/onboarding/activate/$SCHOOL_ID \
-H "Content-Type: application/json" > activate.json

cat activate.json | jq .
