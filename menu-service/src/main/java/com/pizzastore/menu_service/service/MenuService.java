package com.pizzastore.menu_service.service;

import com.pizzastore.menu_service.dto.CategoryDto;
import com.pizzastore.menu_service.dto.MenuItemDto;
import com.pizzastore.menu_service.entity.Category;
import com.pizzastore.menu_service.entity.MenuItem;
import com.pizzastore.menu_service.exception.NotFoundException;
import com.pizzastore.menu_service.repository.CategoryRepository;
import com.pizzastore.menu_service.repository.MenuItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class MenuService {

    private static final Logger logger = LoggerFactory.getLogger(MenuService.class);

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // Category Management
    public CategoryDto createCategory(CategoryDto categoryDto) {
        logger.info("Creating category: {}", categoryDto.getName());

        if (categoryRepository.existsByName(categoryDto.getName())) {
            throw new IllegalArgumentException("Category already exists: " + categoryDto.getName());
        }

        Category category = new Category(categoryDto.getName(), categoryDto.getDescription());
        Category savedCategory = categoryRepository.save(category);

        logger.info("Category created successfully: {}", savedCategory.getName());
        return mapToCategoryDto(savedCategory);
    }

    public List<CategoryDto> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(this::mapToCategoryDto)
                .collect(Collectors.toList());
    }

    // Menu Item Management
    public MenuItemDto createMenuItem(MenuItemDto menuItemDto) {
        logger.info("Creating menu item: {}", menuItemDto.getName());

        Category category = categoryRepository.findById(menuItemDto.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found with ID: " + menuItemDto.getCategoryId()));

        MenuItem menuItem = new MenuItem();
        menuItem.setName(menuItemDto.getName());
        menuItem.setDescription(menuItemDto.getDescription());
        menuItem.setPrice(menuItemDto.getPrice());
        menuItem.setCategory(category);
        menuItem.setImageUrl(menuItemDto.getImageUrl());
        menuItem.setAvailable(menuItemDto.getAvailable() != null ? menuItemDto.getAvailable() : true);

        MenuItem savedItem = menuItemRepository.save(menuItem);

        logger.info("Menu item created successfully: {}", savedItem.getName());
        return mapToMenuItemDto(savedItem);
    }

    public MenuItemDto updateMenuItem(Long itemId, MenuItemDto menuItemDto) {
        logger.info("Updating menu item with ID: {}", itemId);

        MenuItem menuItem = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Menu item not found with ID: " + itemId));

        Category category = categoryRepository.findById(menuItemDto.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found with ID: " + menuItemDto.getCategoryId()));

        menuItem.setName(menuItemDto.getName());
        menuItem.setDescription(menuItemDto.getDescription());
        menuItem.setPrice(menuItemDto.getPrice());
        menuItem.setCategory(category);
        menuItem.setImageUrl(menuItemDto.getImageUrl());
        if (menuItemDto.getAvailable() != null) {
            menuItem.setAvailable(menuItemDto.getAvailable());
        }

        MenuItem savedItem = menuItemRepository.save(menuItem);

        logger.info("Menu item updated successfully: {}", savedItem.getName());
        return mapToMenuItemDto(savedItem);
    }

    public void deleteMenuItem(Long itemId) {
        logger.info("Deleting menu item with ID: {}", itemId);

        if (!menuItemRepository.existsById(itemId)) {
            throw new NotFoundException("Menu item not found with ID: " + itemId);
        }

        menuItemRepository.deleteById(itemId);
        logger.info("Menu item deleted successfully: {}", itemId);
    }

    public MenuItemDto getMenuItemById(Long itemId) {
        MenuItem menuItem = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Menu item not found with ID: " + itemId));

        return mapToMenuItemDto(menuItem);
    }

    public List<MenuItemDto> getAllMenuItems() {
        List<MenuItem> items = menuItemRepository.findAll();
        return items.stream()
                .map(this::mapToMenuItemDto)
                .collect(Collectors.toList());
    }

    // Public APIs
    public List<MenuItemDto> getAvailableItemsByCategory(String categoryName) {
        Category category = categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new NotFoundException("Category not found: " + categoryName));

        List<MenuItem> items = menuItemRepository.findByCategoryAndAvailableTrue(category);
        return items.stream()
                .map(this::mapToMenuItemDto)
                .collect(Collectors.toList());
    }

    public List<MenuItemDto> searchAvailableItems(String keyword) {
        List<MenuItem> items = menuItemRepository.searchAvailableItems(keyword);
        return items.stream()
                .map(this::mapToMenuItemDto)
                .collect(Collectors.toList());
    }

    public List<MenuItemDto> getAllAvailableItems() {
        List<MenuItem> items = menuItemRepository.findByAvailableTrue();
        return items.stream()
                .map(this::mapToMenuItemDto)
                .collect(Collectors.toList());
    }

    // Admin functionality
    public void toggleItemAvailability(Long itemId, Boolean available) {
        logger.info("Toggling availability for item ID: {} to {}", itemId, available);

        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Menu item not found with ID: " + itemId));

        item.setAvailable(available);
        menuItemRepository.save(item);

        logger.info("Item availability updated: {} - {}", item.getName(), available);
    }

    // Statistics
    public Map<String, Object> getMenuStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalItems", menuItemRepository.count());
        stats.put("availableItems", menuItemRepository.countByAvailableTrue());
        stats.put("totalCategories", categoryRepository.count());

        return stats;
    }

    // Helper methods
    private CategoryDto mapToCategoryDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setCategoryId(category.getCategoryId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        return dto;
    }

    private MenuItemDto mapToMenuItemDto(MenuItem item) {
        MenuItemDto dto = new MenuItemDto();
        dto.setItemId(item.getItemId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setPrice(item.getPrice());
        dto.setCategoryId(item.getCategory().getCategoryId());
        dto.setCategoryName(item.getCategory().getName());
        dto.setAvailable(item.getAvailable());
        dto.setImageUrl(item.getImageUrl());
        dto.setCreatedAt(item.getCreatedAt());
        dto.setUpdatedAt(item.getUpdatedAt());
        return dto;
    }
}
