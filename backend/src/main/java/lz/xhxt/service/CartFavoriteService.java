package lz.xhxt.service;

import lz.xhxt.common.Result;

public interface CartFavoriteService {

    Result addToCart(Long userId, Long productId, Integer quantity);

    Result updateCartQuantity(Long userId, Long productId, Integer quantity);

    Result removeFromCart(Long userId, Long productId);

    Result getCartList(Long userId);

    Result addToFavorite(Long userId, Long productId);

    Result removeFromFavorite(Long userId, Long productId);

    Result getFavoriteList(Long userId);

    Result checkFavorite(Long userId, Long productId);
}
