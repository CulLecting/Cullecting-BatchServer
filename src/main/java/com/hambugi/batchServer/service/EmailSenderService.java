package com.hambugi.batchServer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailSenderService {
    private final JavaMailSender mailSender;

    public void send(String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(""); // 수신자 이메일
        message.setSubject(subject);
        message.setText(content);

        try {
            mailSender.send(message);
            System.out.println("✅ 이메일 전송 완료");
        } catch (Exception e) {
            System.out.println("⚠️ 이메일 전송 실패: " + e.getMessage());
        }
    }
}
