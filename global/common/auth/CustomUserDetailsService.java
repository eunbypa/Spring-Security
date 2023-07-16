package com.zucchini.zucchini_back.global.common.auth;

import com.zucchini.zucchini_back.domain.user.dao.UserRepository;
import com.zucchini.zucchini_back.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// 기존에 제공되는 UserDetailsService를 상속받는 클래스
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByLoginId(username);
        if(user == null) throw new UsernameNotFoundException("해당하는 유저를 찾을 수 없습니다.");
        CustomUserDetails userDetails = new CustomUserDetails(user.getLoginId(), user.getPassword(), user.getName(), "ADMIN");

        return userDetails;
    }
}
