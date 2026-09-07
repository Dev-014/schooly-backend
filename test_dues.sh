#!/bin/bash
USER_RES=$(curl -s -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d '{"phone": "9876543212"}')
USER_ID=$(echo $USER_RES | grep -o '"id":[^,]*' | head -n 1 | cut -d':' -f2)
SCHOOL_ID=$(echo $USER_RES | grep -o '"schoolId":[^,]*' | head -n 1 | cut -d':' -f2)
TOKEN_RES=$(curl -s -X POST http://localhost:8080/auth/select-school -H "Content-Type: application/json" -d "{\"userId\": $USER_ID, \"schoolId\": $SCHOOL_ID}")
TOKEN=$(echo $TOKEN_RES | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
curl -s -X GET "http://localhost:8080/api/v1/parent/fees/dues?studentId=12" -H "Authorization: Bearer $TOKEN"
