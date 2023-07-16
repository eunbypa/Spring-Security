package com.zucchini.zucchini_back.domain.user.dto;

import lombok.Data;

@Data
public class UserRequestDto {

    String id;
    String password;

    String name;

}
