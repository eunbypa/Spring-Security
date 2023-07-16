package com.zucchini.zucchini_back.domain.user.dao;

import com.zucchini.zucchini_back.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

    // 로그인에 사용한 아이디로 회원을 조회하는 함수
    User findUserByLoginId(String loginId);

}
