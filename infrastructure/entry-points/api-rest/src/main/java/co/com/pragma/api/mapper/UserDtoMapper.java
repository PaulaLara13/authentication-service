package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.CreateUserDto;
import co.com.pragma.api.dto.UserDto;
import co.com.pragma.model.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", 
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface UserDtoMapper {

    @Mapping(target = "mail", source = "email")
    @Mapping(target = "date", source = "dateOfBirth")
    @Mapping(target = "message", ignore = true) // Ignored field
    UserDto toResponse(User user);

    List<UserDto> toResponseList(List<User> users);

    @Mapping(target = "email", source = "mail")
    @Mapping(target = "dateOfBirth", source = "date")
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "accountNonExpired", constant = "true")
    @Mapping(target = "credentialsNonExpired", constant = "true")
    @Mapping(target = "accountNonLocked", constant = "true")
    @Mapping(target = "roles", ignore = true) // Will be set by the service
    @Mapping(target = "password", ignore = true) // Will be set by the service
    User toModel(CreateUserDto createUserDto);
}
