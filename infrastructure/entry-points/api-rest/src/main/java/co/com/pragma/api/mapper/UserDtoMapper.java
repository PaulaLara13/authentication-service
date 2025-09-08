package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.CreateUserDto;
import co.com.pragma.api.dto.UserDto;
import co.com.pragma.model.user.Usuario;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserDtoMapper {

    UserDto toResponse(Usuario usuario);

    List<UserDto> toResponseList(List<Usuario> users);

    Usuario toModel(CreateUserDto createUserDto);



}
