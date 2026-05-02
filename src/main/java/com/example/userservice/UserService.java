package com.example.userservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthResponse register(RegisterRequest request) throws Exception {
        if (userRepository.existsByName(request.getName())) {
            throw new Exception("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new Exception("Email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");

        User savedUser = userRepository.save(user);

        String token = jwtUtil.generateToken(savedUser.getName());

        return new AuthResponse(token, savedUser.getName(), savedUser.getRole());
    }

    public AuthResponse login(AuthRequest request) throws Exception {
        Optional<User> userOpt = userRepository.findByName(request.getName());

        if (!userOpt.isPresent()) {
            throw new Exception("User not found");
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new Exception("Invalid password");
        }

        String token = jwtUtil.generateToken(user.getName());

        return new AuthResponse(token, user.getName(), user.getRole());
    }

    public User getUserById(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        return userOpt.orElse(null);
    }
}