package dev.sodev.global.audit;

import dev.sodev.global.security.utils.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

@Slf4j
public class AuditorAwareImpl implements AuditorAware<Object> {


    @Override
    public Optional<Object> getCurrentAuditor() {
        String email = SecurityUtil.getMemberEmail();
        if (email == null) {
            return Optional.empty();
        };
        System.out.println(email);
        return Optional.of(email);
    }
}