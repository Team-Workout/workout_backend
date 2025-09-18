package com.workout.notification.service;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import com.workout.notification.dto.FcmRequest;
import com.workout.notification.event.TokenCleanupEvent;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FcmService {

  private final ApplicationEventPublisher eventPublisher; // 3. 이벤트 발행기 주입
  private final FirebaseMessaging firebaseMessaging;

  public FcmService(FirebaseMessaging firebaseMessaging, ApplicationEventPublisher eventPublisher) {
    this.firebaseMessaging = firebaseMessaging;
    this.eventPublisher = eventPublisher;
  }

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
        .build();

    try {
      String response = firebaseMessaging.send(message);
      log.info("FCM 알림 전송 성공: {}", response);
    } catch (FirebaseMessagingException e) {
      log.error("FCM 알림 전송 실패: {}", e.getMessage());
      // TODO: 토큰이 유효하지 않은 경우(Unregistered) DB에서 해당 토큰 삭제 로직 추가
    }
  }

  public void sendEachNotifications(List<FcmRequest> requests) {
    if (requests == null || requests.isEmpty()) {
      log.info("발송할 메시지가 없습니다.");
      return;
    }

    // 1. FcmRequest에서 Message 페이로드만 추출
    List<Message> messages = requests.stream()
        .map(FcmRequest::messagePayload)
        .collect(Collectors.toList());

    try {
      // 2. [핵심] sendAll() 대신 sendEach() 사용
      BatchResponse response = firebaseMessaging.sendEach(messages);

      log.info("FCM 일괄 발송 완료: 총 요청={}, 성공={}, 실패={}",
          messages.size(), response.getSuccessCount(), response.getFailureCount());

      // 3. 실패한 요청이 있다면, 상세 처리 로직 호출
      if (response.getFailureCount() > 0) {
        handleFailedTokens(response.getResponses(), requests);
      }

    } catch (FirebaseMessagingException e) {
      // sendEach API 호출 자체가 실패한 경우 (네트워크 문제 등)
      log.error("FCM sendEach API 호출 자체 실패: {}", e.getMessage());
    }
  }

  // [수정] sendEach의 응답(BatchResponse)을 처리하도록 로직 개선
  private void handleFailedTokens(List<SendResponse> responses, List<FcmRequest> originalRequests) {
    for (int i = 0; i < responses.size(); i++) {
      SendResponse sendResponse = responses.get(i);

      // 성공한 응답은 건너뜁니다.
      if (sendResponse.isSuccessful()) {
        continue;
      }

      // 실패한 경우, 원본 요청에서 토큰 정보를 가져옵니다.
      String originalToken = originalRequests.get(i).targetToken();
      String errorCode = sendResponse.getException().getMessagingErrorCode().name();

      log.warn("알림 발송 실패: Token={}, ErrorCode={}, Message={}",
          originalToken, errorCode, sendResponse.getException().getMessage());

      // 토큰이 유효하지 않거나(UNREGISTERED), 형식이 잘못된 경우(INVALID_ARGUMENT)
      // 해당 토큰을 DB에서 삭제하도록 이벤트를 발행합니다.
      if ("UNREGISTERED".equals(errorCode) || "INVALID_ARGUMENT".equals(errorCode)) {
        log.info("DB 토큰 삭제 이벤트 발행: {}", originalToken);
        eventPublisher.publishEvent(new TokenCleanupEvent(originalToken));
      }
    }
  }
}