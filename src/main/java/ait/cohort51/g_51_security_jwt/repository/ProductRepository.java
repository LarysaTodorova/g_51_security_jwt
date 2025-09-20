package ait.cohort51.g_51_security_jwt.repository;

import ait.cohort51.g_51_security_jwt.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
