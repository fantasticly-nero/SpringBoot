package com.chengfei.buyee.admin.category;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.chengfei.buyee.common.entity.Category;
import com.chengfei.buyee.common.exception.CategoryNotFoundException;

import jakarta.transaction.Transactional;
@Service
@Transactional
public class CategoryService {
    @Autowired private CategoryRepository repo;
    // Create Tasks 
    public Category saveCategory(Category categoryInForm) {
	// Copy children from categoryInDB to categoryInForm
	Integer id = categoryInForm.getId();
	boolean isCreatingNew = id == null;
	Category categoryInDB = null;
	if (!isCreatingNew) categoryInDB = repo.findById(id).get();
	if (categoryInDB != null) categoryInForm.setChildren(categoryInDB.getChildren());
	// Save categoryInForm to database
	Category savedCategory = repo.save(categoryInForm);
	// Update properties linked with other Category objects
	CategoryServiceUtil.setSelfAndSubAllParentIds(savedCategory);
	CategoryServiceUtil.setSelfAndSubLevel(savedCategory);
	return savedCategory;
    }
    // Read Tasks
    public Category readCategoryById(Integer id) throws CategoryNotFoundException {
	try {
	    return repo.findById(id).get();
	} catch (NoSuchElementException e) {
	    throw new CategoryNotFoundException("Could not find any category with id " + id);
	}
    }
    public List<Category> readAllCategories() {
	return (List<Category>) repo.findAll();
    }
    public List<Category> readCategoriesInForm() {
	List<Category> categoriesInForm = new ArrayList<>();
	List<Category> rootCategoriesInDB = repo.readRootCategories(Sort.by("name").ascending());
	for (Category category: rootCategoriesInDB) readSubCategoriesInForm(categoriesInForm, category, 0);
	return categoriesInForm;
    }
    private void readSubCategoriesInForm(List<Category> categoriesInForm, Category category, int level) {
	if (category == null || category.getName() == null) return;
	String name = category.getName();
	for (int i = 0; i < level; i++) name = "····" + name;
	categoriesInForm.add(new Category(category.getId(), name));
	Set<Category> children = sortCategories(category.getChildren());
	for (Category subCategory: children) readSubCategoriesInForm(categoriesInForm, subCategory, level+1);
    }
    public List<Category> readCategoriesFullData() {
	return readCategoriesFullData(null, null);
    }
    public List<Category> readCategoriesFullData(String sortField, String sortOrder) {
	List<Category> categoriesFullData = new ArrayList<>();
	if (sortField == null || sortField.isEmpty()) sortField = "name";
	Sort sort = Sort.by(sortField);
	if (sortOrder == null || sortOrder.isEmpty() || sortOrder.equals("asc")) 
	    sort = sort.ascending(); 
	else sort = sort.descending();
	List<Category> roortCategoriesInDB = repo.readRootCategories(sort);
	for (Category category: roortCategoriesInDB)
	    readSubCategoriesFullData(categoriesFullData, category, 0, sortField, sortOrder);
	return categoriesFullData;
    }
    private void readSubCategoriesFullData(List<Category> categoriesFullData, Category category, 
	    				   int level, String sortField, String sortOrder) {
	if (category == null || category.getName() == null) return;
	String name = category.getName();
	for (int i = 0; i < level; i++) name = "····" + name;
	Category fullCopiedCategory = CategoryServiceUtil.fullCopyCategory(category);
	fullCopiedCategory.setName(name);
	categoriesFullData.add(fullCopiedCategory);
	Set<Category> children = sortCategories(fullCopiedCategory.getChildren(), sortField, sortOrder);
	for (Category subCategory: children)
	    readSubCategoriesFullData(categoriesFullData, subCategory, level+1, sortField, sortOrder);
    }
    public Page<Category> readCategoriesByPageNum(int pageNum, int usersPerPage, String sortField, String sortOrder, String keyword) {
	Pageable pageable = null;
	if (sortField != null && sortOrder != null) {
	    Sort sort = Sort.by(sortField);
	    sort = sortOrder.equals("asc") ? sort.ascending() : sort.descending();
	    pageable = PageRequest.of(pageNum - 1, usersPerPage, sort);
	} else {
	    pageable = PageRequest.of(pageNum - 1, usersPerPage);
	}
	if (keyword != null)
	    return repo.readCategoriesByKeyword(keyword.trim(), pageable);
	return repo.findAll(pageable);
    }
    // Update Tasks
    public void updateCategoryEnabledStatus(Integer id, boolean enabled) {
	repo.updateCategoryEnabledStatus(id, enabled);
    }
    // Delete Tasks
    public void deleteCategoryById(Integer id) throws CategoryNotFoundException {
	Category categoryInDB =  repo.findById(id).get();
	if (categoryInDB == null) throw new CategoryNotFoundException("Could not find any category with ID " + id + ".");
	for (Category subCategory: categoryInDB.getChildren()) {
	    subCategory.setParent(categoryInDB.getParent());
	    CategoryServiceUtil.setSelfAndSubAllParentIds(subCategory);
	    CategoryServiceUtil.setSelfAndSubLevel(subCategory);
	}
	repo.deleteById(id);
    }
    // Validate Tasks
    public String isNameAliasUnique(Integer id, String name, String alias) {
	Category categoryByName = repo.findByName(name);
	Category categoryByAlias = repo.findByAlias(alias);
	if (categoryByName == null && categoryByAlias == null) return "OK";
	boolean isCreatingNew = id == null;
	if (isCreatingNew) {
	    if (categoryByName != null) return "DuplicateName";
	    if (categoryByAlias != null) return "DuplicateAlias";
	}
	if (categoryByName != null && categoryByName.getId() != id) return "DuplicateName";
	if (categoryByAlias != null && categoryByAlias.getId() != id) return "DuplicateAlias";
	return "OK";
    }
    // Sort Tasks
    public Set<Category> sortCategories(Set<Category> subcategories) {
	return sortCategories(subcategories, "name", "asc");
    }
    public Set<Category> sortCategories(Set<Category> subcategories, String sortField, String sortOrder) {
	SortedSet<Category> sortedSubCategories = new TreeSet<>(new Comparator<Category>() {
	    @Override
	    public int compare(Category c1, Category c2) {
		if (sortField == null || sortField.isEmpty() || sortField.equals("name")) {
		    if (sortOrder == null || sortOrder.isEmpty() || sortOrder.equals("asc")) 
			return c1.getName().compareToIgnoreCase(c2.getName());
		    else return c2.getName().compareToIgnoreCase(c1.getName());
		} else {
		    if (sortOrder.equals("asc")) return c1.getName().compareToIgnoreCase(c2.getName());
		    else return c2.getName().compareToIgnoreCase(c1.getName());
		}
	    }
	});
	sortedSubCategories.addAll(subcategories);
	return sortedSubCategories;
    }
}
