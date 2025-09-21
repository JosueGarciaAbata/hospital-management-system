package com.hospital.mappers;

import com.hospital.dtos.CreateUserRequest;
import com.hospital.dtos.UserResponse;
import com.hospital.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toUserResponse(User user);
    User toUser(CreateUserRequest request);

}
