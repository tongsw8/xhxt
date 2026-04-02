package lz.xhxt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lz.xhxt.common.Result;
import lz.xhxt.entity.ProductInfo;
import lz.xhxt.mapper.ProductInfoMapper;
import lz.xhxt.service.ProductManageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shop")
public class ShopController {

    @Resource
    private ProductManageService productManageService;

    @Resource
    private ProductInfoMapper productInfoMapper;

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

    @GetMapping("/product/detail/{id}")
    public Result detail(@PathVariable Long id) {
        ProductInfo p = productInfoMapper.selectOne(new LambdaQueryWrapper<ProductInfo>()
                .eq(ProductInfo::getId, id)
                .eq(ProductInfo::getStatus, 1));
        if (p == null) return Result.ok(null);

        Map<String, Object> data = new HashMap<>();
        data.put("id", p.getId());
        data.put("categoryId", p.getCategoryId());
        data.put("productName", p.getProductName());
        data.put("price", p.getPrice());
        data.put("stock", p.getStock());
        data.put("coverImg", p.getCoverImg());
        data.put("detailText", extractDetailText(p.getDetailText()));
        data.put("detailImgs", parseDetailImgs(p.getDetailText()));
        data.put("status", p.getStatus());
        return Result.ok(data);
    }

    private String extractDetailText(String detailText) {
        if (detailText == null) return "";
        int idx = detailText.indexOf("\n[IMGS]");
        return idx < 0 ? detailText : detailText.substring(0, idx);
    }

    private List<String> parseDetailImgs(String detailText) {
        List<String> list = new ArrayList<>();
        if (detailText == null) return list;
        int idx = detailText.indexOf("\n[IMGS]");
        if (idx < 0) return list;
        String imgs = detailText.substring(idx + 7);
        if (imgs.trim().isEmpty()) return list;
        String[] arr = imgs.split(",");
        for (String s : arr) {
            if (s != null && !s.trim().isEmpty()) list.add(s.trim());
        }
        return list;
    }
}