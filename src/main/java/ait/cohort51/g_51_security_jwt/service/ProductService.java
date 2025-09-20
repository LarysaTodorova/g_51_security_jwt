package ait.cohort51.g_51_security_jwt.service;

import ait.cohort51.g_51_security_jwt.domain.Product;

import java.util.List;

public interface ProductService  {

    Product save(Product product);

    List<Product> getAll();

    Product getById(Long id);

    void deleteById(Long id);



}
