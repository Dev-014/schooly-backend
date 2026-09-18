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

TOKEN=$(echo $LOGIN_RES | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

# Impersonate User ID 3
echo "Impersonating user ID 3..."
IMP_RES=$(curl -s -X POST http://localhost:8080/api/v1/super-admin/users/3/impersonate \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json")

echo "IMP_RES: $IMP_RES"

IMP_TOKEN=$(echo $IMP_RES | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -n "$IMP_TOKEN" ]; then
    echo "Stopping impersonation..."
    STOP_RES=$(curl -s -X POST http://localhost:8080/auth/stop-impersonation \
      -H "Authorization: Bearer $IMP_TOKEN" \
      -H "Content-Type: application/json")
    echo "STOP_RES: $STOP_RES"
else
    echo "Impersonation failed!"
fi
