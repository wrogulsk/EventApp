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

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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
                                .collect(Collectors.toSet()),
                        user.getEvents().stream()
                                .map(Event::getTitle)
                                .collect(Collectors.toSet())
                ))
                .collect(Collectors.toList());
    }

    public ResponseEntity<User> getUserById(Long id) {
        User user = userRepository.findById(id).orElse(null);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    public Long createUser(CreateUserRequest createUserRequest) {
        if (userRepository.existsByEmail(createUserRequest.email().trim().toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Email already in use");
        }

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
}


