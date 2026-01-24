#!/bin/bash

# Spring AI Function-Calling 실행 스크립트
# 작성자: 송그랜트 & 웬즈데이

PORT=8082
APP_NAME="Spring AI Function-Calling"
PID_FILE="app.pid"
LOG_FILE="app.log"

# 사용법 출력
usage() {
    echo "사용법: $0 {start|stop|restart|status|logs}"
    echo ""
    echo "명령어:"
    echo "  start   - 애플리케이션을 백그라운드에서 시작"
    echo "  stop    - 실행 중인 애플리케이션 종료"
    echo "  restart - 애플리케이션 재시작"
    echo "  status  - 애플리케이션 실행 상태 확인"
    echo "  logs    - 실시간 로그 확인 (Ctrl+C로 종료)"
    echo ""
    exit 1
}

# 실행 중인지 확인
is_running() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p "$PID" > /dev/null 2>&1; then
            return 0
        fi
    fi
    return 1
}

# 상태 확인
status() {
    echo "======================================"
    echo "📊 ${APP_NAME} 상태"
    echo "======================================"
    if is_running; then
        PID=$(cat "$PID_FILE")
        echo "✅ 실행 중 (PID: $PID)"
        echo "🌐 웹 UI: http://localhost:${PORT}"
        echo "📡 API: http://localhost:${PORT}/api/chat"
        echo ""
        echo "메모리 사용량:"
        ps -o pid,rss,vsz,command -p "$PID" | awk 'NR==1 {print $0} NR>1 {printf "  PID: %s, RSS: %.2f MB, VSZ: %.2f MB\n", $1, $2/1024, $3/1024}'
    else
        echo "❌ 실행 중이 아님"
    fi
    echo "======================================"
}

# 중지
stop() {
    echo "======================================"
    echo "🛑 ${APP_NAME} 종료"
    echo "======================================"
    if is_running; then
        PID=$(cat "$PID_FILE")
        echo "종료 중... (PID: $PID)"
        kill "$PID" 2>/dev/null || true
        
        # 종료 대기 (최대 30초)
        for i in {1..30}; do
            if ! ps -p "$PID" > /dev/null 2>&1; then
                break
            fi
            sleep 1
            echo -n "."
        done
        echo ""
        
        # 강제 종료
        if ps -p "$PID" > /dev/null 2>&1; then
            echo "⚠️  정상 종료 실패, 강제 종료 중..."
            kill -9 "$PID" 2>/dev/null || true
            sleep 2
        fi
        
        rm -f "$PID_FILE"
        echo "✅ 종료 완료"
    else
        echo "❌ 실행 중인 프로세스가 없습니다."
    fi
    echo "======================================"
}

# 로그 확인
show_logs() {
    if [ -f "$LOG_FILE" ]; then
        echo "======================================"
        echo "📋 ${APP_NAME} 로그"
        echo "======================================"
        echo "Ctrl+C를 눌러 종료하세요."
        echo ""
        tail -f "$LOG_FILE"
    else
        echo "❌ 로그 파일이 없습니다: $LOG_FILE"
    fi
}

