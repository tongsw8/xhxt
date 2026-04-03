package lz.xhxt.controller;

import lz.xhxt.common.Result;
import lz.xhxt.dto.CategorySaveRequest;
import lz.xhxt.dto.ProductSaveRequest;
import lz.xhxt.service.ProductManageService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/admin/product")
public class ProductManageController {

    @Resource
    private ProductManageService productManageService;

    @GetMapping("/category/list")
    public Result listCategory(@RequestParam(required = false) String keyword) {
        return productManageService.listCategory(keyword);
    }

    @PostMapping("/category/save")
    public Result saveCategory(@RequestBody CategorySaveRequest req) {
        return productManageService.saveCategory(req);
    }

    @GetMapping("/category/product-count/{id}")
    public Result categoryProductCount(@PathVariable("id") Long id) {
        return productManageService.categoryProductCount(id);
    }

    @DeleteMapping("/category/delete/{id}")
    public Result deleteCategory(@PathVariable("id") Long id) {
        return productManageService.deleteCategory(id);
    }

    @GetMapping("/list")
    public Result listProduct(@RequestParam(required = false) String keyword,
                              @RequestParam(required = false) Long categoryId,
                              @RequestParam(required = false) Integer status,
                              @RequestParam(required = false, defaultValue = "1") Integer pageNo,
                              @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        return productManageService.listProduct(keyword, categoryId, status, pageNo, pageSize);
    }

    @PostMapping("/save")
    public Result saveProduct(@RequestBody ProductSaveRequest req) {
        return productManageService.saveProduct(req);
    }

    @PostMapping("/status")
    public Result changeStatus(@RequestParam Long id, @RequestParam Integer status) {
        return productManageService.changeProductStatus(id, status);
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteProduct(@PathVariable("id") Long id) {
        return productManageService.deleteProduct(id);
    }
}
