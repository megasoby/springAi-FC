# 🍎 맥미니 신규 설치 가이드

Spring AI Function-Calling 프로젝트 실행을 위한 완벽한 환경 설정 가이드입니다.

---

## 📋 목차

1. [Homebrew 설치](#1-homebrew-설치)
2. [Java 17 설치](#2-java-17-설치)
3. [Docker Desktop 설치](#3-docker-desktop-설치)
4. [AWS 자격증명 설정](#4-aws-자격증명-설정)
5. [OpenWeather API 키 설정](#5-openweather-api-키-설정-선택사항)
6. [프로젝트 실행](#6-프로젝트-실행)
7. [설치 확인](#7-설치-확인)
8. [추가 개발 환경 설정](#8-추가-개발-환경-설정)
   - [Python 설치](#python-설치-pyenv)
   - [MongoDB Compass 설치](#mongodb-compass-설치)
   - [iTerm2 설치](#iterm2-설치)
   - [Oh My Zsh + agnoster 테마](#oh-my-zsh--agnoster-테마-설치)
   - [유틸리티 앱](#유틸리티-앱)

---

## 1. Homebrew 설치

macOS 패키지 관리자입니다. 이미 설치되어 있다면 다음 단계로 넘어가세요.

```bash
# Homebrew 설치
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# 설치 확인
brew --version
```

**Apple Silicon (M1/M2/M3) 추가 설정:**
```bash
# ~/.zshrc 파일에 Homebrew 경로 추가
echo 'eval "$(/opt/homebrew/bin/brew shellenv)"' >> ~/.zshrc
source ~/.zshrc
```

---

## 2. Java 17 설치

Spring Boot를 실행하기 위해 필요합니다.

```bash
# OpenJDK 17 설치
brew install openjdk@17

# Java 심볼릭 링크 생성 (시스템 전역에서 인식하도록)
sudo ln -sfn $(brew --prefix)/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk

# ~/.zshrc에 Java 경로 추가
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
echo 'export JAVA_HOME=$(/usr/libexec/java_home -v 17)' >> ~/.zshrc

# 설정 적용
source ~/.zshrc

# 설치 확인
java -version
```

**예상 출력:**
```
openjdk version "17.0.x" ...
```

---

## 3. Docker Desktop 설치

MongoDB를 컨테이너로 실행하기 위해 필요합니다.

```bash
# Docker Desktop 설치
brew install --cask docker

# Docker Desktop 앱 실행
open -a Docker
```

**⚠️ 중요:**
- Docker Desktop 앱이 실행되고 상단 바에 고래 아이콘이 나타날 때까지 기다려주세요
- 처음 실행 시 권한 승인이 필요할 수 있습니다

**설치 확인:**
```bash
# Docker 데몬이 실행 중인지 확인 (Docker Desktop 실행 후)
docker --version
docker ps
```

---

## 4. AWS 자격증명 설정

AWS Bedrock에서 Claude 모델을 사용하기 위해 필요합니다.

### 4-1. AWS 계정 및 액세스 키 준비

필요한 것:
- AWS Access Key ID
- AWS Secret Access Key
- Bedrock 서비스 활성화 (ap-northeast-2 리전)
- Claude 모델 액세스 권한

### 4-2. 환경 변수 설정

```bash
# ~/.zshrc 파일 편집
nano ~/.zshrc
```

**파일 맨 아래에 다음 내용 추가:**

```bash
# ============================================
# AWS Credentials for Bedrock
# ============================================
export AWS_ACCESS_KEY_ID="여기에_실제_액세스_키_입력"
export AWS_SECRET_ACCESS_KEY="여기에_실제_시크릿_키_입력"
export AWS_REGION="ap-northeast-2"
```

**저장하고 나가기:**
- `Ctrl + O` (저장)
- `Enter`
- `Ctrl + X` (나가기)

**설정 적용:**
```bash
source ~/.zshrc
```

**확인:**
```bash
echo $AWS_ACCESS_KEY_ID
echo $AWS_SECRET_ACCESS_KEY
echo $AWS_REGION
```

### 4-3. AWS Bedrock 설정 확인

AWS 콘솔에서 확인할 사항:
1. **Bedrock 서비스 활성화** (ap-northeast-2 리전)
2. **Claude 모델 액세스 요청**
   - Bedrock 콘솔 → Model access
   - Anthropic Claude Sonnet 4.5 활성화
3. **IAM 권한 확인**
   - `bedrock:InvokeModel` 권한 필요

---

## 5. OpenWeather API 키 설정 (선택사항)

실제 날씨 정보를 가져오려면 설정하세요. **없어도 Mock 데이터로 동작합니다.**

### 5-1. API 키 발급 (무료)

1. https://openweathermap.org/api 접속
2. **Sign Up** → 무료 계정 생성
3. 이메일 인증
4. **API Keys** 메뉴에서 키 복사

**무료 플랜 제한:**
- 1분에 60회 호출
- 하루 1,000회 호출
- 현재 날씨 조회 가능

### 5-2. 환경 변수 설정

```bash
# ~/.zshrc에 추가
echo '' >> ~/.zshrc
echo '# ============================================' >> ~/.zshrc
echo '# OpenWeatherMap API Key' >> ~/.zshrc
echo '# ============================================' >> ~/.zshrc
echo 'export OPENWEATHER_API_KEY="여기에_실제_API_키_입력"' >> ~/.zshrc

# 설정 적용
source ~/.zshrc

# 확인
echo $OPENWEATHER_API_KEY
```

---

## 6. 프로젝트 실행

모든 설치가 완료되었으면 프로젝트를 실행합니다.

### 6-1. MongoDB 컨테이너 실행

```bash
# 프로젝트 디렉토리로 이동
cd /Users/megasoby/Work/github/springAi-FC

# Docker Compose로 MongoDB 실행
docker-compose up -d

# 컨테이너 상태 확인
docker-compose ps
```

**예상 출력:**
```
NAME                IMAGE               STATUS
mongodb            mongo:latest         Up
mongo-express      mongo-express:latest Up
```

### 6-2. Spring Boot 애플리케이션 실행

**방법 1: Gradle로 실행 (권장 - 처음 실행)**
```bash
# Gradle Wrapper 실행 권한 부여
chmod +x gradlew

# 애플리케이션 실행
./gradlew bootRun
```

**방법 2: 백그라운드 실행 스크립트**
```bash
# 백그라운드 실행
./run.sh start

# 상태 확인
./run.sh status

# 로그 확인
./run.sh logs

# 중지
./run.sh stop

# 재시작
./run.sh restart
```

### 6-3. 웹 UI 접속

브라우저에서 다음 주소로 접속:
```
http://localhost:8082
```

**추가 접속 주소:**
- **Mongo Express** (MongoDB 웹 UI): http://localhost:8081
  - Username: `admin`
  - Password: `admin123`

---

## 7. 설치 확인

### 7-1. 전체 환경 확인 스크립트

```bash
# 모든 설치 상태 한번에 확인
echo "=== Homebrew ==="
brew --version

echo -e "\n=== Java ==="
java -version

echo -e "\n=== Docker ==="
docker --version
docker ps

echo -e "\n=== AWS Credentials ==="
echo "AWS_ACCESS_KEY_ID: ${AWS_ACCESS_KEY_ID:0:10}***"
echo "AWS_SECRET_ACCESS_KEY: ${AWS_SECRET_ACCESS_KEY:0:10}***"
echo "AWS_REGION: $AWS_REGION"

echo -e "\n=== OpenWeather API Key ==="
echo "OPENWEATHER_API_KEY: ${OPENWEATHER_API_KEY:0:10}***"

echo -e "\n=== MongoDB ==="
docker-compose ps mongodb

echo -e "\n=== Application ==="
curl -s http://localhost:8082/api/chat/test || echo "Application not running"
```

### 7-2. 애플리케이션 테스트

**터미널에서 테스트:**
```bash
# 헬스 체크
curl http://localhost:8082/api/chat/test

# 날씨 조회
curl -X POST http://localhost:8082/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "서울 날씨 알려줘"}'

# 계산
curl -X POST http://localhost:8082/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "123 곱하기 456은?"}'

# 일반 대화
curl -X POST http://localhost:8082/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "안녕! 웬즈데이"}'
```

**또는 테스트 스크립트 실행:**
```bash
./test.sh
```

---

## 🎯 빠른 설치 - 한번에 복사해서 실행

처음부터 끝까지 한번에 설치하려면 아래 스크립트를 복사해서 실행하세요:

```bash
#!/bin/bash

echo "🍎 맥미니 환경 설정 시작..."

# 1. Homebrew 설치 확인
echo "1️⃣ Homebrew 확인 중..."
if ! command -v brew &> /dev/null; then
    echo "Homebrew 설치 중..."
    /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
    eval "$(/opt/homebrew/bin/brew shellenv)"
else
    echo "✅ Homebrew 이미 설치됨"
fi

# 2. Java 17 설치
echo -e "\n2️⃣ Java 17 설치 중..."
brew install openjdk@17
sudo ln -sfn $(brew --prefix)/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk

# 3. Docker Desktop 설치
echo -e "\n3️⃣ Docker Desktop 설치 중..."
brew install --cask docker

# 4. ~/.zshrc 설정
echo -e "\n4️⃣ 환경 변수 설정 중..."
cat >> ~/.zshrc << 'EOF'

# ============================================
# Java 17
# ============================================
export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# ============================================
# AWS Credentials for Bedrock
# ============================================
export AWS_ACCESS_KEY_ID="여기에_실제_액세스_키_입력"
export AWS_SECRET_ACCESS_KEY="여기에_실제_시크릿_키_입력"
export AWS_REGION="ap-northeast-2"

# ============================================
# OpenWeatherMap API Key (선택사항)
# ============================================
export OPENWEATHER_API_KEY="여기에_실제_API_키_입력"
EOF

# 5. 설정 적용
source ~/.zshrc

echo -e "\n✅ 기본 설치 완료!"
echo -e "\n⚠️ 다음 단계를 수행하세요:"
echo "1. Docker Desktop 앱 실행: open -a Docker"
echo "2. ~/.zshrc 파일에서 AWS 자격증명 입력"
echo "3. nano ~/.zshrc 로 편집 후 실제 키 입력"
echo "4. source ~/.zshrc 로 적용"
echo -e "\n모든 설정 후 프로젝트 실행:"
echo "cd /Users/megasoby/Work/github/springAi-FC"
echo "docker-compose up -d"
echo "./gradlew bootRun"
```

---

## 🔧 문제 해결

### Java가 인식되지 않음
```bash
# Java 경로 확인
/usr/libexec/java_home -V

# JAVA_HOME 다시 설정
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

### Docker 명령어가 동작하지 않음
```bash
# Docker Desktop이 실행 중인지 확인
open -a Docker

# 실행될 때까지 1-2분 대기 후 다시 시도
docker ps
```

### Gradle 빌드 오류
```bash
# Gradle wrapper 권한 부여
chmod +x gradlew

# 캐시 삭제 후 재빌드
./gradlew clean build
```

### MongoDB 연결 실패
```bash
# 컨테이너 재시작
docker-compose down
docker-compose up -d

# 로그 확인
docker-compose logs mongodb
```

### AWS Bedrock 접근 오류
1. AWS 콘솔에서 Bedrock 서비스 활성화 확인
2. ap-northeast-2 리전 선택 확인
3. Claude 모델 액세스 권한 확인
4. IAM 권한 확인 (`bedrock:InvokeModel`)

---

## 8. 추가 개발 환경 설정

### Python 설치 (pyenv)

Python 버전 관리를 위한 pyenv를 사용합니다.

```bash
# pyenv 설치
brew install pyenv

# Python 최신 버전 설치
pyenv install 3.12.1

# 전역 Python 버전 설정
pyenv global 3.12.1

# 확인
python --version
pip --version
```

**~/.zshrc에 pyenv 설정 추가:**
```bash
# pyenv 설정
export PYENV_ROOT="$HOME/.pyenv"
export PATH="$PYENV_ROOT/bin:$PATH"
eval "$(pyenv init --path)"
eval "$(pyenv init -)"
```

### MongoDB Compass 설치

MongoDB를 GUI로 관리하는 공식 도구입니다.

```bash
# MongoDB Compass 설치
brew install --cask mongodb-compass
```

**연결 설정:**
```
mongodb://admin:admin123@localhost:27017/chatdb?authSource=admin
```

### iTerm2 설치

macOS용 강력한 터미널 에뮬레이터입니다.

```bash
# iTerm2 설치
brew install --cask iterm2
```

**권장 설정:**
1. Settings → Profiles → Text → Font
2. **MesloLGS NF** 폰트 선택 (12-13pt)
3. Settings → Profiles → Colors
4. **Solarized Dark** 또는 **Dracula** 색상 프리셋 선택

### Oh My Zsh + agnoster 테마 설치

터미널을 예쁘고 기능적으로 만들어줍니다.

#### 1️⃣ Oh My Zsh 설치

```bash
sh -c "$(curl -fsSL https://raw.githubusercontent.com/ohmyzsh/ohmyzsh/master/tools/install.sh)"
```

#### 2️⃣ Powerline 폰트 설치

```bash
brew tap homebrew/cask-fonts
brew install --cask font-meslo-lg-nerd-font
```

#### 3️⃣ agnoster 테마 설정

**~/.zshrc 파일 편집:**
```bash
nano ~/.zshrc
```

**기본 설정:**
```bash
# Oh My Zsh 설정
export ZSH="$HOME/.oh-my-zsh"
ZSH_THEME="agnoster"
plugins=(git)
source $ZSH/oh-my-zsh.sh

# agnoster 커스터마이징 - 사용자명만 표시, 호스트명 숨기기
prompt_context() {
  prompt_segment black default "%(!.%{%F{yellow}%}.)$USER"
}

# Java 17
export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# AWS Credentials
export AWS_ACCESS_KEY_ID="your-access-key"
export AWS_SECRET_ACCESS_KEY="your-secret-key"
export AWS_REGION="ap-northeast-2"

# pyenv (Python)
export PYENV_ROOT="$HOME/.pyenv"
export PATH="$PYENV_ROOT/bin:$PATH"
eval "$(pyenv init --path)"
eval "$(pyenv init -)"
```

#### 4️⃣ 터미널 재시작

```bash
source ~/.zshrc
```

**결과:**
```
megasoby ~/Work/github/springAi-FC  main 
➜
```

#### 5️⃣ iTerm2 색상 테마 (선택사항)

**Solarized Dark 다운로드:**
```bash
cd ~/Downloads
curl -O https://raw.githubusercontent.com/altercation/solarized/master/iterm2-colors-solarized/Solarized%20Dark.itermcolors
```

**적용:**
1. iTerm2 → Settings → Profiles → Colors
2. Color Presets... → Import...
3. 다운로드한 파일 선택
4. Color Presets... → Solarized Dark 선택

**또는 다양한 테마:**
```bash
# iTerm2 Color Schemes 다운로드
git clone https://github.com/mbadolato/iTerm2-Color-Schemes.git ~/Downloads/iTerm2-Color-Schemes
```

추천 테마:
- **Dracula**
- **Gruvbox Dark**
- **Nord**
- **One Dark**

### 유틸리티 앱

#### RunCat - CPU 모니터

메뉴바에서 귀여운 고양이가 CPU 사용량을 표시해줍니다.

**설치:**
- Mac App Store에서 "RunCat" 검색
- 또는: https://apps.apple.com/kr/app/runcat/id1429033973

**사용법:**
- Applications 폴더에서 실행
- 메뉴바 아이콘 클릭 → 캐릭터 변경 가능
- Settings → Login Item 체크 (자동 시작)

---

## 📚 추가 리소스

- [프로젝트 README](./README.md) - 프로젝트 상세 설명
- [Docker Compose 가이드](./DOCKER_COMPOSE_GUIDE.md) - MongoDB 설정
- [OpenWeather 설정 가이드](./OPENWEATHER_SETUP.md) - API 키 발급

---

## 👤 도움이 필요하신가요?

설치 중 문제가 발생하면:
1. 에러 메시지를 복사해서 공유해주세요
2. 어느 단계에서 멈췄는지 알려주세요
3. 위 "문제 해결" 섹션을 먼저 확인해보세요

**Made with ❤️ by 송그랜트 (Song Grant) & 웬즈데이 (Wednesday)**
