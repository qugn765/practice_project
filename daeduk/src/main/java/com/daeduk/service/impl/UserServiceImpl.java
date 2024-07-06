package com.daeduk.service.impl;

import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daeduk.dto.UserDto;
import com.daeduk.entity.UserEntity;
import com.daeduk.exception.NotFoundException;
import com.daeduk.repository.UserRepository;
import com.daeduk.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public Boolean confirmLogin(String email, String password) {

        Optional<UserEntity> userValue = userRepository.findByEmail(email);

        UserEntity entity = userValue.orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        UserDto userDto = modelMapper.map(entity, UserDto.class);

        if (userDto.getPassword().equals(password)) {
            logger.info("Successful login for email: {}", email);
            return true;
        } else {
            logger.warn("Failed login for email: {} - Wrong password", email);
            return false;
        }

    }

    @Override
    public Boolean signup(String email, String password) {

        /* 최소한의 스크립팅 방어로직 */
        if (password.contains("<")) {
            password = password.replaceAll("<", "&lt;");
        }
        if (password.contains(">")) {
            password = password.replaceAll(">", "&gt;");
        }

        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPassword(password);

        try {
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            return false;
        }

    }
}
