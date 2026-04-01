package lz.xhxt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lz.xhxt.common.Result;
import lz.xhxt.common.ResultCode;
import lz.xhxt.entity.ProductInfo;
import lz.xhxt.entity.UserCart;
import lz.xhxt.entity.UserFavorite;
import lz.xhxt.mapper.ProductInfoMapper;
import lz.xhxt.mapper.UserCartMapper;
import lz.xhxt.mapper.UserFavoriteMapper;
import lz.xhxt.service.CartFavoriteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartFavoriteServiceImpl implements CartFavoriteService {

    @Resource
    private UserCartMapper cartMapper;

    @Resource
    private UserFavoriteMapper favoriteMapper;

    @Resource
    private ProductInfoMapper productMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result addToCart(Long userId, Long productId, Integer quantity) {
        if (userId == null || productId == null || quantity == null || quantity < 1) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "参数不完整");
        }
        ProductInfo product = productMapper.selectById(productId);
        if (product == null || product.getStatus() != 1) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "商品不存在或已下架");
        }
        UserCart existing = cartMapper.selectOne(new LambdaQueryWrapper<UserCart>()
                .eq(UserCart::getUserId, userId)
                .eq(UserCart::getProductId, productId));
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            cartMapper.updateById(existing);
        } else {
            UserCart cart = new UserCart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setQuantity(quantity);
            cartMapper.insert(cart);
        }
        return Result.ok(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateCartQuantity(Long userId, Long productId, Integer quantity) {
        if (userId == null || productId == null || quantity == null) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "参数不完整");
        }
        if (quantity < 1) {
            return removeFromCart(userId, productId);
        }
        UserCart cart = cartMapper.selectOne(new LambdaQueryWrapper<UserCart>()
                .eq(UserCart::getUserId, userId)
                .eq(UserCart::getProductId, productId));
        if (cart == null) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "购物车项不存在");
        }
        cart.setQuantity(quantity);
        cartMapper.updateById(cart);
        return Result.ok(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result removeFromCart(Long userId, Long productId) {
        if (userId == null || productId == null) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "参数不完整");
        }
        cartMapper.delete(new LambdaQueryWrapper<UserCart>()
                .eq(UserCart::getUserId, userId)
                .eq(UserCart::getProductId, productId));
        return Result.ok(null);
    }

    @Override
    public Result getCartList(Long userId) {
        if (userId == null) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "用户ID不能为空");
        }
        List<UserCart> carts = cartMapper.selectList(new LambdaQueryWrapper<UserCart>()
                .eq(UserCart::getUserId, userId)
                .orderByDesc(UserCart::getCreateTime));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (UserCart cart : carts) {
            ProductInfo product = productMapper.selectById(cart.getProductId());
            if (product != null && product.getStatus() == 1) {
                Map<String, Object> row = new HashMap<>();
                row.put("cartId", cart.getId());
                row.put("productId", product.getId());
                row.put("productName", product.getProductName());
                row.put("price", product.getPrice());
                row.put("quantity", cart.getQuantity());
                row.put("coverImg", product.getCoverImg());
                rows.add(row);
            }
        }
        return Result.ok(rows);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result addToFavorite(Long userId, Long productId) {
        if (userId == null || productId == null) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "参数不完整");
        }
        ProductInfo product = productMapper.selectById(productId);
        if (product == null) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "商品不存在");
        }
        UserFavorite existing = favoriteMapper.selectOne(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getProductId, productId));
        if (existing != null) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "已收藏过该商品");
        }
        UserFavorite fav = new UserFavorite();
        fav.setUserId(userId);
        fav.setProductId(productId);
        favoriteMapper.insert(fav);
        return Result.ok(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result removeFromFavorite(Long userId, Long productId) {
        if (userId == null || productId == null) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "参数不完整");
        }
        favoriteMapper.delete(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getProductId, productId));
        return Result.ok(null);
    }

    @Override
    public Result getFavoriteList(Long userId) {
        if (userId == null) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "用户ID不能为空");
        }
        List<UserFavorite> favorites = favoriteMapper.selectList(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .orderByDesc(UserFavorite::getCreateTime));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (UserFavorite fav : favorites) {
            ProductInfo product = productMapper.selectById(fav.getProductId());
            if (product != null && product.getStatus() == 1) {
                Map<String, Object> row = new HashMap<>();
                row.put("productId", product.getId());
                row.put("productName", product.getProductName());
                row.put("price", product.getPrice());
                row.put("coverImg", product.getCoverImg());
                row.put("detailText", product.getDetailText());
                rows.add(row);
            }
        }
        return Result.ok(rows);
    }

    @Override
    public Result checkFavorite(Long userId, Long productId) {
        if (userId == null || productId == null) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "参数不完整");
        }
        UserFavorite fav = favoriteMapper.selectOne(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getProductId, productId));
        Map<String, Object> data = new HashMap<>();
        data.put("isFavorite", fav != null);
        return Result.ok(data);
    }
}
