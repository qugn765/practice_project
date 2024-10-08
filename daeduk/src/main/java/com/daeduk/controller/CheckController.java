package com.daeduk.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.daeduk.dto.UserDto;
import com.daeduk.exception.NotFoundException;
import com.daeduk.service.MailService;
import com.daeduk.service.UserService;
import com.daeduk.service.impl.UserServiceImpl;

@RestController
public class CheckController {

    @Autowired
    UserService userService;

    @Autowired
    MailService mailService;

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final String SUCCESS = "로그인 성공";
    private final String PASSWORD_FAIL = "패스워드 불일치";
    private final String EMAIL_FAIL = "이메일 불일치";
    private final String SERVER_ERROR = "서버 에러";

    /* 로그인 요청 */
    @PostMapping("/confirm")
    public ResponseEntity<Map<String, String>> confirmLogin(@RequestBody Map<String, String> loginData) {

        String email = loginData.get("email");
        String password = loginData.get("password");
        Map<String, String> response = new HashMap<>();

        try {
            Boolean loginResult = userService.confirmLogin(email, password);
            if (loginResult) {
                response.put("message", SUCCESS);
                response.put("success", "true");
                return ResponseEntity.ok().body(response);
            } else {
                response.put("message", PASSWORD_FAIL);
                return ResponseEntity.status(401).body(response);
            }
        } catch (NotFoundException e) {
            logger.info("NotFoundException: {}", EMAIL_FAIL);
            response.put("message", EMAIL_FAIL);
            return ResponseEntity.status(401).body(response);
        } catch (Exception e) {
            logger.info("Exception : {}", SERVER_ERROR);
            response.put("message", SERVER_ERROR);
            return ResponseEntity.status(500).body(response);
        }
    }

    /* 비밀번호 이메일 전송 */
    @PostMapping("/findEmail")
    public ResponseEntity<Map<String, String>> findPassword(@RequestBody Map<String, String> email) {

        Map<String, String> response = new HashMap<>();

        String findEmail = email.get("email");
        UserDto user;

        try {
            user = userService.findPassword(findEmail);
        } catch (NotFoundException e) {
            logger.info("NotFoundException: {}", "해당 이메일로 가입된 이력이 없습니다.");
            response.put("message", "해당 이메일로 가입된 이력이 없습니다.");
            return ResponseEntity.status(401).body(response);
        } catch (Exception e) {
            logger.info("Exception : {}", SERVER_ERROR);
            response.put("message", SERVER_ERROR);
            return ResponseEntity.status(500).body(response);
        }

        try {
            mailService.sendSimpleMessage(user);
        } catch (Exception e) {
            
            System.out.println(e.getStackTrace());
            System.out.println(e.getMessage());

            logger.info("Exception: {}", "이메일 발송 실패했습니다.");
            response.put("message", "이메일 발송 실패했습니다.");
            return ResponseEntity.status(500).body(response);
        }

        response.put("success", "true");
        return ResponseEntity.ok().body(response);
    }

    /* 회원가입 */
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@RequestBody Map<String, String> signupData) {

        /* 비밀번호에 <> 있으면 &lt; &gt; 바꿔서 넣고 이메일로 비밀번호 알려줄때는 다시 원래대로 해서 보내주기 */

        String email = signupData.get("email");
        String password = signupData.get("password");

        Boolean signupResult = userService.signup(email, password);

        Map<String, String> response = new HashMap<>();

        if (signupResult) {
            response.put("success", "true");
            return ResponseEntity.ok().body(response);
        } else {
            response.put("success", "false");
            return ResponseEntity.status(401).body(response);
        }

    }

}
