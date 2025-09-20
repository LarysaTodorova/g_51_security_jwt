package ait.cohort51.g_51_security_jwt.controller;

import ait.cohort51.g_51_security_jwt.domain.Product;
import ait.cohort51.g_51_security_jwt.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    //доступ даем пользователю с ролью ADMIN
    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public Product save(@RequestBody Product product) {
        return productService.save(product);
    }

    // доступ даем анонимным пользователям
    @GetMapping
    public List<Product> getAll() {
        return productService.getAll();
    }

    // доступ даем пользователям с ролью USER, ADMIN
    @GetMapping("/{id}")
    public Product getById(@PathVariable Long id) {
        return productService.getById(id);
    }

    //доступ даем пользователю с ролью ADMIN
    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        productService.deleteById(id);
    }
}
