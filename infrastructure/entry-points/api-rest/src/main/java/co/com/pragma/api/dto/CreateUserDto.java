package co.com.pragma.api.dto;

import java.math.BigInteger;
import java.time.LocalDate;

public record CreateUserDto(
        BigInteger id,
        String name,
        String lastname,
        LocalDate date,
        String address,
        String phone,
        String mail,
        double salary,
        String password,
        BigInteger roleId){
}
