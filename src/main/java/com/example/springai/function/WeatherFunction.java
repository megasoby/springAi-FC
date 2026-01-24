package com.example.springai.function;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/**
 * 날씨 조회 Function
 * AI가 날씨 정보가 필요할 때 이 함수를 호출합니다.
 * OpenWeatherMap API를 사용하여 실제 날씨 정보를 가져옵니다.
 */
public class WeatherFunction implements Function<WeatherFunction.Request, WeatherFunction.Response> {

    // OpenWeatherMap API 키 (무료 플랜)
    // https://openweathermap.org/api 에서 발급 가능
    private static final String API_KEY = System.getenv("OPENWEATHER_API_KEY");
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather";
    
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @JsonClassDescription("도시의 현재 날씨 정보를 가져옵니다")
    public record Request(
            @JsonProperty(required = true, value = "city")
            @JsonPropertyDescription("날씨를 조회할 도시 이름 (예: 서울, Seoul, 부산)")
            String city
    ) {}

    public record Response(
            String city,
            String temperature,
            String condition,
            String humidity
    ) {}

    @Override
    public Response apply(Request request) {
        System.out.println("🌤️ WeatherFunction 호출됨: " + request.city());
        
        // API 키가 없으면 Mock 데이터 반환
        if (API_KEY == null || API_KEY.isEmpty()) {
            System.out.println("⚠️ OPENWEATHER_API_KEY가 설정되지 않아 Mock 데이터를 반환합니다.");
            return getMockWeather(request.city());
        }
        
        try {
            // 실제 OpenWeatherMap API 호출
            String cityName = translateCityName(request.city());
            String url = String.format("%s?q=%s&appid=%s&units=metric&lang=kr", 
                    API_URL, 
                    URLEncoder.encode(cityName, StandardCharsets.UTF_8),
                    API_KEY);
            
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return parseWeatherResponse(response.body(), request.city());
            } else {
                System.out.println("❌ API 호출 실패: " + response.statusCode());
                return getMockWeather(request.city());
            }
            
        } catch (Exception e) {
            System.out.println("❌ 날씨 조회 중 오류: " + e.getMessage());
            return getMockWeather(request.city());
        }
    }
    
    /**
     * OpenWeatherMap API 응답을 파싱합니다.
     */
    private Response parseWeatherResponse(String jsonResponse, String originalCity) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            
            // 온도 (섭씨)
            double temp = root.path("main").path("temp").asDouble();
            String temperature = String.format("%.1f°C", temp);
            
            // 날씨 상태 (한국어)
            String condition = root.path("weather").get(0).path("description").asText();
            
            // 습도
            int humidity = root.path("main").path("humidity").asInt();
            String humidityStr = humidity + "%";
            
            // 도시 이름
            String cityName = root.path("name").asText(originalCity);
            
            System.out.println("✅ 실제 날씨 데이터 조회 성공: " + cityName);
            
            return new Response(cityName, temperature, condition, humidityStr);
            
        } catch (Exception e) {
            System.out.println("❌ 응답 파싱 실패: " + e.getMessage());
            return getMockWeather(originalCity);
        }
    }
    
    /**
     * 한글 도시명을 영문으로 변환합니다.
     */
    private String translateCityName(String city) {
        return switch (city.toLowerCase()) {
            case "서울" -> "Seoul";
            case "부산" -> "Busan";
            case "인천" -> "Incheon";
            case "대구" -> "Daegu";
            case "대전" -> "Daejeon";
            case "광주" -> "Gwangju";
            case "울산" -> "Ulsan";
            case "제주" -> "Jeju";
            default -> city;  // 이미 영문이거나 다른 도시
        };
    }
    
    /**
     * API 키가 없거나 오류 발생 시 Mock 데이터를 반환합니다.
     */
    private Response getMockWeather(String city) {
        return switch (city.toLowerCase()) {
            case "서울", "seoul" -> new Response(
                    "서울",
                    "15°C",
                    "맑음 (Mock 데이터)",
                    "65%"
            );
            case "부산", "busan" -> new Response(
                    "부산",
                    "18°C",
                    "흐림 (Mock 데이터)",
                    "70%"
            );
            case "제주", "jeju" -> new Response(
                    "제주",
                    "20°C",
                    "비 (Mock 데이터)",
                    "85%"
            );
            default -> new Response(
                    city,
                    "22°C",
                    "알 수 없음 (Mock 데이터)",
                    "60%"
            );
        };
    }
}

