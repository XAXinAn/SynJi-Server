package org.example.synjiserver.service;

import org.example.synjiserver.dto.UserDto;
import org.example.synjiserver.entity.User;
import org.example.synjiserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }
        // 转换为 DTO
        return new UserDto(
                String.valueOf(user.getUserId()),
                user.getPhoneNumber(),
                user.getNickname(),
                false // 获取详情时不再是新用户
        );
    }

    @Transactional
    public UserDto updateUser(Long userId, String nickname) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setNickname(nickname);
        User savedUser = userRepository.save(user);
        
        return new UserDto(
                String.valueOf(savedUser.getUserId()),
                savedUser.getPhoneNumber(),
                savedUser.getNickname(),
                false
        );
    }
}
