package com.workout.email.service;

import com.workout.global.exception.RestApiException;
import com.workout.global.exception.errorcode.MailErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final long CODE_EXPIRE_SECONDS = 300; // 60 * 5 = 300(5분) TTL
    private static final int CODE_LENGTH = 6;       // 생성할 인증 코드의 길이
    private static final int CODE_BOUNDARY = 10;    // 0부터 9까지의 숫자 중 한 개가 각 자리에 들어감

    private final RedisTemplate<String, String> redisTemplate;
    private final JavaMailSender mailSender;

    public void sendEmail(String email) {
        String code = createCode(); // 6자리 코드 생성
        String createdContent = createContents(code); // HTML 본문 생성

        // Redis에 저장 (TTL 5분)
        redisTemplate.opsForValue().set(email, code, CODE_EXPIRE_SECONDS);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setTo(email);
            helper.setSubject("Workout 회원가입 인증 코드");
            helper.setText(createdContent, true);
            mailSender.send(mimeMessage);

            log.info("인증 이메일 전송 성공: {}", email);

        } catch (MessagingException | MailException e) {
            log.error("이메일 전송 실패", e);
            throw new RestApiException(MailErrorCode.FAILED_SEND_MAIL);
        }
    }

    public boolean verifyCode(String email, String inputCode) {
        String storedCode = redisTemplate.opsForValue().get(email);

        if (storedCode == null) {
            log.warn("인증 코드 만료: {}", email);
            return false;
        }

        if (!storedCode.equals(inputCode)) {
            log.warn("인증 코드 불일치");
            return false;
        }

        redisTemplate.delete(email);
        log.info("인증 코드 검증 성공: {}", email);
        return true;
    }

    public String createCode(){
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            builder.append(random.nextInt(CODE_BOUNDARY));
        }
        return String.valueOf(builder);
    }

    public String createContents(String code){

        String content = """
            <html>
              <body>
                <h1>Workout 인증 코드: %s</h1>
                <p>해당 코드를 홈페이지에 입력하세요.</p>
                <footer style='color: grey; font-size: small;'>
                  <p>※본 메일은 자동응답 메일이므로 본 메일에 회신하지 마시기 바랍니다.</p>
                </footer>
              </body>
            </html>
            """.formatted(code);

        return content;
    }
}
