#!/bin/bash
TOKEN=$(curl -s -X POST -H "Content-Type: application/json" -d '{"email":"deepanshusondkar3@gmail.com","phone":"9999999999","password":"password123"}' http://localhost:8080/auth/login | jq -r .data.id)
# Actually auth/login doesn't return a token, it returns a user and requires OTP. 
