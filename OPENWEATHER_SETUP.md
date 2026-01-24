# OpenWeatherMap API 키 발급 가이드

## 1. 회원가입 및 API 키 발급 (무료)

1. **OpenWeatherMap 사이트 접속**
   ```
   https://openweathermap.org/api
   ```

2. **Sign Up 클릭** (무료 계정)
   - 이메일, 비밀번호 입력
   - 이메일 인증

3. **API Keys 메뉴**
   - 우측 상단 계정 → API keys
   - 기본 키가 자동 생성되어 있음
   - 또는 "Create Key" 버튼으로 새로 생성

4. **API 키 복사**
   - 예시: `1234567890abcdef1234567890abcdef`

## 2. 환경변수 설정

### macOS/Linux (~/.zshrc 또는 ~/.bashrc)
```bash
export OPENWEATHER_API_KEY="your-api-key-here"
```

적용:
```bash
source ~/.zshrc
```

### Windows (시스템 환경변수)
```cmd
setx OPENWEATHER_API_KEY "your-api-key-here"
```

## 3. 확인

```bash
echo $OPENWEATHER_API_KEY
```

## 4. 애플리케이션 재시작

```bash
./gradlew bootRun
```

## ⚠️ 주의사항

- **무료 플랜**: 1분에 60회, 하루 1,000회 호출 제한
- **API 키 활성화**: 발급 후 10분 정도 소요될 수 있음
- **키가 없으면**: Mock 데이터 자동 반환 (테스트용)

## 📝 무료 플랜 상세

- ✅ 현재 날씨 조회
- ✅ 5일 예보 (3시간 단위)
- ✅ 하루 1,000회 호출
- ❌ 1분 이상 예보
- ❌ 과거 날씨 데이터

우리 프로젝트는 현재 날씨만 조회하므로 무료 플랜으로 충분합니다! 😊

