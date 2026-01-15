package com.example.demo.mapper;

import com.example.demo.entity.ProductEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProductMapper {

    /**
     * 查询所有商品
     */
    List<ProductEntity> selectAll();

    /**
     * 根据ID查询商品
     */
    ProductEntity selectById(Long id);

    /**
     * 根据名称查询商品
     */
    ProductEntity selectByName(String name);

    /**
     * 插入商品
     */
    int insert(ProductEntity product);

    /**
     * 更新商品
     */
    int update(ProductEntity product);

    /**
     * 根据ID删除商品
     */
    int deleteById(Long id);
}