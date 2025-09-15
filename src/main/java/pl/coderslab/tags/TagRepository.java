package pl.coderslab.tags;

import org.springframework.core.metrics.StartupStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.coderslab.invitations.Invitation;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

}
