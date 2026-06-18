package com.mason.api.menu;

import java.util.List;

import com.mason.api.menu.entity.CustomMenuItem;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomMenuItemRepository extends JpaRepository<CustomMenuItem, Long> {

    List<CustomMenuItem> findAllByOrderBySortOrderAsc();

    List<CustomMenuItem> findAllByEnabledTrueOrderBySortOrderAsc();
}