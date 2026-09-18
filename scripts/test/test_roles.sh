#!/bin/bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/verify-otp -H "Content-Type: application/json" -d '{"phone": "9999999999", "otp": "1111"}' | grep -o '"accessToken":"[^"]*' | grep -o '[^"]*$')
echo "Token: $TOKEN"
curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/roles?schoolId=101"
