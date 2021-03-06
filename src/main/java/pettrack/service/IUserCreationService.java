package pettrack.service;

import pettrack.domain.User;

public interface IUserCreationService {
    void save(final User user) ;

    boolean emailExists(final String email);

    User findByConfirmationToken(final String token);
}
