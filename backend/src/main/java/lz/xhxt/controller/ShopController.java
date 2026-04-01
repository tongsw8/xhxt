package lz.xhxt.controller;

import lz.xhxt.common.Result;
import lz.xhxt.service.ProductManageService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/shop")
public class ShopController {

    @Resource
    private ProductManageService productManageService;

    @GetMapping("/category/list")
    public Result listCategory() {
        return productManageService.listCategory(null);
    }

    @GetMapping("/product/list")
    public Result listProduct(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(required = false, defaultValue = "12") Integer pageSize) {
        return productManageService.listProduct(keyword, categoryId, 1, pageNo, pageSize);
    }
}
