package com.example.springai.config;

import com.example.springai.function.CalculatorFunction;
import com.example.springai.function.WeatherFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

/**
 * Function Calling 설정
 * Spring AI가 사용할 Function들을 Bean으로 등록합니다.
 */
@Configuration
public class FunctionConfig {

    @Bean
    @Description("도시의 현재 날씨 정보를 가져옵니다")
    public Function<WeatherFunction.Request, WeatherFunction.Response> weatherFunction() {
        return new WeatherFunction();
    }

    @Bean
    @Description("두 숫자의 사칙연산을 수행합니다")
    public Function<CalculatorFunction.Request, CalculatorFunction.Response> calculatorFunction() {
        return new CalculatorFunction();
    }
}

