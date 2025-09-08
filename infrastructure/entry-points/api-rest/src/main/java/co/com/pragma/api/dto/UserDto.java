package co.com.pragma.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;

import java.math.BigInteger;
import java.time.LocalDate;

// internamente crea los getters y el constructor el RECORD
@Builder
public record UserDto(BigInteger id, String name,
                      String lastname,
                      LocalDate date,
                      String address,
                      String phone,
                      String mail,
                      double salary,
                        @JsonIgnore String message) {

}
