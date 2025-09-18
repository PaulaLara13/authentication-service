package co.com.pragma.r2dbc.entity;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("UserEntity")
public class UserEntity {
  @Id
  private Long id;
  private String name;
  private String lastname;

  @Column("date")
  private LocalDate date;

  private String address;
  private Integer phone;
  private String email;
  private double salary;
  private String password;
}
