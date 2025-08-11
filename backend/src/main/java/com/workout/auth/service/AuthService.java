// AuthService.java

package com.workout.auth.service;

import com.workout.auth.domain.UserPrincipal;
import com.workout.user.domain.User;
import com.workout.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse; // HttpServletResponse import 추가
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository; // SecurityContextRepository import 추가
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthService {

    private final UserService userService;
    private final SecurityContextRepository securityContextRepository; // SecurityContextRepository 주입


    public AuthService(UserService userService, SecurityContextRepository securityContextRepository) {
        this.userService = userService;
        this.securityContextRepository = securityContextRepository;
    }

    // 로그인
    public User login(String email, String password, HttpServletRequest request, HttpServletResponse response) {
        User user = userService.authenticate(email, password);
        log.info("아이디 비번 확인 완료: {}", user.getEmail());

        UserPrincipal userPrincipal = new UserPrincipal(user);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            userPrincipal, null, userPrincipal.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        // [핵심] SecurityContext를 세션에 저장하고 쿠키를 발급하도록 명시적으로 호출
        securityContextRepository.saveContext(context, request, response);

        return user;
    }
}