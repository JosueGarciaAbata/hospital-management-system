package com.hospital.mappers;

import com.hospital.dtos.CreateUserRequest;
import com.hospital.dtos.UserResponse;
import com.hospital.entities.Role;
import com.hospital.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "roles", source = "roles")
    UserResponse toUserResponse(User user);
    User toUser(CreateUserRequest request);

    default List<String> mapRoles(Set<Role> roles) {
        return roles == null ? java.util.List.of()
                : roles.stream().map(Role::getName).sorted().toList();
    }
}
