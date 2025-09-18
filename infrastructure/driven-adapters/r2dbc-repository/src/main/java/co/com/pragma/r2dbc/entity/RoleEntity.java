package co.com.pragma.r2dbc.entity;

import java.math.BigInteger;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("RoleEntity")
public class RoleEntity {
    @Id
    private BigInteger id;
    private String name; // ADMIN, ADVISOR, CLIENT

}
