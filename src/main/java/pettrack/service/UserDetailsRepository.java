package pettrack.service;

import org.springframework.data.mongodb.repository.MongoRepository;
import pettrack.domain.User;

public interface UserDetailsRepository extends MongoRepository<User, String> {

    User findByUsername(final String username);

}
