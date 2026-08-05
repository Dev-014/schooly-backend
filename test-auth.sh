#!/bin/bash

# Wait for backend to start
while ! curl -s http://localhost:8080/health > /dev/null; do
  sleep 1
done

# Login as super admin
echo "Logging in..."
LOGIN_RES=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phone": "9999999999", "password": "password"}')

USER_ID=$(echo $LOGIN_RES | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

# Select school to get token
echo "Selecting school for user $USER_ID..."
TOKEN_RES=$(curl -s -X POST http://localhost:8080/auth/select-school \
  -H "Content-Type: application/json" \
  -d "{\"userId\": $USER_ID, \"schoolId\": 1, \"role\": \"SUPER_ADMIN\"}")

TOKEN=$(echo $TOKEN_RES | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
echo "TOKEN: $TOKEN"

# Request roles
echo "Requesting roles..."
curl -i -s -X GET http://localhost:8080/api/v1/super-admin/users/roles \
  -H "Authorization: Bearer $TOKEN"
