package pl.coderslab.users;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.coderslab.users.dto.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getUsers();
        if (users.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok().body(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return user != null ? ResponseEntity.ok().body(user) : ResponseEntity.notFound().build();
    }

    @GetMapping("/total")
    public ResponseEntity<Long> getAllUsersTotal() {
        Long total = userService.getTotalUsersCount();
        return ResponseEntity.ok().body(total);
    }

    @GetMapping("/lastname/{lastName}")
    public ResponseEntity<List<UserResponse>> getUsersByLastName(@PathVariable String lastName) {
        List<User> users = userService.getUserByLastName(lastName);

        if (users.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<UserResponse> userResponses = users.stream()
                .map(UserResponse::fromUser)
                .collect(Collectors.toList());

        return ResponseEntity.ok(userResponses);
    }

    @PostMapping("/add")
    public ResponseEntity<Long> addUser(@RequestBody CreateUserRequest user) {
        Long id = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody EditUserRequest user) {
        userService.updateUser(user);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PutMapping("/{id}/email")
    public ResponseEntity<UserResponse> updateUserEmail(@PathVariable Long id, @Valid @RequestBody UpdateEmailRequest request) {
        userService.updateUserEmail(id, request.email());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        if (id == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.OK).body("User deleted successfully");
    }
}

