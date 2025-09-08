package co.com.pragma.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

@Entity
@Getter
@Setter
@Table(name = "roles")
public class RoleEntity {
    @Id
    private BigInteger id;

    @Column(nullable = false,unique = true, length = 50)
    private String name;
}
