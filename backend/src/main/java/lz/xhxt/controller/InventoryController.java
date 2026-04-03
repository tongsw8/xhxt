package lz.xhxt.controller;

import lz.xhxt.entity.ProductInfo;
import lz.xhxt.mapper.ProductInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/product")
public class InventoryController {

    @Autowired
    private ProductInfoMapper productMapper;

    @PostMapping("/stock")
    public void updateStock(@RequestParam Long id, @RequestParam Integer amount) {
        ProductInfo product = productMapper.selectById(id);
        if (product != null) {
            Integer stock = product.getStock() == null ? 0 : product.getStock();
            product.setStock(stock + amount);
            productMapper.updateById(product);
        }
    }
}