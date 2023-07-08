package dev.sodev.global.email;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final ApplicationEmail email;

    public void sendEmail(EmailRequest request) throws MessagingException {
        //메일 관련 정보
        String host = this.email.getHost();
        final String username = this.email.getUsername(); //네이버 이메일 주소중 @ naver.com 앞주소만 작성
        final String password = this.email.getPassword(); //네이버 이메일 비밀번호를 작성
        int port = this.email.getPort();                  //네이버 SMTP 포트 번호


        //메일 내용
        String recipient = request.getReceiver(); // 받는 사람의 이메일 주소
        String subject = request.getSubject();     // 메일 발송시 제목을 작성
        String body = request.getBody();           // 메일 발송시 내용 작성

        Properties props = System.getProperties();

        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.ssl.trust", host);

        Session session = Session.getDefaultInstance(props, new Authenticator() {
            String un=username;
            String pw=password;
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(un, pw);
            }
        });
        session.setDebug(true); //for debug

        Message mimeMessage = new MimeMessage(session);
        mimeMessage.setFrom(new InternetAddress(this.email.getUsername()+"@naver.com"));
        mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
        mimeMessage.setSubject(subject);
        mimeMessage.setText(body);
        Transport.send(mimeMessage);
    }
}
