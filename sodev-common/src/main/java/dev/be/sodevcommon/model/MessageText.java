package dev.be.sodevcommon.model;

import jakarta.persistence.Entity;

@Entity
public class MessageText extends Message {

    private String text;
}
