package com.java.codeworld;

import com.java.codeworld.entity.Product;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SimpleCrudExampleApplicationTests {

    @LocalServerPort
    private int port;

    private String baseUrl = "http://localhost";

    private static RestTemplate restTemplate;

    @Autowired
    private TestH2Repository testH2Repository;

    @BeforeAll
    public static void init() {
        restTemplate = new RestTemplate();
    }

    @BeforeEach
    public void setUp() {
        baseUrl = baseUrl.concat(":").concat(port + "").concat("/products");
    }

    @Test
    public void testAddProduct() {
        Product product = new Product("headset", 2, 7000);
        Product response = restTemplate.postForObject(baseUrl, product, Product.class);

        assertEquals("headset", response.getName());
        assertEquals(1, testH2Repository.findAll().size());
    }

    @Test
    @Sql(statements = "INSERT INTO PRODUCT_TBL (id, name, quantity, price) VALUES (4, 'AC', 1, 12500)", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "DELETE FROM PRODUCT_TBL WHERE name = 'AC'", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testGetProducts() {

        List<Product> products = restTemplate.getForObject(baseUrl, List.class);

        assertEquals(1, products.size());
        assertEquals(1, testH2Repository.findAll().size());

    }

    @Test
    @Sql(statements = "INSERT INTO PRODUCT_TBL (id, name, quantity, price) VALUES (5, 'CAR', 1, 400000)", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "DELETE FROM PRODUCT_TBL WHERE id = 5", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testFindProductById() {

        Product product = restTemplate.getForObject(baseUrl + "/{id}", Product.class, 5);

        assertAll(
                () -> assertNotNull(product),
                () -> assertEquals(5, product.getId()),
                () -> assertEquals("CAR", product.getName())
        );

    }

    @Test
    @Sql(statements = "INSERT INTO PRODUCT_TBL (id, name, quantity, price) VALUES (6, 'SHOES', 1, 999)", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "DELETE FROM PRODUCT_TBL WHERE id = 6", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testUpdateProduct() {

        Product product = new Product("SHOES", 1, 1999);
        restTemplate.put(baseUrl + "/update/{id}", product, 6);
        Product productFromDB = testH2Repository.findById(6).get();
        assertAll(
                () -> assertNotNull(productFromDB),
                () -> assertEquals(1999, productFromDB.getPrice())
        );

    }

    @Test
    @Sql(statements = "INSERT INTO PRODUCT_TBL (id, name, quantity, price) VALUES (8, 'BOOK', 1, 100)", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void testDeleteProduct() {
        int recordCount = testH2Repository.findAll().size();
        assertTrue(recordCount > 0);
        restTemplate.delete(baseUrl + "/delete/{id}", 8);
        assertTrue(recordCount > testH2Repository.findAll().size());
    }


}
