package com.mason.api.menu.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상단 네비게이션 "바로가기" 드롭다운에 노출되는 커스텀 메뉴 항목.
 * type=URL이면 클릭 시 외부 연결 안내 페이지를 거쳐 value(링크)로 이동하고,
 * type=HTML이면 value를 그대로 페이지 본문에 렌더링한다.
 * type=DIVIDER는 클릭 불가능한 구분선으로, label/value는 빈 문자열로 둔다.
 * enabled=false면 드롭다운에는 노출되지 않지만 관리 화면에서는 계속 보인다(삭제 없이 임시로 숨기는 용도).
 */
@Entity
@Table(name = "custom_menu_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomMenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MenuItemType type;

    @Column(nullable = false, columnDefinition = "text")
    private String value;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public CustomMenuItem(String label, MenuItemType type, String value, Integer sortOrder, boolean enabled) {
        this.label = label;
        this.type = type;
        this.value = value;
        this.sortOrder = sortOrder;
        this.enabled = enabled;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void update(String label, MenuItemType type, String value, Integer sortOrder, boolean enabled) {
        this.label = label;
        this.type = type;
        this.value = value;
        this.sortOrder = sortOrder;
        this.enabled = enabled;
        this.updatedAt = LocalDateTime.now();
    }

    public enum MenuItemType {
        URL, HTML, DIVIDER
    }
}