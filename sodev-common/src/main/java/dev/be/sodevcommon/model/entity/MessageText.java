package dev.be.sodevcommon.model.entity;

import jakarta.persistence.Entity;

@Entity
public class MessageText extends Message {

    private String text;
}
