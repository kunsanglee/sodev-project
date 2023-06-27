package dev.be.sodevcommon.domain.entity;

import jakarta.persistence.Entity;

@Entity
public class MessageText extends Message {

    private String text;
}
