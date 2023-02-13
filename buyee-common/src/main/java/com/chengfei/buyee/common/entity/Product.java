package com.chengfei.buyee.common.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.chengfei.buyee.common.Constants;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product {
    // Default Properties
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "created_time")
    private Date createdTime;
    @Column(name = "updated_time")
    private Date updatedTime;
    
    // Overview Section
    @Column(length = 256, nullable = false, unique = true)
    private String name;
    @Column(length = 256, nullable = false, unique = true)
    private String alias;
    @ManyToOne
    @JoinColumn(name = "brand_id")
    private Brand brand;
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    private boolean enabled;
    @Column(name = "in_stock")
    private boolean inStock;
    private float cost;
    private float price;
    @Column(name = "discount_percent")
    private float discountPercent;

    // Description Section
    @Column(name = "short_description",length = 512, nullable = false)
    private String shortDescription;
    @Column(name = "full_description", length = 4096, nullable = false)
    private String fullDescription;
    
    // Images Section
    @Column(name = "main_image")
    private String mainImage;
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductImage> images = new HashSet<>();
    
    // Details Section 
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductDetail> details = new ArrayList<>();
    
    // Shipping Section
    private float length;
    private float width;
    private float height;
    private float weight;

    // Generators
    public Product() {}
     
    // Default Properties
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public Date getCreatedTime() {
	return createdTime;
    }
    public void setCreatedTime(Date createdTime) {
	this.createdTime = createdTime;
    }
    public Date getUpdatedTime() {
	return updatedTime;
    }
    public void setUpdatedTime(Date updatedTime) {
	this.updatedTime = updatedTime;
    }
    
    // Overview Section
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getAlias() {
        return alias;
    }
    public void setAlias(String alias) {
        this.alias = alias;
    }
    public Brand getBrand() {
	return brand;
    }
    public void setBrand(Brand brand) {
	this.brand = brand;
    }
    public Category getCategory() {
	return category;
    }
    public void setCategory(Category category) {
	this.category = category;
    }
    public boolean isEnabled() {
	return enabled;
    }
    public void setEnabled(boolean enabled) {
	this.enabled = enabled;
    }
    public boolean isInStock() {
	return inStock;
    }
    public void setInStock(boolean inStock) {
	this.inStock = inStock;
    }
    public float getCost() {
	return cost;
    }
    public void setCost(float cost) {
	this.cost = cost;
    }
    public float getPrice() {
	return price;
    }
    public void setPrice(float price) {
	this.price = price;
    }
    public float getDiscountPercent() {
	return discountPercent;
    }
    public void setDiscountPercent(float discountPercent) {
	this.discountPercent = discountPercent;
    }
    
    // Description Section
    public String getShortDescription() {
        return shortDescription;
    }
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }
    public String getFullDescription() {
        return fullDescription;
    }
    public void setFullDescription(String fullDescription) {
        this.fullDescription = fullDescription;
    }
    
    // Images Section
    // Getters and Setters
    public String getMainImage() {
	return mainImage;
    }
    public void setMainImage(String mainImage) {
	this.mainImage = mainImage;
    }
    public Set<ProductImage> getImages() {
        return images;
    }
    public void setImages(Set<ProductImage> images) {
        this.images = images;
    }
    // Adder
    public void addExtraImage(String imageName) {
	this.images.add(new ProductImage(imageName, this)); 
    }
    // Generator 
    public List<ProductImage> getSortedImages() {
	List<ProductImage> sortedImages = new ArrayList<>(this.images);
	sortedImages.sort((image1, image2) -> {
	    return image1.getName().compareToIgnoreCase(image2.getName());
	});
	return sortedImages;
    }
    public String getMainImagePathString() {
	if (id == null || mainImage == null) return Constants.S3_BASE_URI + "/product-images/default-image.png";
	return Constants.S3_BASE_URI + "/product-images/" + this.getId() + "/" + this.getMainImage();
    }
    public String getDefaultImagePathString() {
	return Constants.S3_BASE_URI + "/product-images/default-image.png";
    }
    // Validator
    public boolean containExtraImageName(String imageName) {
	Iterator<ProductImage> iterator = this.images.iterator();
	while (iterator.hasNext()) {
	    if (iterator.next().getName().equals(imageName))
		return true;
	}
	return false;
    }
    
    // Details Section
    public List<ProductDetail> getDetails() {
        return details;
    }
    public void setDetails(List<ProductDetail> details) {
        this.details = details;
    }
    public void addDetail(String name, String value) {
	this.details.add(new ProductDetail(name, value, this));
    }

    // Shipping Section
    public float getLength() {
	return length;
    }
    public void setLength(float length) {
	this.length = length;
    }
    public float getWidth() {
	return width;
    }
    public void setWidth(float width) {
	this.width = width;
    }
    public float getHeight() {
	return height;
    }
    public void setHeight(float height) {
	this.height = height;
    }
    public float getWeight() {
	return weight;
    }
    public void setWeight(float weight) {
	this.weight = weight;
    }

    @Override
    public String toString() {
	return "Product [id=" + id + ", name=" + name + "]";
    }
    
    // Builder
    private Product(ProductBuilder builder) {
	this.id = builder.id;
	this.name = builder.name;
	this.alias = builder.alias;
	this.shortDescription = builder.shortDescription;
	this.fullDescription = builder.fullDescription;
	this.createdTime = builder.createdTime;
	this.updatedTime = builder.updatedTime;
	this.enabled = builder.enabled;
	this.inStock = builder.inStock;
	this.cost = builder.cost;
	this.price = builder.price;
	this.discountPercent = builder.discountPercent;
	this.length = builder.length;
	this.width = builder.width;
	this.height = builder.height;
	this.weight = builder.weight;
	this.category = builder.category;
	this.brand = builder.brand;
    }
    public static class ProductBuilder {
	 private Integer id;
	 private String name;
	 private String alias;
	 private String shortDescription;
	 private String fullDescription;
	 private Date createdTime;
	 private Date updatedTime;
	 private boolean enabled;
	 private boolean inStock;
	 private float cost;
	 private float price;
	 private float discountPercent;
	 private float length;
	 private float width;
	 private float height;
	 private float weight;
	 private Category category;
	 private Brand brand;
	 
	 public ProductBuilder(String name, String alias, String shortDescription, String fullDescription) {
	     this.name = name;
	     this.alias = alias;
	     this.shortDescription = shortDescription; 
	     this.fullDescription = fullDescription; 
	 }
	 
	 public ProductBuilder setId(Integer id) {
	     this.id = id;
	     return this;
	 }
	 public ProductBuilder setCreatedTime(Date createdTime) {
	     this.createdTime = createdTime;
	     return this;
	 }
	 public ProductBuilder setUpdatedTime(Date updatedTime) {
	     this.updatedTime = updatedTime;
	     return this;
	 }
	 public ProductBuilder setEnabled(boolean enabled) {
	     this.enabled = enabled;
	     return this;
	 }
	 public ProductBuilder setInStock(boolean inStock) {
	     this.inStock = inStock;
	     return this;
	 }
	 public ProductBuilder setCost(float cost) {
	     this.cost = cost;
	     return this;
	 }
	 public ProductBuilder setPrice(float price) {
	     this.price = price;
	     return this;
	 }
	 public ProductBuilder setDiscountPercent(float discountPercent) {
	     this.discountPercent = discountPercent;
	     return this;
	 }
	 public ProductBuilder setLength(float length) {
	     this.length = length;
	     return this;
	 }
	 public ProductBuilder setWidth(float width) {
	     this.width = width;
	     return this;
	 }
	 public ProductBuilder setHeight(float height) {
	     this.height = height;
	     return this;
	 }
	 public ProductBuilder setWeight(float weight) {
	     this.weight = weight;
	     return this;
	 }
	 public ProductBuilder setBrand(Brand brand) {
	     this.brand = brand;
	     return this;
	 }
	 public ProductBuilder setCategory(Category category) {
	     this.category = category;
	     return this;
	 }
	 public Product build() {
	     return new Product(this);
	 }
    }
}