package ait.cohort51.g_51_security_jwt.controller;

import ait.cohort51.g_51_security_jwt.domain.Product;
import ait.cohort51.g_51_security_jwt.domain.Role;
import ait.cohort51.g_51_security_jwt.domain.User;
import ait.cohort51.g_51_security_jwt.repository.ProductRepository;
import ait.cohort51.g_51_security_jwt.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;


// @SpringBootTest при старте теста запускает наше приложение
// полноценно на тестовом экземпляре Tomcat
// webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT -
// этот атрибут говорит о том, что тестовый экземпляр Tomcat с нашим приложением
// должен подняться на случайно выбранном свободном порту нашей операционной системы
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// В интеграционных тестах иногда важен порядок запуска тестов.
// Если нам важен порядок, мы должны включить соответствующую настройку.
// И аннотация @TestMethodOrder(MethodOrderer.OrderAnnotation.class) говорит
// о том, что мы будем запускать методы в определенном порядке,
// регулируя это при помощи другой аннотации, которая будет стоять
// на наших тестовых методах
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductControllerTestIT {

    // Аннотация @LocalServerPort позволяет в это поле
    // сохранить значение случайно выбранного порта, на котором стартовал
    // тестовый Tomcat
    @LocalServerPort
    private int port;

    // TestRestTemplate - это такой объект, при помощи которого
    // мы можем отправлять http-запросы на тестируемое приложение
    // и получать http-ответы.
    // Аннотация @Autowired говорит фреймворку о том, что в это поле
    // при запуске приложения нужно внедрить объект типа TestRestTemplate.
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Value("${key.access}")
    private String accessPhrase;

    private Product testProduct;
    private HttpHeaders headers;
    private User admin;
    private String adminAccessToken;
    private SecretKey accessKey;

    // Этот метод автоматически запускается перед каждым тестом
    // и выполняет какие-то необходимые действия, например,
    // создание тестовых объектов, запись чего-то в базу,
    // генерация токенов и проч.
    @BeforeEach
    public void setUp() {
        // Создаём тестовый продукт
        testProduct = createTestProduct();
        // Здесь мы создаём заголовок для будущего http-запроса
        // Пока нам в них нечего добавлять, они будут пустые
        headers = new HttpHeaders();
        // Создаём секретный ключ
        accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessPhrase));
        // Создаём объект админа
        admin = createAdmin();
        // Создаём токен доступа для админа
        adminAccessToken = generateAdminAccessToken();
    }

    // Аннотация @Test говорит фреймворку о том, что это именно тестовый метод,
    // и его нужно запускать как тест
    @Test
    // Аннотация @Order(1) говорит о том, что этот метод нужно запустить первым по счету
    @Order(1)
    public void checkRequestForAllProducts() {

        // Здесь мы создаём объект http-запроса, передавая
        // ему в конструктор объект заголовков (в нашем случае пустых).
        // При этом запрос мы параметризуем типом Void, что говорит о том,
        // что мы никакой объект не собираемся класть в тело запроса.
        // У нас GET-запрос без тела.
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Здесь мы отправляем на наше тестовое приложение реальный http-запрос
        // и получаем реальный http-ответ. Четыре аргумента метода:
        // 1. Эндпоинт, на который отправляется запрос
        // 2. Тип (метод) запроса
        // 3. Объект запроса
        // 4. Тип данных, который мы ожидаем увидеть в ответе от сервера
        ResponseEntity<Product[]> response = restTemplate.exchange(
                "/products", HttpMethod.GET, request, Product[].class
        );

        // Здесь мы проверяем, действительно ли от сервера пришёл статус ответа, который мы ожидали
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Unexpected HTTP status");

        // Получим тело из запроса
        Product[] body = response.getBody();

        // Проверяем, пришло ли вообще тело в ответе
        assertNotNull(body, "Response body should not be null");

        // Здесь мы проверяем, что все поля всех продуктов заполнены
        // значениями. В базе у нас не может быть продукта, у которого
        // какое-то из значений null
        for (Product product : body) {
            assertNotNull(product.getId(), "Product id should not be null");
            assertNotNull(product.getTitle(), "Product title should not be null");
            assertNotNull(product.getPrice(), "Product price should not be null");
        }

    }

    @Test
    @Order(2)
    public void checkForbiddenStatusWhileSavingProductWithoutAuthorization() {
        // На этот раз, так как мы хотим отправить объект продукта на сервер
        // в качестве тела запроса, мы параметризуем наш запрос типом Product
        // и передаём в конструктор и тело, и заголовки
        HttpEntity<Product> request = new HttpEntity<>(testProduct, headers);

        ResponseEntity<Product> response = restTemplate.exchange(
                "/products", HttpMethod.POST, request, Product.class
        );

        // Здесь мы уже проверяем, что сервер вернул статус FORBIDDEN,
        // то есть действие нам запрещено, т.к. мы не авторизовались,
        // а сохранение продукта разрешено только админу
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(), "Unexpected HTTP status");
        assertNull(response.getBody(), "Response body should be null");
    }

    @Test
    @Order(3)
    public void checkSuccessWhileSavingProductsWithAdminAuthorization() {
        // Кладём токен доступа админа в куки в заголовки запроса
        headers.add(HttpHeaders.COOKIE, "Access-Token=" + adminAccessToken);

        HttpEntity<Product> request = new HttpEntity<>(testProduct, headers);

        ResponseEntity<Product> response = restTemplate.exchange(
                "/products", HttpMethod.POST, request, Product.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Unexpected HTTP status");

        Product savedProduct = response.getBody();

        assertNotNull(savedProduct, "Saved  product should not be null");
        assertNotNull(savedProduct.getId(), "Saved  product ID should not be null");
        assertEquals(testProduct.getTitle(), savedProduct.getTitle(), "Saved  product has incorrect title");
        assertEquals(testProduct.getPrice(), savedProduct.getPrice(), "Saved  product has incorrect price");

        userRepository.delete(admin);
        productRepository.delete(savedProduct);
    }

    @Test
    @Order(4)
    public void checkSuccessWhileGettingByIdProductWithAdminAuthorization() {

        headers.add(HttpHeaders.COOKIE, "Access-Token=" + adminAccessToken);

        Product savedTestProduct = productRepository.save(testProduct);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Product> response = restTemplate.exchange(
                "/products/{id}", HttpMethod.GET, request, Product.class, testProduct.getId()
        );

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Unexpected HTTP status");

        Product retrievedProduct = response.getBody();

        assertNotNull(retrievedProduct, "Saved  product should not be null");
        assertNotNull(retrievedProduct.getId(), "Saved  product ID should not be null");
        assertEquals(savedTestProduct.getId(), retrievedProduct.getId(), "We have no product with such ID");

        userRepository.delete(admin);
        productRepository.delete(retrievedProduct);
    }

    @Test
    @Order(5)
    public void checkForbiddenStatusWhileGettingProductByIdWithoutAuthorization() {

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Product> response = restTemplate.exchange(
                "/products/{id}", HttpMethod.GET, request, Product.class, testProduct.getId()
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(), "Unexpected HTTP status");
        assertNull(response.getBody(), "Response body should be null");
    }


    private Product createTestProduct() {
        Product product = new Product();
        product.setTitle("test product");
        product.setPrice(new BigDecimal(777));
        return product;
    }

    private String generateAdminAccessToken() {
        return Jwts.builder()
                .subject(admin.getEmail())
                .expiration(new Date(System.currentTimeMillis() + 60 * 60 * 1000))
                .signWith(accessKey)
                .compact();
    }

    private User createAdmin() {
        String adminEmail = "admin@test.com";
        User admin = userRepository.findByEmail(adminEmail).orElse(null);

        if (admin == null) {
            admin = new User();
            admin.setEmail(adminEmail);
            admin.setName("Admin");
            admin.setRole(Role.ROLE_ADMIN);
            admin.setPassword("111");

            userRepository.save(admin);
        }

        return admin;
    }
}