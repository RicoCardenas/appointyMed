package com.appointymed.auth.application.mapper;

import com.appointymed.auth.application.dto.UserDto;
import com.appointymed.auth.domain.model.Email;
import com.appointymed.auth.domain.model.PhoneNumber;
import com.appointymed.auth.domain.model.User;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-21T01:04:50-0500",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.19 (Ubuntu)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDto toDto(User user) {
        if ( user == null ) {
            return null;
        }

        String email = null;
        String phone = null;
        UUID id = null;
        String fullName = null;
        String status = null;
        boolean emailVerified = false;
        Instant createdAt = null;
        Instant updatedAt = null;

        email = userEmailValue( user );
        phone = userPhoneValue( user );
        id = user.getId();
        fullName = user.getFullName();
        if ( user.getStatus() != null ) {
            status = user.getStatus().name();
        }
        emailVerified = user.isEmailVerified();
        createdAt = user.getCreatedAt();
        updatedAt = user.getUpdatedAt();

        List<String> roles = user.getRoles().stream().map(Enum::name).toList();

        UserDto userDto = new UserDto( id, email, fullName, phone, status, emailVerified, roles, createdAt, updatedAt );

        return userDto;
    }

    @Override
    public String map(PhoneNumber phoneNumber) {
        if ( phoneNumber == null ) {
            return null;
        }

        String string = new String();

        return string;
    }

    private String userEmailValue(User user) {
        if ( user == null ) {
            return null;
        }
        Email email = user.getEmail();
        if ( email == null ) {
            return null;
        }
        String value = email.getValue();
        if ( value == null ) {
            return null;
        }
        return value;
    }

    private String userPhoneValue(User user) {
        if ( user == null ) {
            return null;
        }
        PhoneNumber phone = user.getPhone();
        if ( phone == null ) {
            return null;
        }
        String value = phone.getValue();
        if ( value == null ) {
            return null;
        }
        return value;
    }
}
