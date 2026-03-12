#!/bin/bash
# Sleep Logger API - Test Script
# Prerequisites: docker-compose up --build

BASE_URL="http://localhost:8080/api/sleep"
USER_ID=1

run() {
  local response http_code body
  response=$(curl -s -w "\n%{http_code}" "$@")
  http_code=$(echo "$response" | tail -1)
  body=$(echo "$response" | sed '$d')
  echo "$body" | jq . 2>/dev/null || echo "$body"
  echo "HTTP Status: $http_code"
}

echo "=== 1. Create sleep log ==="
run -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: $USER_ID" \
  -d '{
    "bedTime": "22:53:00",
    "wakeTime": "07:05:00",
    "feeling": "GOOD"
  }'

echo ""
echo "=== 2. Get last night's sleep ==="
run "$BASE_URL/last-night" \
  -H "X-User-Id: $USER_ID"

echo ""
echo "=== 3. Get 30-day averages ==="
run "$BASE_URL/averages?days=30" \
  -H "X-User-Id: $USER_ID"

echo ""
echo "=== 4. Duplicate create (should return 409) ==="
run -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: $USER_ID" \
  -d '{
    "bedTime": "23:00:00",
    "wakeTime": "06:00:00",
    "feeling": "BAD"
  }'

echo ""
echo "=== 5. Get sleep for user with no data (should return 404) ==="
run "$BASE_URL/last-night" \
  -H "X-User-Id: 999"

echo ""
read -p "Press Enter to close..."
