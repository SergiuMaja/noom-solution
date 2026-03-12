#!/bin/bash
# Sleep Logger API - Test Script
# Prerequisites: docker-compose up --build

BASE_URL="http://localhost:8080/api/sleep"
USER_ID=1

echo "=== 1. Create sleep log ==="
curl -s -w "\nHTTP Status: %{http_code}\n" -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: $USER_ID" \
  -d '{
    "bedTime": "22:53:00",
    "wakeTime": "07:05:00",
    "feeling": "GOOD"
  }' | jq . 2>/dev/null || cat

echo ""
echo "=== 2. Get last night's sleep ==="
curl -s -w "\nHTTP Status: %{http_code}\n" "$BASE_URL/last-night" \
  -H "X-User-Id: $USER_ID" | jq . 2>/dev/null || cat

echo ""
echo "=== 3. Get 30-day averages ==="
curl -s -w "\nHTTP Status: %{http_code}\n" "$BASE_URL/averages?days=30" \
  -H "X-User-Id: $USER_ID" | jq . 2>/dev/null || cat

echo ""
echo "=== 4. Duplicate create (should return 409) ==="
curl -s -w "\nHTTP Status: %{http_code}\n" -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: $USER_ID" \
  -d '{
    "bedTime": "23:00:00",
    "wakeTime": "06:00:00",
    "feeling": "BAD"
  }' | jq . 2>/dev/null || cat

echo ""
echo "=== 5. Get sleep for user with no data (should return 404) ==="
curl -s -w "\nHTTP Status: %{http_code}\n" "$BASE_URL/last-night" \
  -H "X-User-Id: 999" | jq . 2>/dev/null || cat
