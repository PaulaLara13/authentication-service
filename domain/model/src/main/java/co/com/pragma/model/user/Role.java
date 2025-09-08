package co.com.pragma.model.user;

import lombok.*;
import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)

public class Role {
    private BigInteger id;
    private String name;
}
