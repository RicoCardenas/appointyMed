package com.appointymed.auth.application.mapper;

import com.appointymed.auth.application.dto.UserDto;
import com.appointymed.auth.domain.model.PhoneNumber;
import com.appointymed.auth.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "email", source = "email.value")
    @Mapping(target = "phone", source = "phone.value")
    @Mapping(target = "roles", expression = "java(user.getRoles().stream().map(Enum::name).toList())")
    UserDto toDto(User user);

    String map(PhoneNumber phoneNumber);
}