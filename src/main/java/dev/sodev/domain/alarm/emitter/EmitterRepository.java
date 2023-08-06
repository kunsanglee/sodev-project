package dev.sodev.domain.alarm.emitter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
@RequiredArgsConstructor
public class EmitterRepository {

    private final Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    public SseEmitter save(Long memberId, SseEmitter emitter) {
        final String key = getKey(memberId);
        log.info("Set Emitter to Redis {}({})", key, emitter);
        emitterMap.put(key, emitter);
        return emitter;
    }

    public void delete(Long memberId) {
        emitterMap.remove(getKey(memberId));
    }

    public Optional<SseEmitter> get(Long memberId) {
        SseEmitter result = emitterMap.get(getKey(memberId));
        log.info("Get Emitter from Redis {}", result);
        return Optional.ofNullable(result);
    }

    private String getKey(Long memberId) {
        return "emitter:UID:" + memberId;
    }

}
