#!/bin/bash
USER_RES=$(curl -s -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d '{"phone": "9876543212"}')
# Wait, auth/login does NOT return an accessToken! It returns AuthUserResponse.
# To get an accessToken, you MUST call /auth/select-school or /auth/login/student-credentials or verify-otp.
echo "USER_RES: $USER_RES"
