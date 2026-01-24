#!/bin/bash

# Spring AI Function-Calling 테스트 스크립트
PORT=8082

echo "======================================"
echo "Spring AI Function-Calling 테스트"
echo "포트: ${PORT}"
echo "======================================"
echo ""

# 1. 헬스체크
echo "1️⃣ 헬스체크 테스트"
curl -s http://localhost:${PORT}/api/chat/test
echo -e "\n"

# 2. 간단한 인사
echo "2️⃣ 간단한 대화 테스트"
curl -s -X POST http://localhost:${PORT}/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "안녕 웬즈데이!"}' | jq -r '.response'
echo -e "\n"

# 3. 날씨 질문
echo "3️⃣ 날씨 질문 테스트"
curl -s -X POST http://localhost:${PORT}/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "서울 날씨 어때?"}' | jq -r '.response'
echo -e "\n"

# 4. 계산 질문
echo "4️⃣ 계산 질문 테스트"
curl -s -X POST http://localhost:${PORT}/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "25 곱하기 48은 얼마야?"}' | jq -r '.response'
echo -e "\n"

# 5. 일반 질문
echo "5️⃣ 일반 질문 테스트"
curl -s -X POST http://localhost:${PORT}/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Spring AI에 대해 간단히 설명해줘"}' | jq -r '.response'
echo -e "\n"

echo "======================================"
echo "테스트 완료!"
echo "Web UI: http://localhost:${PORT}"
echo "======================================"

