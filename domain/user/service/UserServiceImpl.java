package com.zucchini.zucchini_back.domain.user.service;

import com.zucchini.zucchini_back.domain.user.dao.UserRepository;
import com.zucchini.zucchini_back.domain.user.dto.UserRequestDto;
import com.zucchini.zucchini_back.domain.user.dto.UserResponseDto;
import com.zucchini.zucchini_back.domain.user.entity.User;
import com.zucchini.zucchini_back.global.common.auth.CustomAuthenticationEntryPoint;
import com.zucchini.zucchini_back.global.common.auth.CustomUserDetails;
import com.zucchini.zucchini_back.global.common.auth.JWTProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final Logger log = LoggerFactory.getLogger(CustomAuthenticationEntryPoint.class);

    private final UserRepository userRepository;
    private final JWTProvider jwtProvider;
    private final RedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    @Override
    public void addUser(UserRequestDto userRequestDto) {
        // 비밀번호는 암호화해서 DB에 저장
        User user = new User(userRequestDto.getId(), passwordEncoder.encode(userRequestDto.getPassword()), userRequestDto.getName());
        userRepository.save(user);
    }

    @Override
    public UserResponseDto findUser(String userId) {
        User user = userRepository.findUserByLoginId(userId);
        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setId(user.getLoginId());
        userResponseDto.setName(user.getName());
        return userResponseDto;
    }

    @Override
    public UserResponseDto.TokenInfo login(String id, String password) {
        // 1. Login ID/PW 를 기반으로 Authentication 객체 생성
        // 이때 authentication 는 인증 여부를 확인하는 authenticated 값이 false
//        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(id, password);
        log.info("아이디 : " + id);
        User user = userRepository.findUserByLoginId(id);
        log.info("조회된 user : " + user);

        // 해당 아이디 회원 존재하지 않음
        if(user == null) return null;
        // 2. 실제 검증 (사용자 비밀번호 체크)이 이루어지는 부분
        // authenticate 매서드가 실행될 때 CustomUserDetailsService 에서 만든 loadUserByUsername 메서드가 실행
        // This object has not been built 오류 발생
//        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        // 패스워드 불일치
        log.info("패스워드 비교 : " + passwordEncoder.matches(password, user.getPassword()));
        if(!passwordEncoder.matches(password, user.getPassword())) return null;

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        CustomUserDetails customUserDetails = new CustomUserDetails(user.getLoginId(), user.getPassword(), user.getName(), "USER");
        UserResponseDto.TokenInfo tokenInfo = jwtProvider.generateToken(customUserDetails);
        log.info("토큰 생성 : " + tokenInfo);


        // 4. RefreshToken Redis 저장 (expirationTime 설정을 통해 자동 삭제 처리)
        redisTemplate.opsForValue()
                .set("RT:" + id, tokenInfo.getRefreshToken(), tokenInfo.getRefreshTokenExpirationTime(), TimeUnit.MILLISECONDS);

        return tokenInfo;
    }

    @Override
    public void logout(String accessToken) {

        // Access Token으로 인증 정보 가져오기
        Authentication authentication = jwtProvider.getAuthentication(accessToken);

        // Redis 에서 해당 회원의 아이디 로 저장된 Refresh Token 이 있는지 여부를 확인 후 있을 경우 삭제
        if (redisTemplate.opsForValue().get("RT:" + authentication.getName()) != null) {
            // Refresh Token 삭제
            log.info("토큰 삭제 완료");
            redisTemplate.delete("RT:" + authentication.getName());
        }

        // 해당 Access Token 유효시간 가지고 와서 BlackList 로 저장하기
        Long expiration = jwtProvider.getExpiration(accessToken);
        redisTemplate.opsForValue()
                .set(accessToken, "logout", expiration, TimeUnit.MILLISECONDS);

    }


}
