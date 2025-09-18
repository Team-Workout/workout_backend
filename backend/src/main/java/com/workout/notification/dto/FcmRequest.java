package com.workout.notification.dto; // (패키지 경로는 예시입니다)

import com.google.firebase.messaging.Message;

/**
 * FCM 발송 요청을 래핑하는 객체
 * 배치 프로세서에서 생성되어 Writer로 전달되고, FcmService에서 소비됩니다.
 *
 * @param targetToken  [오류 처리용] 실제 발송 대상 토큰 (읽기 가능해야 함)
 * @param messagePayload [SDK 전송용] Firebase SDK가 요구하는 Message 객체
 */
public record FcmRequest(
    String targetToken,
    Message messagePayload
) {
}