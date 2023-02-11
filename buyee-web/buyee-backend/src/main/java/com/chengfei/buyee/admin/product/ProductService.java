package com.chengfei.buyee.admin.product;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.chengfei.buyee.common.entity.Product;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ProductService {
    public static final Integer PRODUCTS_PER_PAGE = 5;
    
    @Autowired 
    private ProductRepository repo;
    
    // Create Tasks
    
    public Product saveProduct(Product product) {
	if (product.getId() == null) {
	    product.setCreatedTime(new Date());
	}
	product.setUpdatedTime(new Date());
	if (product.getAlias() == null || product.getAlias().isEmpty()) {
	    String defaultAlias = product.getName().toLowerCase().replaceAll(" ", "_");
	    product.setAlias(defaultAlias);
	} else {
	    product.setAlias(product.getAlias().toLowerCase().replaceAll(" ", "_"));
	}
	return repo.save(product);
    }
    
    // Read Tasks
    
    public Page<Product> readProductsByPageNum(int pageNum, String sortField, String sortOrder, String keyword) {
	Pageable pageable = null;
	if (sortField != null && sortOrder != null) {
	    Sort sort = Sort.by(sortField);
	    sort = sortOrder.equals("asc") ? sort.ascending() : sort.descending();
	    pageable = PageRequest.of(pageNum - 1, PRODUCTS_PER_PAGE, sort);
	} else {
	    pageable = PageRequest.of(pageNum - 1, PRODUCTS_PER_PAGE);
	}
	if (keyword != null)
	    return repo.readProductsByKeyword(keyword.trim(), pageable);
	return repo.findAll(pageable);
    }
    
    public List<Product> readAllProducts() {
	return (List<Product>) repo.findAll();
    }
    
    // Update Tasks
    
    public void updateProductEnabledStatus(Integer id, boolean enabled) {
	repo.updateProductEnabledStatus(id, enabled);
    }

    // Delete Tasks

    public void deleteProductById(Integer id) throws ProductNotFoundException {
	Long countById = repo.countProductById(id);
	if (countById == null || countById == 0) {
	    throw new ProductNotFoundException("Could not find any product with ID " + id + ".");
	}
	repo.deleteById(id);
    }
    
    // Validate Tasks
    
    public boolean isNameUnique(Integer id, String name) {
	Product product = repo.findByName(name);
	if (product == null) return true;
	boolean isCreatingNew = id == null;
	if (isCreatingNew) return false;
	if (product.getId() == id) return true;
	return false;
    }
}
