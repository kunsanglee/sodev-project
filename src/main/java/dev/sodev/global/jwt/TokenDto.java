package dev.sodev.global.jwt;


public record TokenDto(String accessToken, String refreshToken) {
    public TokenDto(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
