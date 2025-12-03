package sk.semestralka.studybase.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sk.semestralka.studybase.DTO.RegisterRequest;
import sk.semestralka.studybase.DTO.LoginRequest;
import sk.semestralka.studybase.DTO.UpdateUserRequest;
import sk.semestralka.studybase.DTO.UserResponse;
import sk.semestralka.studybase.Entity.User;
import sk.semestralka.studybase.Repository.UserRepository;
import sk.semestralka.studybase.Util.PasswordUtil;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordUtil passwordUtil;

    public UserResponse register(RegisterRequest request) {
        // Skontroluj či email už existuje
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email už je registrovaný: " + request.getEmail());
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordUtil.hashPassword(request.getPassword()));

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getUserId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getCreatedAt()
        );
    }


    public UserResponse login(LoginRequest request) {
        // Nájdi používateľa podľa emailu
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Nesprávny email alebo heslo");
        }

        User user = userOpt.get();


        if (!passwordUtil.verifyPassword(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Nesprávny email alebo heslo");
        }


        return new UserResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }

    /**
     * Získanie používateľa podľa ID
     */
    public UserResponse getUserById(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Používateľ nebol nájdený: " + userId);
        }

        User user = userOpt.get();
        return new UserResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }

    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Používateľ nebol nájdený: " + userId);
        }

        User user = userOpt.get();

        // Kontrola či email už existuje (ak sa mení)
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email už je registrovaný: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        // Aktualizácia mena
        if (request.getName() != null) {
            user.setName(request.getName());
        }

        User updatedUser = userRepository.save(user);

        return new UserResponse(
                updatedUser.getUserId(),
                updatedUser.getName(),
                updatedUser.getEmail(),
                updatedUser.getCreatedAt()
        );
    }

}