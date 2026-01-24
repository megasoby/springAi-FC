# Docker Compose 사용 가이드

## 🚀 MongoDB 시작/종료

### 시작
```bash
docker-compose up -d
```

### 종료
```bash
docker-compose down
```

### 재시작
```bash
docker-compose restart
```

### 로그 확인
```bash
docker-compose logs -f mongodb
```

### 상태 확인
```bash
docker-compose ps
```

---

## 🔧 설정 정보

### MongoDB
- **Host**: localhost
- **Port**: 27017
- **Username**: admin
- **Password**: admin123
- **Database**: chatdb
- **Connection String**: `mongodb://admin:admin123@localhost:27017/?authSource=admin`

### Mongo Express (웹 UI)
- **URL**: http://localhost:8081
- **Username**: admin
- **Password**: admin123

> ⚠️ Mongo Express는 선택사항입니다. 이미 MongoDB Compass를 사용 중이므로 필요 없으면 docker-compose.yml에서 주석 처리하세요.

---

## 📊 데이터 관리

### 볼륨 확인
```bash
docker volume ls | grep mongodb
```

### 데이터 완전 삭제 (주의!)
```bash
docker-compose down -v
```

### 백업
```bash
docker exec mongodb mongodump -u admin -p admin123 --authenticationDatabase admin -o /data/backup
docker cp mongodb:/data/backup ./mongodb-backup
```

### 복원
```bash
docker cp ./mongodb-backup mongodb:/data/backup
docker exec mongodb mongorestore -u admin -p admin123 --authenticationDatabase admin /data/backup
```

---

## 🔄 기존 MongoDB 교체

### 1. 기존 컨테이너 중지 및 삭제
```bash
docker stop mongodb
docker rm mongodb
```

### 2. Docker Compose로 시작
```bash
docker-compose up -d
```

---

## 🌐 네트워크

Spring Boot 애플리케이션도 Docker로 실행할 경우, 같은 네트워크(`springai-network`)를 사용하면 서로 통신할 수 있습니다.

---

## 💡 팁

- `restart: unless-stopped` 설정으로 서버 재부팅 시 자동 시작
- 볼륨으로 데이터 영구 보존
- 네트워크 분리로 보안 강화
