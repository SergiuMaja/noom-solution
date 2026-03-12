#!/bin/bash
# Sleep Logger API - Test Script
# Prerequisites: docker-compose up --build

BASE_URL="http://localhost:8080/api/sleep"
USER_ID=1
YESTERDAY=$(date -d "yesterday" +%Y-%m-%d 2>/dev/null || date -v-1d +%Y-%m-%d)

run() {
  local response http_code body
  response=$(curl -s -w "\n%{http_code}" "$@")
  http_code=$(echo "$response" | tail -1)
  body=$(echo "$response" | sed '$d')
  echo "$body" | jq . 2>/dev/null || echo "$body"
  echo "HTTP Status: $http_code"
}

echo "=== 1. Create yesterday's sleep log (4h, BAD) ==="
run -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: $USER_ID" \
  -d "{
    \"sleepDate\": \"$YESTERDAY\",
    \"bedTime\": \"02:00:00\",
    \"wakeTime\": \"06:00:00\",
    \"feeling\": \"BAD\"
  }"

echo ""
echo "=== 2. Create today's sleep log (8h 30min, GOOD) ==="
run -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: $USER_ID" \
  -d '{
    "bedTime": "22:30:00",
    "wakeTime": "07:00:00",
    "feeling": "GOOD"
  }'

echo ""
echo "=== 3. Get last night's sleep ==="
run "$BASE_URL/last-night" \
  -H "X-User-Id: $USER_ID"

echo ""
echo "=== 4. Get 30-day averages (expected: 6h 15min avg from 4h + 8h30) ==="
run "$BASE_URL/averages?days=30" \
  -H "X-User-Id: $USER_ID"

echo ""
echo "=== 5. Duplicate create (should return 409) ==="
run -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: $USER_ID" \
  -d '{
    "bedTime": "23:00:00",
    "wakeTime": "06:00:00",
    "feeling": "BAD"
  }'

echo ""
echo "=== 6. Get sleep for user with no data (should return 404) ==="
run "$BASE_URL/last-night" \
  -H "X-User-Id: 999"

echo ""
read -p "Press Enter to close..."
