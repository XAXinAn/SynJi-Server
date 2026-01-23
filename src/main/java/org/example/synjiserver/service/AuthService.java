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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationCodeRepository codeRepository;

    // 发送验证码
    @Transactional
    public void sendCode(String phoneNumber) {
        // 1. 生成6位随机验证码
        String code = String.format("%06d", new Random().nextInt(999999));
        
        // 2. 保存到数据库
        VerificationCode vc = new VerificationCode();
        vc.setPhoneNumber(phoneNumber);
        vc.setCode(code);
        vc.setExpiresAt(LocalDateTime.now().plusMinutes(5)); // 5分钟有效期
        codeRepository.save(vc);

        // 3. 实际场景这里调用短信API发送 code 给 phoneNumber
        System.out.println("=== 模拟短信发送 ===");
        System.out.println("手机号: " + phoneNumber);
        System.out.println("验证码: " + code);
        System.out.println("====================");
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
