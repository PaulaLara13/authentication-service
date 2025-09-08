package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.CreateUserDto;
import co.com.pragma.api.dto.UserDto;
import co.com.pragma.model.user.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserDtoMapper {

    UserDto toResponse(User user);

    List<UserDto> toResponseList(List<User> users);

    User toModel(CreateUserDto createUserDto);



}
