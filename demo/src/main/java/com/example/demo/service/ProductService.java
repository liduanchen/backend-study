package com.example.demo.service;

import com.example.demo.entity.ProductEntity;
import com.example.demo.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductMapper productMapper;

    /**
     * 查询所有商品
     */
    public List<ProductEntity> getAllProducts() {
        return productMapper.selectAll();
    }

    /**
     * 根据ID查询商品
     */
    public ProductEntity getProductById(Long id) {
        return productMapper.selectById(id);
    }

    /**
     * 根据名称查询商品
     */
    public ProductEntity getByName(String name) {
        return productMapper.selectByName(name);
    }

    /**
     * 保存商品（新增或更新）
     */
    @Transactional
    public ProductEntity saveProduct(ProductEntity product) {
        if (product.getId() == null) {
            // 新增
            productMapper.insert(product);
        } else {
            // 更新
            productMapper.update(product);
        }
        return product;
    }

    /**
     * 删除商品
     * @return 删除的记录数，0表示未找到要删除的记录
     */
    @Transactional
    public int deleteProduct(Long id) {
        return productMapper.deleteById(id);
    }
}
