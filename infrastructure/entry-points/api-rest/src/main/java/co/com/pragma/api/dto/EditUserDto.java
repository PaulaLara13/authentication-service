package co.com.pragma.api.dto;

public record EditUserDto(String name,
                          String lastname,
                          Integer age,
                          Integer IdType,
                          Long idNumber) {
}
