package co.com.pragma.api.dto;

import lombok.Builder;

@Builder
public record LoginRequest(String email, String password) {}
