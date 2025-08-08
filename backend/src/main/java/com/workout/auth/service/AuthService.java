package com.workout.auth.service;

import com.workout.auth.domain.SessionConst;
import com.workout.auth.domain.UserSessionDto;
import com.workout.user.domain.User;
import com.workout.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthService {

    private final UserService userService;

    public AuthService(UserService userService) {
        this.userService = userService;
    }

    // 로그인
    public User login(String email, String password, HttpServletRequest request) {
        User user = userService.authenticate(email, password);
        log.info("아이디 비번 확인 완료");
        log.info(String.valueOf(user));
        UserSessionDto sessionUser = new UserSessionDto(user.getId(), user.getEmail());
        HttpSession session = request.getSession(true);
        session.setAttribute(SessionConst.LOGIN_MEMBER, sessionUser);

        return user;
    }

    //로그아웃
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

}
