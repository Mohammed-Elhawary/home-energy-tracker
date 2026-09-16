package com.micro.user_service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.micro.user_service.dto.UserDTo;
import com.micro.user_service.entity.User;
import com.micro.user_service.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDTo createUser(UserDTo userDTo) {
        final User user = User.builder()
                .name(userDTo.getName())
                .surname(userDTo.getSurname())
                .email(userDTo.getEmail())
                .address(userDTo.getAddress())
                .alerting(userDTo.getAlerting())
                .energyAlertingThreshold(userDTo.getEnergyAlertingThreshold())
                .build();
        final User savedUser = userRepository.save(user);
        return toDto(savedUser);
    }

    public UserDTo getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public UserDTo updateUser(Long id, UserDTo userDTo) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    existingUser.setName(userDTo.getName());
                    existingUser.setSurname(userDTo.getSurname());
                    existingUser.setEmail(userDTo.getEmail());
                    existingUser.setAddress(userDTo.getAddress());
                    existingUser.setAlerting(userDTo.getAlerting());
                    existingUser.setEnergyAlertingThreshold(userDTo.getEnergyAlertingThreshold());
                    User updatedUser = userRepository.save(existingUser);
                    return toDto(updatedUser);
                })
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public UserDTo patchUser(Long id, UserDTo userDTo) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    if (userDTo.getName() != null) {
                        existingUser.setName(userDTo.getName());
                    }
                    if (userDTo.getSurname() != null) {
                        existingUser.setSurname(userDTo.getSurname());
                    }
                    if (userDTo.getEmail() != null) {
                        existingUser.setEmail(userDTo.getEmail());
                    }
                    if (userDTo.getAddress() != null) {
                        existingUser.setAddress(userDTo.getAddress());
                    }
                    if (userDTo.getAlerting() != null) {
                        existingUser.setAlerting(userDTo.getAlerting());
                    }
                    if (userDTo.getEnergyAlertingThreshold() != null) {
                        existingUser.setEnergyAlertingThreshold(userDTo.getEnergyAlertingThreshold());
                    }
                    User updatedUser = userRepository.save(existingUser);
                    return toDto(updatedUser);
                })
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    public List<UserDTo> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .filter(user -> user.getId() != null
                        && user.getName() != null
                        && user.getSurname() != null
                        && user.getEmail() != null)
                .map(this::toDto)
                .toList();
    }

    private UserDTo toDto(User user) {
        return UserDTo.builder()
                .id(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .email(user.getEmail())
                .address(user.getAddress())
                .alerting(user.getAlerting())
                .energyAlertingThreshold(user.getEnergyAlertingThreshold())
                .build();
    }

}
