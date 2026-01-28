package org.example.synjiserver.service;

import org.example.synjiserver.dto.LoginResponse;
import org.example.synjiserver.dto.UserDto;
import org.example.synjiserver.entity.User;
import org.example.synjiserver.entity.VerificationCode;
import org.example.synjiserver.repository.UserRepository;
import org.example.synjiserver.repository.VerificationCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationCodeRepository codeRepository;

    private final WebClient webClient;

    public AuthService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://push.spug.cc").build();
    }

    // 发送验证码
    @Transactional
    public void sendCode(String phoneNumber) {
        // 1. 生成6位随机验证码
        // String code = String.format("%06d", new Random().nextInt(999999));
        String code = "111111"; // 内测固定验证码
        
        // 2. 保存到数据库
        VerificationCode vc = new VerificationCode();
        vc.setPhoneNumber(phoneNumber);
        vc.setCode(code);
        vc.setExpiresAt(LocalDateTime.now().plusMinutes(5)); // 5分钟有效期
        codeRepository.save(vc);

        // 3. 调用 Spug 接口发送短信
        /*
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("name", "讯极日历");
            body.put("code", code);
            body.put("targets", phoneNumber);

            String response = webClient.post()
                    .uri("/send/X4PBx8E5Pq8YAny5")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // 阻塞等待结果，确保发送成功

            System.out.println("短信发送结果: " + response);
        } catch (Exception e) {
            System.err.println("短信发送失败: " + e.getMessage());
            // 即使发送失败，为了防止暴力请求，数据库记录依然保留，或者您可以选择在这里抛出异常回滚事务
            // throw new RuntimeException("短信发送失败，请稍后重试");
        }
        */
        System.out.println("内测模式，验证码固定为: " + code);
    }

    // 登录/注册
    @Transactional
    public LoginResponse login(String phoneNumber, String code) {
        // 1. 校验验证码
        Optional<VerificationCode> validCode = codeRepository
                .findFirstByPhoneNumberAndCodeAndIsUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                        phoneNumber, code, LocalDateTime.now());

        if (validCode.isEmpty()) {
            throw new RuntimeException("验证码无效或已过期");
        }

        // 标记验证码为已使用
        VerificationCode vc = validCode.get();
        vc.setUsed(true);
        codeRepository.save(vc);

        // 2. 查询或创建用户
        User user = userRepository.findByPhoneNumber(phoneNumber).orElse(null);
        boolean isNewUser = false;

        if (user == null) {
            // 注册新用户
            user = new User();
            user.setPhoneNumber(phoneNumber);
            user.setNickname("用户" + phoneNumber.substring(phoneNumber.length() - 4));
            user = userRepository.save(user);
            isNewUser = true;
        }

        // 3. 生成 Token (这里简单模拟，实际应用应使用 JWT)
        String token = "mock-token-" + user.getUserId() + "-" + System.currentTimeMillis();

        // 4. 构造返回结果
        UserDto userDto = new UserDto(
                String.valueOf(user.getUserId()),
                user.getPhoneNumber(),
                user.getNickname(),
                isNewUser
        );

        return new LoginResponse(token, userDto);
    }
}