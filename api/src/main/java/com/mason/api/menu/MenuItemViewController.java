package com.mason.api.menu;

import com.mason.api.menu.entity.CustomMenuItem;
import com.mason.api.menu.entity.CustomMenuItem.MenuItemType;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * "바로가기" 드롭다운에서 메뉴 항목 클릭 시 보여주는 공개 페이지.
 * HTML 타입은 value(업로드된 HTML)를 그대로 렌더링하고, URL 타입은 외부 연결 안내 페이지를 보여준다.
 */
@Controller
public class MenuItemViewController {

    private final CustomMenuItemService customMenuItemService;

    public MenuItemViewController(CustomMenuItemService customMenuItemService) {
        this.customMenuItemService = customMenuItemService;
    }

    @GetMapping("/menu/{id}")
    public String menuItem(@PathVariable Long id, Model model) {
        CustomMenuItem item = customMenuItemService.findById(id);
        model.addAttribute("label", item.getLabel());

        if (item.getType() == MenuItemType.HTML) {
            model.addAttribute("htmlContent", item.getValue());
            return "menu-item-html";
        }

        model.addAttribute("targetUrl", item.getValue());
        return "menu-item-url";
    }
}