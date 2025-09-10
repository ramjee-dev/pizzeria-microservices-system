package com.pizzastore.menu_service.controller;

import com.pizzastore.menu_service.dto.CategoryDto;
import com.pizzastore.menu_service.dto.MenuItemDto;
import com.pizzastore.menu_service.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/menu")
@Tag(name = "Menu Management", description = "Operations for managing pizzeria menu items and categories")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MenuController {

    private static final Logger logger = LoggerFactory.getLogger(MenuController.class);

    @Autowired
    private MenuService menuService;

    // Public APIs (No authentication required)

    @GetMapping("/categories")
    @Operation(summary = "Get all categories", description = "Retrieve all menu categories")
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        logger.info("GET /api/menu/categories - Getting all categories");

        List<CategoryDto> categories = menuService.getAllCategories();

        return ResponseEntity.ok(categories);
    }

    @GetMapping("/items")
    @Operation(summary = "Get all available menu items", description = "Retrieve all available menu items")
    public ResponseEntity<List<MenuItemDto>> getAllAvailableItems() {
        logger.info("GET /api/menu/items - Getting all available items");

        List<MenuItemDto> items = menuService.getAllAvailableItems();

        return ResponseEntity.ok(items);
    }

    @GetMapping("/category/{categoryName}")
    @Operation(summary = "Get items by category", description = "Retrieve available menu items by category name")
    public ResponseEntity<List<MenuItemDto>> getItemsByCategory(
            @PathVariable @Parameter(description = "Category name") String categoryName) {

        logger.info("GET /api/menu/category/{} - Getting items by category", categoryName);

        List<MenuItemDto> items = menuService.getAvailableItemsByCategory(categoryName);

        return ResponseEntity.ok(items);
    }

    @GetMapping("/search")
    @Operation(summary = "Search menu items", description = "Search available menu items by keyword")
    public ResponseEntity<List<MenuItemDto>> searchItems(
            @RequestParam @Parameter(description = "Search keyword") String keyword) {

        logger.info("GET /api/menu/search - Searching items with keyword: {}", keyword);

        List<MenuItemDto> items = menuService.searchAvailableItems(keyword);

        return ResponseEntity.ok(items);
    }

    // Admin APIs (Would require authentication in full implementation)

    @PostMapping("/admin/categories")
    @Operation(summary = "Create category (Admin)", description = "Create a new menu category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Category created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or category already exists")
    })
    public ResponseEntity<CategoryDto> createCategory(
            @Valid @RequestBody CategoryDto categoryDto) {

        logger.info("POST /api/menu/admin/categories - Creating category: {}", categoryDto.getName());

        CategoryDto createdCategory = menuService.createCategory(categoryDto);

        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    @PostMapping("/admin/items")
    @Operation(summary = "Create menu item (Admin)", description = "Create a new menu item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Menu item created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<MenuItemDto> createMenuItem(
            @Valid @RequestBody MenuItemDto menuItemDto) {

        logger.info("POST /api/menu/admin/items - Creating menu item: {}", menuItemDto.getName());

        MenuItemDto createdItem = menuService.createMenuItem(menuItemDto);

        return new ResponseEntity<>(createdItem, HttpStatus.CREATED);
    }

    @PutMapping("/admin/items/{itemId}")
    @Operation(summary = "Update menu item (Admin)", description = "Update an existing menu item")
    public ResponseEntity<MenuItemDto> updateMenuItem(
            @PathVariable Long itemId,
            @Valid @RequestBody MenuItemDto menuItemDto) {

        logger.info("PUT /api/menu/admin/items/{} - Updating menu item", itemId);

        MenuItemDto updatedItem = menuService.updateMenuItem(itemId, menuItemDto);

        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/admin/items/{itemId}")
    @Operation(summary = "Delete menu item (Admin)", description = "Delete a menu item")
    public ResponseEntity<String> deleteMenuItem(@PathVariable Long itemId) {

        logger.info("DELETE /api/menu/admin/items/{} - Deleting menu item", itemId);

        menuService.deleteMenuItem(itemId);

        return ResponseEntity.ok("Menu item deleted successfully");
    }

    @GetMapping("/admin/items")
    @Operation(summary = "Get all menu items (Admin)", description = "Retrieve all menu items including unavailable ones")
    public ResponseEntity<List<MenuItemDto>> getAllItems() {
        logger.info("GET /api/menu/admin/items - Getting all items for admin");

        List<MenuItemDto> items = menuService.getAllMenuItems();

        return ResponseEntity.ok(items);
    }

    @PatchMapping("/admin/items/{itemId}/availability")
    @Operation(summary = "Toggle item availability (Admin)", description = "Toggle menu item availability")
    public ResponseEntity<String> toggleAvailability(
            @PathVariable Long itemId,
            @RequestParam Boolean available) {

        logger.info("PATCH /api/menu/admin/items/{}/availability - Toggling to {}", itemId, available);

        menuService.toggleItemAvailability(itemId, available);

        return ResponseEntity.ok("Item availability updated successfully");
    }

    @GetMapping("/admin/statistics")
    @Operation(summary = "Get menu statistics (Admin)", description = "Get menu statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        logger.info("GET /api/menu/admin/statistics - Getting menu statistics");

        Map<String, Object> stats = menuService.getMenuStatistics();

        return ResponseEntity.ok(stats);
    }
}