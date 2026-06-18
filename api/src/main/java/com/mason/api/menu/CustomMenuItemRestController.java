package com.mason.api.menu;

import java.util.List;

import com.mason.api.menu.dto.CustomMenuItemBulkItem;
import com.mason.api.menu.dto.CustomMenuItemResponse;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/custom-menu-items")
public class CustomMenuItemRestController {

    private final CustomMenuItemService customMenuItemService;

    public CustomMenuItemRestController(CustomMenuItemService customMenuItemService) {
        this.customMenuItemService = customMenuItemService;
    }

    @GetMapping
    public List<CustomMenuItemResponse> findAll() {
        return customMenuItemService.findAll().stream()
            .map(CustomMenuItemResponse::from)
            .toList();
    }

    /** 화면에 보이는 순서/내용을 한 번에 저장한다(드래그앤드롭으로 바뀐 순서 포함). */
    @PutMapping
    public ResponseEntity<Void> replaceAll(@Valid @RequestBody List<CustomMenuItemBulkItem> items) {
        customMenuItemService.replaceAll(items);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customMenuItemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}