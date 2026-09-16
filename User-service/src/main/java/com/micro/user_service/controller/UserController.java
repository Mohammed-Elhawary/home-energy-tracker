package com.micro.user_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.micro.user_service.dto.UserDTo;
import com.micro.user_service.service.UserService;




@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

        @PostMapping
        public ResponseEntity<UserDTo> createUser(@RequestBody UserDTo userDTo) {
            UserDTo createdUser = userService.createUser(userDTo);
            return ResponseEntity.ok(createdUser);
        }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserDTo> getUserById(@PathVariable Long id) {

        UserDTo user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<Iterable<UserDTo>> getAllUsers() {
        Iterable<UserDTo> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<UserDTo> putMethodName(@PathVariable Long id, @RequestBody UserDTo userDTo) {
        UserDTo updatedUser = userService.updateUser(id, userDTo);
        if (updatedUser == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedUser);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserDTo> patchMethodName(@PathVariable Long id, @RequestBody UserDTo userDTo) {
        UserDTo updatedUser = userService.patchUser(id, userDTo);
        if (updatedUser == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    
}
