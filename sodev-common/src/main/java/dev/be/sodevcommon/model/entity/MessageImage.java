package dev.be.sodevcommon.model.entity;

import jakarta.persistence.Entity;

@Entity
public class MessageImage extends Message {

    private String url;
    private String name;
    private String uuid;
}
