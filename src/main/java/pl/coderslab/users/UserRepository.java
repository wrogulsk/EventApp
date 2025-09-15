package pl.coderslab.users;

import jakarta.validation.Valid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.coderslab.users.dto.EditUserRequest;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    public boolean existsByEmail(String email);

    public User findByEmail(String email);

    public List<User> findByLastName(String username);

}
