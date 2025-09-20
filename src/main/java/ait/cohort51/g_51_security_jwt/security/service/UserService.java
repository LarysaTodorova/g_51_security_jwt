package ait.cohort51.g_51_security_jwt.security.service;

import ait.cohort51.g_51_security_jwt.domain.User;
import ait.cohort51.g_51_security_jwt.repository.UserRepository;
import ait.cohort51.g_51_security_jwt.security.AuthUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = repository.findByEmail(username).orElseThrow(
                () -> new UsernameNotFoundException(String.format("User with  email %s not found", username))
        );
        return new AuthUserDetails(user);
    }
}
