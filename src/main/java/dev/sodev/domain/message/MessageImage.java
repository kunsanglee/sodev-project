package dev.sodev.domain.message;

import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageImage extends Message {

    private String url;
    private String name;
    private String uuid;
}
