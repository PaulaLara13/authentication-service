package co.com.pragma.model.user;

import lombok.*;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class User {
    private BigInteger id;
    private String name;
    private String lastname;
    private LocalDate dateOfBirth;
    private String address;
    private String phone;
    private String email;
    private String password;
    private Double salary;
    private boolean enabled;
    private boolean accountNonExpired;
    private boolean credentialsNonExpired;
    private boolean accountNonLocked;
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    public User(String email, String password, Set<Role> roles) {
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.enabled = true;
        this.accountNonExpired = true;
        this.credentialsNonExpired = true;
        this.accountNonLocked = true;
    }
    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    public String getMail() {
        return this.email;
    }

    public Double getSalary() {
        return this.salary;
    }
}
