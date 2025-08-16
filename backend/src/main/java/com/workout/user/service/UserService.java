package com.workout.user.service;

import com.workout.auth.dto.SignupRequest;
import com.workout.gym.domain.Gym;
import com.workout.gym.service.GymService;
import com.workout.user.domain.AccountStatus;
import com.workout.user.domain.User;
import com.workout.user.repository.UserRepository;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final GymService gymService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, GymService gymService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.gymService = gymService;
        this.passwordEncoder = passwordEncoder;
    }

    public User authenticate(String email, String password) {
        // 1. 이메일로 사용자를 조회합니다.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        // 2. 평문 비밀번호와 암호화된 비밀번호를 비교합니다.
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3. 비밀번호가 일치하면 사용자 정보를 반환합니다.
        return user;
    }

    private void ensureUserNameAndEmailAreUnique(String name, String email) throws IllegalArgumentException {
        if(userRepository.existsByName(name)) {
            throw new IllegalArgumentException("존재하는 이름입니다.");
        }
        if(userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("존재하는 이메일 입니다");
        }
    }

    public void ensureGymIdIsValid(Long gymId) {
        if(!gymService.existsById(gymId)) {
            throw new IllegalArgumentException("존재하지 않는 헬스장입니다.");
        }
    }

    public User registerUser(SignupRequest signupRequest) {
        log.info("1");
        Gym gym = gymService.findById(signupRequest.gymId());
        log.info("2");
        ensureUserNameAndEmailAreUnique(signupRequest.name(), signupRequest.email());

        String encodedPassword = passwordEncoder.encode(signupRequest.password());

        User user = signupRequest.toEntity(gym, encodedPassword);

        return userRepository.save(user);
    }
}
