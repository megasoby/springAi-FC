# Spring AI Function-Calling 예제 프로젝트 🚀

Spring AI의 Function-calling 기능을 활용한 간단한 챗봇 애플리케이션입니다.  
**AWS Bedrock Claude Sonnet 4.5** (2025년 9월 버전)을 사용합니다.

## 📋 프로젝트 개요

AI가 사용자의 질문을 이해하고, 필요할 때 자동으로 함수를 호출하여 답변하는 시스템입니다.
**이전 대화를 기억하고 맥락을 유지하는 대화형 AI 에이전트**입니다.

### 주요 기능

1. **대화 기억 (Conversation Memory)** 🧠
   - 이전 대화 내용을 기억하고 맥락을 유지합니다
   - 세션 기반 대화 관리
   - "거기 날씨는?" 같은 맥락 기반 질문에 답변 가능

2. **날씨 조회** (`WeatherFunction`)
   - 도시 이름을 입력받아 현재 날씨 정보를 반환
   - 예: "서울 날씨 알려줘"

3. **계산기** (`CalculatorFunction`)
   - 두 숫자의 사칙연산 수행
   - 예: "123 곱하기 456은?"
   - 이전 계산 결과를 기억: "그 결과에 10을 더하면?"

## 🛠️ 기술 스택

- Java 17
- Spring Boot 3.2.0
- Spring AI 1.0.0-M4
- AWS Bedrock Converse API
- Claude Sonnet 4.5 (2025-09-29)
- Gradle

## 🚀 시작하기

### 1. AWS 자격증명 설정

`~/.zshrc` 파일에 이미 설정되어 있어야 합니다:

```bash
export AWS_ACCESS_KEY_ID=your-access-key
export AWS_SECRET_ACCESS_KEY=your-secret-key
export AWS_REGION=ap-northeast-2
```

터미널 재시작 또는:
```bash
source ~/.zshrc
```

### 2. 애플리케이션 실행

#### 방법 1: 스크립트로 실행 (권장)

**백그라운드 실행:**
```bash
./run.sh start
```

**유용한 명령어:**
```bash
./run.sh status   # 실행 상태 확인
./run.sh logs     # 실시간 로그 확인
./run.sh stop     # 애플리케이션 종료
./run.sh restart  # 재시작
```

#### 방법 2: Gradle로 직접 실행
```bash
./gradlew bootRun
```

애플리케이션이 **8082 포트**에서 시작됩니다.

### 3. Web UI로 테스트

브라우저에서 아래 주소로 접속:

```
http://localhost:8082
```

**또는 터미널에서:**

#### 헬스체크
```bash
curl http://localhost:8082/api/chat/test
```

#### 채팅 요청
```bash
# 날씨 조회
curl -X POST http://localhost:8082/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "서울 날씨 알려줘"}'

# 계산
curl -X POST http://localhost:8082/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "25 곱하기 48은?"}'

# 일반 대화
curl -X POST http://localhost:8082/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "안녕! 웬즈데이"}'
```

#### 테스트 스크립트 실행
```bash
./test.sh
```

## 📁 프로젝트 구조

```
src/main/java/com/example/springai/
├── SpringAiFunctionCallingApplication.java  # 메인 애플리케이션
├── config/
│   └── FunctionConfig.java                  # Function Bean 설정
├── controller/
│   └── ChatController.java                  # REST API 컨트롤러
├── service/
│   └── ChatService.java                     # AI Chat 서비스
├── function/
│   ├── WeatherFunction.java                 # 날씨 조회 Function
│   └── CalculatorFunction.java              # 계산기 Function
└── dto/
    ├── ChatRequest.java                     # 요청 DTO
    └── ChatResponse.java                    # 응답 DTO
```

## 💡 Function-Calling 동작 방식

1. **사용자가 질문** → "서울 날씨 알려줘"
2. **AI가 분석** → "날씨 정보가 필요하네"
3. **Function 호출** → `weatherFunction("서울")`
4. **결과 반환** → "서울: 맑음, 15°C"
5. **AI가 답변 생성** → "서울의 현재 날씨는 맑음이며, 기온은 15도입니다."

## 🎯 테스트 시나리오

### 날씨 조회
- "서울 날씨 어때?"
- "부산 날씨 알려줘"
- "제주도 날씨는?"

### 계산
- "123 더하기 456은?"
- "1000 나누기 25는?"
- "50 곱하기 30을 계산해줘"

### 일반 대화
- "안녕!"
- "오늘 기분이 좋아"
- "Spring AI에 대해 설명해줘"

## 📝 커스터마이징

### 새로운 Function 추가하기

1. **Function 클래스 생성**
```java
public class MyFunction implements Function<Request, Response> {
    @JsonClassDescription("함수 설명")
    public record Request(
        @JsonProperty(required = true, value = "param")
        @JsonPropertyDescription("파라미터 설명")
        String param
    ) {}
    
    public record Response(String result) {}
    
    @Override
    public Response apply(Request request) {
        // 로직 구현
        return new Response("결과");
    }
}
```

2. **FunctionConfig에 Bean 등록**
```java
@Bean
@Description("함수 설명")
public Function<Request, Response> myFunction() {
    return new MyFunction();
}
```

## 🔍 로그 확인

애플리케이션 실행 시 콘솔에서 Function 호출 로그를 확인할 수 있습니다:

