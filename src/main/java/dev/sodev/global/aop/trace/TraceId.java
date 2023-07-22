package dev.sodev.global.aop.trace;

import dev.sodev.global.security.utils.SecurityUtil;

import java.util.UUID;

public class TraceId {

    private String id; // 트랜잭션 id
    private int level; // 깊이

    public TraceId() {
        this.id = createId();
        this.level = 0;
    }

    private TraceId(String id, int level) {
        this.id = id;
        this.level = level;
    }

    private String createId() {
        try {
            SecurityUtil.getMemberEmail();
        }catch (NullPointerException | ClassCastException e ){ // 로그인 안한 사용자는 UUID 처리
            return String.format("[Anonymous: %S]",UUID.randomUUID().toString().substring(0,8));
        }
        return SecurityUtil.getMemberEmail();
    }

    public TraceId createNextId() {
        return new TraceId(id, level + 1);
    }

    public TraceId createPreviousId() {
        return new TraceId(id, level - 1);
    }

    public boolean isFirstLevel() {
        return level == 0;
    }

    public String getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }
}
