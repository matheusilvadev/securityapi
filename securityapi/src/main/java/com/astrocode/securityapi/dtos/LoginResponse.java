package com.astrocode.securityapi.dtos;

public record LoginResponse(String accessToken, Long expiresIn) {
}
