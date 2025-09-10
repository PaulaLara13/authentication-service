package co.com.pragma.model.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    private BigInteger id;
    private RoleName name;
    
    public static Role from(RoleName roleName) {
        return Role.builder()
                .name(roleName)
                .build();
    }
}
