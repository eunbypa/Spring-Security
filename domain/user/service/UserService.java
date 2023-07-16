package com.zucchini.zucchini_back.domain.user.service;

import com.zucchini.zucchini_back.domain.user.dto.UserRequestDto;
import com.zucchini.zucchini_back.domain.user.dto.UserResponseDto;

import java.util.Map;

public interface UserService {
    void addUser(UserRequestDto userRequestDto);

    UserResponseDto findUser(String userId);

    UserResponseDto.TokenInfo login(String id, String password);

    void logout(String accessToken);
}