```
💬 사용자 메시지: 서울 날씨 알려줘
🌤️ WeatherFunction 호출됨: 서울
🤖 AI 응답: 서울의 현재 날씨는 맑음이며, 기온은 15°C입니다.
```

## 🤔 문제 해결

### AWS 자격증명 오류
- 환경 변수가 제대로 설정되었는지 확인: `echo $AWS_ACCESS_KEY_ID`
- AWS 계정에서 Bedrock 서비스가 활성화되어 있는지 확인
- ap-northeast-2 리전에서 Claude 모델을 사용할 수 있는지 확인

### Function이 호출되지 않음
- `@Description` 어노테이션이 명확한지 확인
- `JsonPropertyDescription`이 구체적인지 확인
- Claude Sonnet 4.5는 function-calling을 지원합니다

### Bedrock 모델 접근 오류
- AWS Bedrock 콘솔에서 Claude 모델에 대한 액세스 요청
- IAM 권한 확인 (bedrock:InvokeModel 권한 필요)

### Gradle 빌드 오류
- Java 17이 설치되어 있는지 확인: `java -version`
- Gradle wrapper 실행 권한 확인: `chmod +x gradlew`

## 📚 참고 자료

- [Spring AI 공식 문서](https://docs.spring.io/spring-ai/reference/)
- [AWS Bedrock 문서](https://docs.aws.amazon.com/bedrock/)
- [Claude 3.5 Sonnet](https://www.anthropic.com/claude)
- [참고 프로젝트](../spring-boot-elasticsearch) - 실제 프로덕션 환경에서 사용 중

## 👤 개발자

송그랜트 (Song Grant) & 웬즈데이 (Wednesday)

---

**Made with ❤️ using Spring AI + AWS Bedrock**


## 📁 프로젝트 구조

```
src/main/java/com/example/springai/
├── SpringAiFunctionCallingApplication.java  # 메인 애플리케이션
├── config/
│   └── FunctionConfig.java                  # Function Bean 설정
├── controller/
│   └── ChatController.java                  # REST API 컨트롤러
├── service/
│   └── ChatService.java                     # AI Chat 서비스
├── function/
│   ├── WeatherFunction.java                 # 날씨 조회 Function
│   └── CalculatorFunction.java              # 계산기 Function
└── dto/
    ├── ChatRequest.java                     # 요청 DTO
    └── ChatResponse.java                    # 응답 DTO
```

## 💡 Function-Calling 동작 방식

1. **사용자가 질문** → "서울 날씨 알려줘"
2. **AI가 분석** → "날씨 정보가 필요하네"
3. **Function 호출** → `weatherFunction("서울")`
4. **결과 반환** → "서울: 맑음, 15°C"
5. **AI가 답변 생성** → "서울의 현재 날씨는 맑음이며, 기온은 15도입니다."

## 🎯 테스트 시나리오

### 날씨 조회
- "서울 날씨 어때?"
- "부산 날씨 알려줘"
- "제주도 날씨는?"

### 계산
- "123 더하기 456은?"
- "1000 나누기 25는?"
- "50 곱하기 30을 계산해줘"

### 복합 질문
- "서울 날씨 알려주고, 15 곱하기 30도 계산해줘"

## 📝 커스터마이징

### 새로운 Function 추가하기

1. **Function 클래스 생성**
```java
public class MyFunction implements Function<Request, Response> {
    @JsonClassDescription("함수 설명")
    public record Request(
        @JsonProperty(required = true, value = "param")
        @JsonPropertyDescription("파라미터 설명")
        String param
    ) {}
    
    public record Response(String result) {}
    
    @Override
    public Response apply(Request request) {
        // 로직 구현
        return new Response("결과");
    }
}
```

2. **FunctionConfig에 Bean 등록**
```java
@Bean
@Description("함수 설명")
public Function<Request, Response> myFunction() {
    return new MyFunction();
}
```

3. **ChatService에서 사용**
```java
.withFunctionCallbacks(List.of("weatherFunction", "calculatorFunction", "myFunction"))
```

## 🔍 로그 확인

애플리케이션 실행 시 콘솔에서 Function 호출 로그를 확인할 수 있습니다:

```
💬 사용자 메시지: 서울 날씨 알려줘
🌤️ WeatherFunction 호출됨: 서울
🤖 AI 응답: 서울의 현재 날씨는 맑음이며, 기온은 15°C입니다.
```

## 🤔 문제 해결

### AWS 자격증명 오류
- 환경 변수가 제대로 설정되었는지 확인: `echo $AWS_ACCESS_KEY_ID`
- AWS 계정에서 Bedrock 서비스가 활성화되어 있는지 확인
- ap-northeast-2 리전에서 Claude 모델을 사용할 수 있는지 확인

### Function이 호출되지 않음
- `@Description` 어노테이션이 명확한지 확인
- `JsonPropertyDescription`이 구체적인지 확인
- Claude 3.5 Sonnet은 function-calling을 지원합니다

### Bedrock 모델 접근 오류
- AWS Bedrock 콘솔에서 Claude 모델에 대한 액세스 요청
- IAM 권한 확인 (bedrock:InvokeModel 권한 필요)

## 📚 참고 자료

- [Spring AI 공식 문서](https://docs.spring.io/spring-ai/reference/)
- [AWS Bedrock 문서](https://docs.aws.amazon.com/bedrock/)
- [Claude 3.5 Sonnet](https://www.anthropic.com/claude)

## 👤 개발자

송그랜트 (Song Grant)

---

**Made with ❤️ using Spring AI + AWS Bedrock**

