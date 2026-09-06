package com.appointymed.auth.application.mapper;

import com.appointymed.auth.application.dto.UserDto;
import com.appointymed.auth.domain.model.DomainId;
import com.appointymed.auth.domain.model.DomainInstant;
import com.appointymed.auth.domain.model.PhoneNumber;
import com.appointymed.auth.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "email", source = "email.value")
    @Mapping(target = "phone", source = "phone.value")
    @Mapping(target = "roles", expression = "java(roleNames(user.getRoles()))")
    UserDto toDto(User user);

    String map(PhoneNumber phoneNumber);

    default UUID map(DomainId id) { return id == null ? null : UUID.fromString(id.value()); }

    default Instant map(DomainInstant instant) { return instant == null ? null : Instant.ofEpochMilli(instant.toEpochMillis()); }

    default java.util.List<String> roleNames(com.appointymed.auth.domain.model.DomainSet<com.appointymed.auth.domain.model.Role> roles) {
        java.util.List<String> result = new java.util.ArrayList<>();
        for (int index = 0; index < roles.size(); index++) result.add(roles.get(index).name());
        return result;
    }
}
