package com.workout.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FcmService {

  private final FirebaseMessaging
      firebaseMessaging;

  public FcmService(FirebaseMessaging firebaseMessaging) {
    this.firebaseMessaging = firebaseMessaging;
  }

  // 특정 토큰을 대상으로 알림 전송
  public void sendNotification(String targetToken, String title, String body) {
    if (targetToken == null || targetToken.isBlank()) {
      log.warn("FCM 타겟 토큰이 비어있어 알림을 전송할 수 없습니다.");
      return;
    }

    Notification notification = Notification.builder()
        .setTitle(title)
        .setBody(body)
        .build();

    Message message = Message.builder()
        .setToken(targetToken) // 대상 기기의 FCM 토큰
        .setNotification(notification)
        // .putData("key", "value") // 필요시 커스텀 데이터 추가
        .build();

    try {
      String response = firebaseMessaging.send(message);
      log.info("FCM 알림 전송 성공: {}", response);
    } catch (FirebaseMessagingException e) {
      log.error("FCM 알림 전송 실패: {}", e.getMessage());
      // TODO: 토큰이 유효하지 않은 경우(Unregistered) DB에서 해당 토큰 삭제 로직 추가
    }
  }
}