# 시작
start() {
    echo "======================================"
    echo "🚀 ${APP_NAME} 시작"
    echo "======================================"
    echo ""
    
    # 이미 실행 중인지 확인
    if is_running; then
        PID=$(cat "$PID_FILE")
        echo "⚠️  이미 실행 중입니다. (PID: $PID)"
        echo "종료하려면: $0 stop"
        exit 1
    fi
    
    # 1. 환경변수 체크
    echo "📋 1단계: 환경 설정 확인"
    echo "--------------------------------------"
    
    # AWS 자격증명 체크
    if [ -z "$AWS_ACCESS_KEY_ID" ]; then
        echo "⚠️  경고: AWS_ACCESS_KEY_ID 환경변수가 설정되지 않았습니다."
        echo "   AWS Bedrock을 사용하려면 다음 명령어를 실행하세요:"
        echo "   export AWS_ACCESS_KEY_ID='your-access-key'"
        echo ""
    fi
    
    if [ -z "$AWS_SECRET_ACCESS_KEY" ]; then
        echo "⚠️  경고: AWS_SECRET_ACCESS_KEY 환경변수가 설정되지 않았습니다."
        echo "   AWS Bedrock을 사용하려면 다음 명령어를 실행하세요:"
        echo "   export AWS_SECRET_ACCESS_KEY='your-secret-key'"
        echo ""
    fi
    
    if [ -n "$AWS_ACCESS_KEY_ID" ] && [ -n "$AWS_SECRET_ACCESS_KEY" ]; then
        echo "✅ AWS 자격증명 설정 완료"
    fi
    
    # OpenWeatherMap API 키 체크 (선택사항)
    if [ -z "$OPENWEATHER_API_KEY" ]; then
        echo "ℹ️  OpenWeatherMap API 키가 설정되지 않았습니다. (선택사항)"
        echo "   실제 날씨 정보를 사용하려면 설정하세요."
        echo "   자세한 내용: OPENWEATHER_SETUP.md 참고"
    else
        echo "✅ OpenWeatherMap API 키 설정 완료"
    fi
    
    echo ""
    
    # 2. Java 버전 체크
    echo "📋 2단계: Java 버전 확인"
    echo "--------------------------------------"
    if ! command -v java &> /dev/null; then
        echo "❌ Java가 설치되어 있지 않습니다."
        echo "   Java 17 이상을 설치해주세요."
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
    echo "✅ Java 버전: $(java -version 2>&1 | head -1)"
    
    if [ "$JAVA_VERSION" -lt 17 ]; then
        echo "❌ Java 17 이상이 필요합니다. (현재: Java $JAVA_VERSION)"
        exit 1
    fi
    echo ""
    
    # 3. 포트 사용 중 체크
    echo "📋 3단계: 포트 확인"
    echo "--------------------------------------"
    if lsof -Pi :${PORT} -sTCP:LISTEN -t >/dev/null 2>&1 ; then
        echo "⚠️  포트 ${PORT}가 이미 사용 중입니다."
        echo "🔄 기존 프로세스 종료 중..."
        kill -9 $(lsof -ti:${PORT}) 2>/dev/null || true
        sleep 2
        echo "✅ 기존 프로세스 종료 완료"
    else
        echo "✅ 포트 ${PORT} 사용 가능"
    fi
    echo ""
    
    # 4. Gradle Wrapper 실행 권한 확인
    echo "📋 4단계: Gradle Wrapper 권한 확인"
    echo "--------------------------------------"
    if [ ! -x "./gradlew" ]; then
        echo "🔧 gradlew 실행 권한 추가 중..."
        chmod +x ./gradlew
        echo "✅ 실행 권한 추가 완료"
    else
        echo "✅ gradlew 실행 권한 확인 완료"
    fi
    echo ""
    
    # 5. 애플리케이션 백그라운드 실행
    echo "======================================"
    echo "🚀 애플리케이션 백그라운드 시작"
    echo "======================================"
    echo ""
    echo "포트: ${PORT}"
    echo "웹 UI: http://localhost:${PORT}"
    echo "API: http://localhost:${PORT}/api/chat"
    echo "로그 파일: ${LOG_FILE}"
    echo ""
    
    # 백그라운드 실행
    nohup ./gradlew bootRun > "$LOG_FILE" 2>&1 &
    GRADLE_PID=$!
    
    # Gradle이 실제 애플리케이션을 시작할 때까지 대기
    echo "애플리케이션 시작 중..."
    sleep 3
    
    # 실제 Spring Boot 프로세스 PID 찾기
    for i in {1..30}; do
        SPRING_PID=$(ps aux | grep "springAi-FC" | grep -v grep | grep java | awk '{print $2}' | head -1)
        if [ -n "$SPRING_PID" ]; then
            echo "$SPRING_PID" > "$PID_FILE"
            echo "✅ 애플리케이션이 백그라운드에서 시작되었습니다!"
            echo "   PID: $SPRING_PID"
            echo ""
            echo "유용한 명령어:"
            echo "  상태 확인: $0 status"
            echo "  로그 확인: $0 logs"
            echo "  종료: $0 stop"
            echo "  재시작: $0 restart"
            echo ""
            echo "======================================"
            
            # 10초 후 로그 미리보기
            sleep 7
            echo ""
            echo "📋 최근 로그:"
            echo "--------------------------------------"
            tail -20 "$LOG_FILE"
            echo "--------------------------------------"
            return 0
        fi
        sleep 1
    done
    
    echo "❌ 애플리케이션 시작 실패"
    echo "로그를 확인하세요: tail -f $LOG_FILE"
    exit 1
}

# 재시작
restart() {
    echo "======================================"
    echo "🔄 ${APP_NAME} 재시작"
    echo "======================================"
    stop
    sleep 2
    start
}

# 메인 로직
case "${1:-}" in
    start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        restart
        ;;
    status)
        status
        ;;
    logs)
        show_logs
        ;;
    *)
        usage
        ;;
esac
