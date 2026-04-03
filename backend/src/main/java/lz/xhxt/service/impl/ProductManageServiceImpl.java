package lz.xhxt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lz.xhxt.common.Result;
import lz.xhxt.common.ResultCode;
import lz.xhxt.dto.CategorySaveRequest;
import lz.xhxt.dto.ProductSaveRequest;
import lz.xhxt.entity.ProductCategory;
import lz.xhxt.entity.ProductInfo;
import lz.xhxt.mapper.ProductCategoryMapper;
import lz.xhxt.mapper.ProductInfoMapper;
import lz.xhxt.service.ProductManageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductManageServiceImpl implements ProductManageService {

    @Resource
    private ProductCategoryMapper categoryMapper;

    @Resource
    private ProductInfoMapper productMapper;

    @Override
    public Result listCategory(String keyword) {
        LambdaQueryWrapper<ProductCategory> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            qw.like(ProductCategory::getCategoryName, keyword.trim());
        }
        qw.orderByAsc(ProductCategory::getSortNo).orderByDesc(ProductCategory::getId);
        return Result.ok(categoryMapper.selectList(qw));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result saveCategory(CategorySaveRequest req) {
        if (req == null || req.getCategoryName() == null || req.getCategoryName().trim().isEmpty()) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "分类名称不能为空");
        }
        String name = req.getCategoryName().trim();
        Integer cnt = categoryMapper.selectCount(new LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getCategoryName, name)
                .ne(req.getId() != null, ProductCategory::getId, req.getId()));
        if (cnt != null && cnt > 0) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "分类名称已存在");
        }

        Date now = new Date();
        ProductCategory c;
        if (req.getId() == null) {
            c = new ProductCategory();
            c.setCreateTime(now);
        } else {
            c = categoryMapper.selectById(req.getId());
            if (c == null) {
                return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "分类不存在");
            }
        }
        c.setCategoryName(name);
        c.setSortNo(req.getSortNo() == null ? 0 : req.getSortNo());
        c.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        c.setUpdateTime(now);

        if (req.getId() == null) categoryMapper.insert(c);
        else categoryMapper.updateById(c);

        return Result.ok(c);
    }

    @Override
    public Result categoryProductCount(Long id) {
        if (id == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "分类ID不能为空");
        Integer count = productMapper.selectCount(new LambdaQueryWrapper<ProductInfo>().eq(ProductInfo::getCategoryId, id));
        Map<String, Object> data = new HashMap<>();
        data.put("count", count == null ? 0 : count);
        return Result.ok(data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteCategory(Long id) {
        if (id == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "分类ID不能为空");
        Integer used = productMapper.selectCount(new LambdaQueryWrapper<ProductInfo>().eq(ProductInfo::getCategoryId, id));
        if (used != null && used > 0) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "该分类下有商品，不能删除");
        }
        categoryMapper.deleteById(id);
        return Result.ok(null);
    }

    @Override
    public Result listProduct(String keyword, Long categoryId, Integer status, Integer pageNo, Integer pageSize) {
        int pNo = (pageNo == null || pageNo < 1) ? 1 : pageNo;
        int pSize = (pageSize == null || pageSize < 1) ? 10 : Math.min(pageSize, 100);

        LambdaQueryWrapper<ProductInfo> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            qw.like(ProductInfo::getProductName, keyword.trim());
        }
        if (categoryId != null) qw.eq(ProductInfo::getCategoryId, categoryId);
        if (status != null) qw.eq(ProductInfo::getStatus, status);
        qw.orderByDesc(ProductInfo::getId);

        Page<ProductInfo> page = new Page<>(pNo, pSize);
        Page<ProductInfo> resultPage = productMapper.selectPage(page, qw);

        List<ProductCategory> categories = categoryMapper.selectList(new LambdaQueryWrapper<ProductCategory>()
                .orderByAsc(ProductCategory::getSortNo)
                .orderByDesc(ProductCategory::getId));
        Map<Long, String> categoryMap = new HashMap<>();
        for (ProductCategory c : categories) {
            categoryMap.put(c.getId(), c.getCategoryName());
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (ProductInfo p : resultPage.getRecords()) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", p.getId());
            row.put("categoryId", p.getCategoryId());
            row.put("categoryName", categoryMap.get(p.getCategoryId()));
            row.put("productName", p.getProductName());
            row.put("price", p.getPrice());
            row.put("stock", p.getStock());
            row.put("coverImg", p.getCoverImg());
            row.put("detailText", extractDetailText(p.getDetailText()));
            row.put("detailImgs", parseDetailImgs(p.getDetailText()));
            row.put("status", p.getStatus());
            row.put("createTime", p.getCreateTime());
            row.put("updateTime", p.getUpdateTime());
            rows.add(row);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("records", rows);
        data.put("total", resultPage.getTotal());
        data.put("pageNo", pNo);
        data.put("pageSize", pSize);
        return Result.ok(data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result saveProduct(ProductSaveRequest req) {
        if (req == null || req.getCategoryId() == null || req.getProductName() == null || req.getProductName().trim().isEmpty()) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "分类和商品名称不能为空");
        }
        if (req.getPrice() == null) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "价格不能为空");
        }

        ProductCategory category = categoryMapper.selectById(req.getCategoryId());
        if (category == null) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "分类不存在");
        }

        Date now = new Date();
        ProductInfo p;
        if (req.getId() == null) {
            p = new ProductInfo();
            p.setCreateTime(now);
        } else {
            p = productMapper.selectById(req.getId());
            if (p == null) {
                return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "商品不存在");
            }
        }

        p.setCategoryId(req.getCategoryId());
        p.setProductName(req.getProductName().trim());
        p.setPrice(req.getPrice());
        p.setStock(req.getStock() == null ? 0 : req.getStock());
        p.setCoverImg(req.getCoverImg());
        p.setDetailText(mergeDetailText(req.getDetailText(), req.getDetailImgs()));
        p.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        p.setUpdateTime(now);

        if (req.getId() == null) productMapper.insert(p);
        else productMapper.updateById(p);

        return Result.ok(p);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result changeProductStatus(Long id, Integer status) {
        if (id == null || status == null) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "参数不完整");
        }
        ProductInfo p = productMapper.selectById(id);
        if (p == null) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "商品不存在");
        }
        p.setStatus(status);
        p.setUpdateTime(new Date());
        productMapper.updateById(p);
        return Result.ok(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteProduct(Long id) {
        if (id == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "商品ID不能为空");
        productMapper.deleteById(id);
        return Result.ok(null);
    }

    private String mergeDetailText(String detailText, List<String> detailImgs) {
        String text = detailText == null ? "" : detailText;
        StringBuilder sb = new StringBuilder();
        for (String img : detailImgs == null ? new ArrayList<String>() : detailImgs) {
            if (img != null && !img.trim().isEmpty()) {
                if (sb.length() > 0) sb.append(',');
                sb.append(img.trim());
            }
        }
        return text + "\n[IMGS]" + sb;
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