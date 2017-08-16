package pettrack.service;

import pettrack.domain.User;

public interface UserCreationService {
    void save(final User user) ;

    boolean emailExists(final User user);

    User findByConfirmationToken(final String token);
}
