#!/bin/bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login -H "Content-Type: application/json" -d '{"email": "admin@example.com", "password": "password"}' | grep -o '"token":"[^"]*' | cut -d'"' -f4)
echo "Token: $TOKEN"
curl -v -H "Authorization: Bearer $TOKEN" 'http://localhost:8080/api/students?schoolId=4&page=0&size=20'
