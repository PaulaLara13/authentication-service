package co.com.pragma.api.dto;

import lombok.Builder;

@Builder
public record TokenResponse(String accessToken, String tokenType, long expiresIn) {

}