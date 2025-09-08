package co.com.pragma.model.user;

import lombok.*;

import java.math.BigInteger;
import java.time.LocalDate;
//import lombok.NoArgsConstructor;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class User {
    //se colocan los atributos
    private BigInteger id;
    private String name;
    private String lastname;
    private LocalDate date;
    private String address;
    private String phone;
    private String mail;
    private double salary;
    private String message;

}
