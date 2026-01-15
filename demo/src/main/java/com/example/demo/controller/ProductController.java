package com.example.demo.controller;

import com.example.demo.entity.ProductEntity;
import com.example.demo.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    /**
     * 查询所有商品
     */
    @GetMapping("/list")
    public ResponseEntity<List<ProductEntity>> getAllProducts() {
        List<ProductEntity> products = service.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * 查询单个商品（根据ID）
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductEntity> getProductById(@PathVariable Long id) {
        ProductEntity product = service.getProductById(id);
        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(product);
    }

    /**
     * 根据名称查询商品价格
     */
    @GetMapping("/search")
    public ResponseEntity<ProductEntity> getProductByName(@RequestParam String name) {
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        ProductEntity product = service.getByName(name.trim());
        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(product);
    }

    /**
     * 新增商品
     */
    @PostMapping
    public ResponseEntity<ProductEntity> addProduct(@RequestBody ProductEntity product) {
        ProductEntity savedProduct = service.saveProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    /**
     * 更新商品
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductEntity> updateProduct(@PathVariable Long id, @RequestBody ProductEntity product) {
        // 检查商品是否存在
        ProductEntity existingProduct = service.getProductById(id);
        if (existingProduct == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        product.setId(id);
        ProductEntity updatedProduct = service.saveProduct(product);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        // 检查商品是否存在
        ProductEntity existingProduct = service.getProductById(id);
        if (existingProduct == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        // 执行删除，返回删除的记录数
        int deletedCount = service.deleteProduct(id);
        if (deletedCount > 0) {
            return ResponseEntity.noContent().build();
        } else {
            // 删除失败（理论上不应该发生，因为已经检查过存在性）
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
