package com.mason.api.menu.dto;

import com.mason.api.menu.entity.CustomMenuItem.MenuItemType;

import jakarta.validation.constraints.NotNull;

/**
 * 전체 저장(드래그앤드롭 순서 반영) 요청의 항목 하나.
 * id가 없으면 신규 생성, 있으면 해당 id를 수정한다. 배열 내 순서가 곧 sortOrder가 된다.
 */
public record CustomMenuItemBulkItem(
    Long id,
    String label,
    @NotNull MenuItemType type,
    String value,
    @NotNull Boolean enabled
) {
}