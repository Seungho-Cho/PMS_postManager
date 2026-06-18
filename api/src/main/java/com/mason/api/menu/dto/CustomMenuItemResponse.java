package com.mason.api.menu.dto;

import com.mason.api.menu.entity.CustomMenuItem;
import com.mason.api.menu.entity.CustomMenuItem.MenuItemType;

public record CustomMenuItemResponse(
    Long id,
    String label,
    MenuItemType type,
    String value,
    Integer sortOrder,
    boolean enabled
) {
    public static CustomMenuItemResponse from(CustomMenuItem item) {
        return new CustomMenuItemResponse(
            item.getId(),
            item.getLabel(),
            item.getType(),
            item.getValue(),
            item.getSortOrder(),
            item.isEnabled()
        );
    }
}