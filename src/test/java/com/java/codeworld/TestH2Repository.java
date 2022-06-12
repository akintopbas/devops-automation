package com.java.codeworld;

import com.java.codeworld.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestH2Repository extends JpaRepository<Product, Integer> {
}
