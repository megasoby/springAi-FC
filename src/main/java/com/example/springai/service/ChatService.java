package com.example.springai.service;

import com.example.springai.function.CalculatorFunction;
import com.example.springai.function.WeatherFunction;
import com.example.springai.model.ConversationSession;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI Chat 서비스
 * Function-calling을 사용하여 사용자 질문에 답변합니다.
 * 대화 히스토리를 관리하여 이전 대화를 기억합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatModel chatModel;  // AWS Bedrock Claude
    private final ApplicationContext applicationContext;
    private final SessionManager sessionManager;
    private final WeatherFunction weatherFunction = new WeatherFunction();
    private final CalculatorFunction calculatorFunction = new CalculatorFunction();

    /**
     * 사용자 메시지를 처리하고 필요시 Function을 호출하여 응답합니다.
     * 세션 ID를 통해 대화 히스토리를 관리합니다.
     */
    public com.example.springai.dto.ChatResponse chat(String userMessage, String sessionId) {
        log.info("💬 사용자 메시지: {} (세션: {})", userMessage, sessionId);
        
        try {
            // 세션 가져오기 또는 생성
            ConversationSession session = sessionManager.getOrCreateSession(sessionId);
            sessionId = session.getSessionId();
            
            // 등록된 Function 확인
            logRegisteredFunctions();
            
            String systemPrompt = """
                당신은 친절하고 전문적인 AI 어시스턴트입니다.
                사용자는 '송그랜트'이고, 당신은 '웬즈데이'입니다.
                
                역할:
                - 사용자의 질문에 정확하고 친절하게 답변해주세요.
                - 이전 대화 내용을 기억하고 맥락을 유지하세요.
                - 필요한 경우 제공된 함수(Function)를 호출하여 정보를 얻으세요.
                - 이모지를 적절히 사용하여 친근하게 대화하세요.
                
                사용 가능한 함수:
                - weatherFunction: 도시의 날씨 정보 조회 (파라미터: city - 도시 이름)
                - calculatorFunction: 두 숫자의 사칙연산 (파라미터: a, b, operation)
                
                예시:
                - 사용자가 "서울 날씨 어때?"라고 물으면 weatherFunction을 호출하세요
                - 사용자가 "25 곱하기 48은?"이라고 물으면 calculatorFunction을 호출하세요
                - 이전 대화를 참고하여 "거기 날씨는?"과 같은 질문에도 답할 수 있어야 합니다.
                """;
            
            UserMessage userMsg = new UserMessage(userMessage);
            
            // MongoDB에 사용자 메시지 저장
            sessionManager.addMessage(sessionId, userMsg);
            
            // 저장 후 세션을 다시 가져와서 업데이트된 히스토리 확인
            session = sessionManager.getOrCreateSession(sessionId);
            List<Message> historyMessages = session.getMessageHistory();
            
            // 프롬프트 생성: 히스토리가 1개(방금 추가한 첫 메시지)면 시스템 프롬프트 포함
            List<Message> messages = new ArrayList<>();
            if (historyMessages.size() == 1) {
                // 첫 대화: 시스템 프롬프트 + 사용자 메시지를 합침
                messages.add(new UserMessage(systemPrompt + "\n\n사용자: " + userMessage));
            } else {
                // 이후 대화: 히스토리만 사용
                messages.addAll(historyMessages);
            }
            
            Prompt prompt = new Prompt(messages);
            
            ChatResponse chatResponse = chatModel.call(prompt);
            String response = chatResponse.getResult().getOutput().getContent();
            
            log.info("🤖 AI 초기 응답: {}", response);
            
            boolean functionCalled = false;
            
            // Function 호출이 포함되어 있는지 확인
            if (response.contains("<function_calls>")) {
                functionCalled = true;
                String functionResult = executeFunctionFromResponse(response);
                
                // Function 실행 결과를 포함하여 다시 AI에게 질의
                if (functionResult != null) {
                    String followUpPrompt = String.format("""
                        사용자 질문: %s
                        
                        함수 실행 결과:
                        %s
                        
                        위 결과를 바탕으로 사용자에게 친절하고 자연스러운 답변을 해주세요.
                        이전 대화 맥락도 고려하세요.
                        """, userMessage, functionResult);
                    
                    UserMessage followUpMsg = new UserMessage(followUpPrompt);
                    
                    // 함수 결과를 포함한 새 프롬프트
                    List<Message> followUpMessages = new ArrayList<>();
                    followUpMessages.addAll(session.getMessageHistory());
                    followUpMessages.add(followUpMsg);
                    
                    Prompt followUpPromptObj = new Prompt(followUpMessages);
                    
                    response = chatModel.call(followUpPromptObj).getResult().getOutput().getContent();
                    log.info("🤖 최종 응답 (Function 결과 포함): {}", response);
                }
            }
            
            // AI 응답을 MongoDB에 저장
            AssistantMessage assistantMessage = new AssistantMessage(response);
            sessionManager.addMessage(sessionId, assistantMessage);
            
            // 세션 정보 다시 가져오기 (대화 턴 수 업데이트를 위해)
            session = sessionManager.getOrCreateSession(sessionId);
            
            log.info("📊 세션 {} - 현재 대화 턴: {}", sessionId, session.getConversationTurnCount());
            
            return new com.example.springai.dto.ChatResponse(
                response, 
                functionCalled,
                sessionId,
                session.getConversationTurnCount()
            );
            
        } catch (Exception e) {
            log.error("❌ AI 응답 생성 실패: {}", e.getMessage(), e);
            return new com.example.springai.dto.ChatResponse(
                "AI 응답 생성 중 오류가 발생했습니다: " + e.getMessage(),
                false,
                sessionId,
                0
            );
        }
    }
    
    /**
     * 하위 호환성을 위한 메서드 (세션 없이 호출)
     */
    public String chat(String userMessage) {
        com.example.springai.dto.ChatResponse response = chat(userMessage, null);
        return response.response();
    }
    
    /**
     * AI 응답에서 Function 호출을 파싱하고 실행합니다.
     */
    private String executeFunctionFromResponse(String response) {
        try {
            // weatherFunction 파싱
            Pattern weatherPattern = Pattern.compile("<tool_name>weatherFunction</tool_name>.*?<city>(.*?)</city>", Pattern.DOTALL);
            Matcher weatherMatcher = weatherPattern.matcher(response);
            
            if (weatherMatcher.find()) {
                String city = weatherMatcher.group(1).trim();
                log.info("🌤️ WeatherFunction 호출: city={}", city);
                
                WeatherFunction.Request request = new WeatherFunction.Request(city);
                WeatherFunction.Response result = weatherFunction.apply(request);
                
                return String.format("도시: %s\n온도: %s\n날씨: %s\n습도: %s", 
                        result.city(), result.temperature(), result.condition(), result.humidity());
            }
            
            // calculatorFunction 파싱
            Pattern calcPattern = Pattern.compile("<tool_name>calculatorFunction</tool_name>.*?<a>(.*?)</a>.*?<b>(.*?)</b>.*?<operation>(.*?)</operation>", Pattern.DOTALL);
            Matcher calcMatcher = calcPattern.matcher(response);
            
            if (calcMatcher.find()) {
                double a = Double.parseDouble(calcMatcher.group(1).trim());
                double b = Double.parseDouble(calcMatcher.group(2).trim());
                String operation = calcMatcher.group(3).trim();
                
                log.info("🧮 CalculatorFunction 호출: a={}, b={}, operation={}", a, b, operation);
                
                CalculatorFunction.Request request = new CalculatorFunction.Request(a, b, operation);
                CalculatorFunction.Response result = calculatorFunction.apply(request);
                
                return String.format("계산 결과: %s", result.expression());
            }
            
        } catch (Exception e) {
            log.error("❌ Function 실행 실패: {}", e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * 등록된 Function들을 로그로 출력합니다.
     */
    private void logRegisteredFunctions() {
        try {
            var functions = applicationContext.getBeansOfType(Function.class);
            log.info("📋 등록된 Functions: {}", functions.keySet());
        } catch (Exception e) {
            log.debug("Function 목록 조회 실패: {}", e.getMessage());
        }
    }
}
