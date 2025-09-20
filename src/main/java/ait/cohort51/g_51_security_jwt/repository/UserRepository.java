package ait.cohort51.g_51_security_jwt.repository;

import ait.cohort51.g_51_security_jwt.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
