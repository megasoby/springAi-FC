#!/bin/bash

# MongoDB 조회 헬퍼 스크립트
# 사용법: ./mongo-query.sh [명령어]

CONTAINER="mongodb"
USER="admin"
PASS="admin123"
DB="chatdb"

case "${1:-help}" in
    "count")
        echo "📊 저장된 세션 수:"
        docker exec $CONTAINER mongosh -u $USER -p $PASS --authenticationDatabase admin $DB \
            --eval "db.conversation_sessions.countDocuments()" --quiet
        ;;
    
    "list")
        echo "📋 모든 세션 목록 (요약):"
        docker exec $CONTAINER mongosh -u $USER -p $PASS --authenticationDatabase admin $DB \
            --eval "db.conversation_sessions.find({}, {sessionId: 1, createdAt: 1, 'messages': {\$slice: 1}}).pretty()" --quiet
        ;;
    
    "recent")
        echo "🕐 최근 세션 5개:"
        docker exec $CONTAINER mongosh -u $USER -p $PASS --authenticationDatabase admin $DB \
            --eval "db.conversation_sessions.find().sort({lastAccessedAt: -1}).limit(5).pretty()" --quiet
        ;;
    
    "stats")
        echo "📈 통계 정보:"
        docker exec $CONTAINER mongosh -u $USER -p $PASS --authenticationDatabase admin $DB \
            --eval "
                var total = db.conversation_sessions.countDocuments();
                var withMessages = db.conversation_sessions.countDocuments({'messages.0': {\$exists: true}});
                var empty = total - withMessages;
                print('전체 세션: ' + total);
                print('대화 있는 세션: ' + withMessages);
                print('빈 세션: ' + empty);
            " --quiet
        ;;
    
    "clear-empty")
        echo "🗑️ 빈 세션 삭제:"
        docker exec $CONTAINER mongosh -u $USER -p $PASS --authenticationDatabase admin $DB \
            --eval "db.conversation_sessions.deleteMany({messages: {\$size: 0}})" --quiet
        ;;
    
    "session")
        if [ -z "$2" ]; then
            echo "❌ 사용법: ./mongo-query.sh session [세션ID]"
            exit 1
        fi
        echo "🔍 세션 상세 정보:"
        docker exec $CONTAINER mongosh -u $USER -p $PASS --authenticationDatabase admin $DB \
            --eval "db.conversation_sessions.findOne({sessionId: '$2'}).pretty()" --quiet
        ;;
    
    "shell")
        echo "🐚 MongoDB Shell 접속 중..."
        docker exec -it $CONTAINER mongosh -u $USER -p $PASS --authenticationDatabase admin $DB
        ;;
    
    "help"|*)
        echo "MongoDB 조회 헬퍼 스크립트"
        echo ""
        echo "사용법: ./mongo-query.sh [명령어]"
        echo ""
        echo "명령어:"
        echo "  count         - 세션 개수 확인"
        echo "  list          - 세션 목록 보기 (요약)"
        echo "  recent        - 최근 세션 5개"
        echo "  stats         - 통계 정보"
        echo "  clear-empty   - 빈 세션 삭제"
        echo "  session [ID]  - 특정 세션 상세 보기"
        echo "  shell         - MongoDB 셸 접속"
        echo "  help          - 도움말"
        echo ""
        ;;
esac
