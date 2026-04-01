package lz.xhxt.service;

import lz.xhxt.common.Result;
import lz.xhxt.dto.CategorySaveRequest;
import lz.xhxt.dto.ProductSaveRequest;

public interface ProductManageService {

    Result listCategory(String keyword);

    Result saveCategory(CategorySaveRequest req);

    Result deleteCategory(Long id);

    Result categoryProductCount(Long id);

    Result listProduct(String keyword, Long categoryId, Integer status, Integer pageNo, Integer pageSize);

    Result saveProduct(ProductSaveRequest req);

    Result changeProductStatus(Long id, Integer status);

    Result deleteProduct(Long id);
}
