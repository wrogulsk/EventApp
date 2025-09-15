package pl.coderslab.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.coderslab.events.Event;
import pl.coderslab.roles.Role;
import pl.coderslab.roles.RoleRepository;
import pl.coderslab.users.dto.CreateUserRequest;
import pl.coderslab.users.dto.EditUserRequest;
import pl.coderslab.users.dto.UserResponse;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> getUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getRoles().stream()
                                .map(Role::getName)
                                .collect(Collectors.toSet())
                ))
                .collect(Collectors.toList());
    }

//    public ResponseEntity<User> getUserById(Long id) {
//        User user = userRepository.findById(id).orElse(null);
//        return new ResponseEntity<>(user, HttpStatus.OK);
//    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public Long createUser(CreateUserRequest createUserRequest) {
        String email = createUserRequest.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use");
        }

        validatePassword(createUserRequest.password());

        Set<Role> roleEntities = new HashSet<>(roleRepository.findAllById(createUserRequest.roleIds()));
        if (roleEntities.size() != createUserRequest.roleIds().size()) {
            throw new IllegalArgumentException("One or more roles not found");
        }

        User u = new User();
        u.setFirstName(createUserRequest.firstName().trim());
        u.setLastName(createUserRequest.lastName().trim());
        u.setEmail(createUserRequest.email().trim().toLowerCase(Locale.ROOT));
        u.setPassword(passwordEncoder.encode(createUserRequest.password().trim()));
        u.setRoles(roleEntities);

        return userRepository.save(u).getId();
    }

    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        String trimmedPassword = password.trim();
        List<String> errors = new ArrayList<>();

        if (trimmedPassword.length() < 8) {
            errors.add("Password must be at least 8 characters long");
        }

        if (!hasLowercase(trimmedPassword)) {
            errors.add("Password must contain at least one lowercase letter");
        }

        if (!hasUppercase(trimmedPassword)) {
            errors.add("Password must contain at least one uppercase letter");
        }

        if (!hasDigit(trimmedPassword)) {
            errors.add("Password must contain at least one digit");
        }

        if (isCommonPassword(trimmedPassword)) {
            errors.add("Password is too common, please choose a stronger password");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Password validation failed: " + String.join(", ", errors));
        }
    }

    public void deleteUser(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        userRepository.delete(user);
    }

    public void updateUserEmail(long id, String newEmail) {
        User user = userRepository.findById(id).get();
        user.setEmail(newEmail);
        userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public void updateUser(EditUserRequest editRequest) {
        User user = userRepository.findById(editRequest.id())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.getEmail().equals(editRequest.email()) &&
                userRepository.existsByEmail(editRequest.email())) {
            throw new IllegalArgumentException("Email already in use by another user");
        }

        Set<Role> roleEntities = new HashSet<>(roleRepository.findAllById(editRequest.roleIds()));
        if (roleEntities.size() != editRequest.roleIds().size()) {
            throw new IllegalArgumentException("One or more roles not found");
        }

        user.setFirstName(editRequest.firstName().trim());
        user.setLastName(editRequest.lastName().trim());
        user.setEmail(editRequest.email().trim().toLowerCase());
        user.setRoles(roleEntities);

        userRepository.save(user);
    }

    public long getTotalUsersCount() {
        return userRepository.count();
    }

    public List<User> getUserByLastName(String lastName) {
        return userRepository.findByLastName(lastName);
    }

    private boolean hasLowercase(String password) {
        return password.matches(".*[a-z].*");
    }

    private boolean hasUppercase(String password) {
        return password.matches(".*[A-Z].*");
    }

    private boolean hasDigit(String password) {
        return password.matches(".*\\d.*");
    }

    private boolean isCommonPassword(String password) {
        Set<String> commonPasswords = Set.of(
                "password", "123456", "password123", "admin", "qwerty",
                "letmein", "welcome", "monkey", "dragon", "master",
                "123456789", "qwerty123", "password1", "admin123",
                "welcome123", "login", "guest", "test", "user"
        );
        return commonPasswords.contains(password.toLowerCase());
    }
}


