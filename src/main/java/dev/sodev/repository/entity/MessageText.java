package dev.sodev.repository.entity;

import jakarta.persistence.Entity;

@Entity
public class MessageText extends Message {

    private String text;
}
