package com.zucchini.zucchini_back.domain.user.api;

import com.zucchini.zucchini_back.domain.user.dto.UserRequestDto;
import com.zucchini.zucchini_back.domain.user.dto.UserResponseDto;
import com.zucchini.zucchini_back.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("")
    ResponseEntity<?> signUp(@RequestBody UserRequestDto userRequestDto){
        userService.addUser(userRequestDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/login")
    ResponseEntity<?> login(@RequestBody UserRequestDto userRequestDto){
        UserResponseDto.TokenInfo tokenInfo = userService.login(userRequestDto.getId(), userRequestDto.getPassword());

        return new ResponseEntity<>(tokenInfo, HttpStatus.OK);
    }

    @GetMapping("/logout")
    ResponseEntity<?> logout(HttpServletRequest request) {
        String accessToken = (String)request.getAttribute("accessToken");
        userService.logout(accessToken);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("")
    ResponseEntity<?> findUsers() {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/{userId}")
    ResponseEntity<?> findUserInfo(@PathVariable String userId) {
        UserResponseDto userResponseDto = userService.findUser(userId);
        return new ResponseEntity<>(userResponseDto, HttpStatus.OK);
    }

}
