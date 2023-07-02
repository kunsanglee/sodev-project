package dev.sodev.global.aop.trace;

import dev.sodev.global.aop.trace.logtrace.LogTrace;
import dev.sodev.global.aop.trace.logtrace.ThreadLocalLogTrace;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogTraceConfig {

    @Bean
    public LogTrace logTrace() {

        return new ThreadLocalLogTrace();
    }
}
