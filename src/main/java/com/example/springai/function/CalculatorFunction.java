package com.example.springai.function;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.function.Function;

/**
 * 계산기 Function
 * AI가 수학 계산이 필요할 때 이 함수를 호출합니다.
 */
public class CalculatorFunction implements Function<CalculatorFunction.Request, CalculatorFunction.Response> {

    @JsonClassDescription("두 숫자의 사칙연산을 수행합니다")
    public record Request(
            @JsonProperty(required = true, value = "a")
            @JsonPropertyDescription("첫 번째 숫자")
            double a,
            
            @JsonProperty(required = true, value = "b")
            @JsonPropertyDescription("두 번째 숫자")
            double b,
            
            @JsonProperty(required = true, value = "operation")
            @JsonPropertyDescription("연산 종류: add(더하기), subtract(빼기), multiply(곱하기), divide(나누기)")
            String operation
    ) {}

    public record Response(
            double result,
            String operation,
            String expression
    ) {}

    @Override
    public Response apply(Request request) {
        System.out.println("🧮 CalculatorFunction 호출됨: " + request.a() + " " + request.operation() + " " + request.b());
        
        double result = switch (request.operation().toLowerCase()) {
            case "add" -> request.a() + request.b();
            case "subtract" -> request.a() - request.b();
            case "multiply" -> request.a() * request.b();
            case "divide" -> {
                if (request.b() == 0) {
                    throw new IllegalArgumentException("0으로 나눌 수 없습니다");
                }
                yield request.a() / request.b();
            }
            default -> throw new IllegalArgumentException("지원하지 않는 연산: " + request.operation());
        };

        String expression = String.format("%.2f %s %.2f = %.2f",
                request.a(),
                getOperationSymbol(request.operation()),
                request.b(),
                result);

        return new Response(result, request.operation(), expression);
    }

    private String getOperationSymbol(String operation) {
        return switch (operation.toLowerCase()) {
            case "add" -> "+";
            case "subtract" -> "-";
            case "multiply" -> "×";
            case "divide" -> "÷";
            default -> operation;
        };
    }
}

