package com.zucchini.zucchini_back.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zucchini.zucchini_back.global.common.auth.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

// 스프링 시큐리티 기본 설정 클래스
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final CustomUserDetailsService customUserDetailsService;
    private final JWTProvider jwtProvider;
    private final RedisTemplate redisTemplate;

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Cross Site Request Forgery(사이트 간 위조 요청) 방지 기능 비활성화
        // 이유 : rest api를 이용한 서버면 session 기반 인증과는 달리 서버에 인증정보를 보관하지 않으므로 CSRF 방지 기능이 불필요함
        http.csrf().disable()
                // CORS 처리
                .cors()
                .configurationSource(corsConfigurationSource())
                // 스프링 시큐리티 세션 정책 Stateless 서버로 만듦 -> JWT 기반 인증을 사용하기 때문
                // 즉, 스프링 시큐리티가 세션을 생성하지 않고 세션이 존재해도 사용하지 않음
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // 기본으로 제공하는 formLogin 사용 안함 처리
                .formLogin().disable()
                // Http Basic 인증 비활성화 -> JWT 기반 인증 사용하기 때문
                .httpBasic().disable()
                // 시큐리티 처리에 HttpServletRequest를 이용한다는 의미
                .authorizeRequests()
                // POST 요청은 인증을 요구하지 않고 접근 허용, 여기선 회원가입과 로그인 요청 시 인증을 요구하지 않고 접근을 허용함
                .antMatchers(HttpMethod.POST, "/api/user", "/api/user/login").permitAll()
                // 전체 회원 목록 조회 등 관리자만 접근 할 수 있는 기능
                .antMatchers(HttpMethod.GET, "/api/user")
                // 관리자 권한을 가지고 있어야만 접근 가능
                .access("hasRole('ADMIN')")
                // 그 외 요청 모두 인증되어야 접근 가능
                .anyRequest().authenticated()
                .and()
                // 토큰 관련 예외 처리
                .exceptionHandling()
                // 커스텀 entry point 추가
                .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                // 권한 예외 처리
                .accessDeniedHandler(new CustomAccessDeniedHandler())
                .and()
                // 커스텀 필터 시큐리티 필터 체인에 추가
                .addFilterBefore(new CustomAuthenticationFilter(jwtProvider, redisTemplate), UsernamePasswordAuthenticationFilter.class);

    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 커스텀 구현한 UserDetailsService 등록
        auth.userDetailsService(customUserDetailsService);
    }

    // 스프링 시큐리티 CORS 설정
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 모든 ip에 응답 허용
        configuration.addAllowedOrigin("*");
        // 모든 post, get, put, delete, patch 요청을 허용
        configuration.addAllowedMethod("*");
        // 모든 header에 응답 허용
        configuration.addAllowedHeader("*");
        // 내 서버가 응답을 할 때 JSON을 자바스크립트에서 처리를 할 수 있게 할지를 설정
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
