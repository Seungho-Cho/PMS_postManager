package com.mason.api.menu;

import java.util.List;

import com.mason.api.menu.dto.CustomMenuItemBulkItem;
import com.mason.api.menu.entity.CustomMenuItem;
import com.mason.api.menu.entity.CustomMenuItem.MenuItemType;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 상단 "바로가기" 드롭다운 메뉴 항목({@link CustomMenuItem})에 대한 조회/생성/수정/삭제를 담당한다.
 */
@Service
@Transactional(readOnly = true)
public class CustomMenuItemService {

    private final CustomMenuItemRepository customMenuItemRepository;

    public CustomMenuItemService(CustomMenuItemRepository customMenuItemRepository) {
        this.customMenuItemRepository = customMenuItemRepository;
    }

    /** 관리 화면용 — 비활성 항목도 포함해 전부 조회한다. */
    public List<CustomMenuItem> findAll() {
        return customMenuItemRepository.findAllByOrderBySortOrderAsc();
    }

    /** 네비게이션 드롭다운용 — 활성화된 항목만 조회한다. */
    public List<CustomMenuItem> findAllEnabled() {
        return customMenuItemRepository.findAllByEnabledTrueOrderBySortOrderAsc();
    }

    public CustomMenuItem findById(Long id) {
        return customMenuItemRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(String.valueOf(id)));
    }

    @Transactional
    public void create(String label, MenuItemType type, String value, Integer sortOrder, boolean enabled) {
        customMenuItemRepository.save(new CustomMenuItem(nullToEmpty(label), type, nullToEmpty(value), sortOrder, enabled));
    }

    @Transactional
    public void update(Long id, String label, MenuItemType type, String value, Integer sortOrder, boolean enabled) {
        findById(id).update(nullToEmpty(label), type, nullToEmpty(value), sortOrder, enabled);
    }

    @Transactional
    public void delete(Long id) {
        customMenuItemRepository.deleteById(id);
    }

    /**
     * 화면에 보이는 순서 그대로 한 번에 저장한다. id가 있으면 수정, 없으면 신규 생성하고
     * 배열 내 위치를 sortOrder로 사용한다(드래그앤드롭으로 바뀐 순서 반영).
     */
    @Transactional
    public void replaceAll(List<CustomMenuItemBulkItem> items) {
        for (int i = 0; i < items.size(); i++) {
            CustomMenuItemBulkItem item = items.get(i);
            if (item.id() == null) {
                create(item.label(), item.type(), item.value(), i, item.enabled());
            } else {
                update(item.id(), item.label(), item.type(), item.value(), i, item.enabled());
            }
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}