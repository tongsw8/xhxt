package lz.xhxt.controller;

import lz.xhxt.common.Result;
import lz.xhxt.common.ResultCode;
import lz.xhxt.service.CartFavoriteService;
import lz.xhxt.service.JwtService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/user")
public class CartFavoriteController {

    @Resource
    private CartFavoriteService cartFavoriteService;

    @Resource
    private JwtService jwtService;

    @PostMapping("/cart/add")
    public Result addToCart(@RequestHeader(value = "Authorization", required = false) String authorization,
                            @RequestParam Long productId,
                            @RequestParam(defaultValue = "1") Integer quantity) {
        Long userId = getUserIdFromToken(authorization);
        if (userId == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "请先登录");
        return cartFavoriteService.addToCart(userId, productId, quantity);
    }

    @PostMapping("/cart/update")
    public Result updateCartQuantity(@RequestHeader(value = "Authorization", required = false) String authorization,
                                     @RequestParam Long productId,
                                     @RequestParam Integer quantity) {
        Long userId = getUserIdFromToken(authorization);
        if (userId == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "请先登录");
        return cartFavoriteService.updateCartQuantity(userId, productId, quantity);
    }

    @DeleteMapping("/cart/remove/{productId}")
    public Result removeFromCart(@RequestHeader(value = "Authorization", required = false) String authorization,
                                 @PathVariable Long productId) {
        Long userId = getUserIdFromToken(authorization);
        if (userId == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "请先登录");
        return cartFavoriteService.removeFromCart(userId, productId);
    }

    @GetMapping("/cart/list")
    public Result getCartList(@RequestHeader(value = "Authorization", required = false) String authorization) {
        Long userId = getUserIdFromToken(authorization);
        if (userId == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "请先登录");
        return cartFavoriteService.getCartList(userId);
    }

    @PostMapping("/favorite/add")
    public Result addToFavorite(@RequestHeader(value = "Authorization", required = false) String authorization,
                                @RequestParam Long productId) {
        Long userId = getUserIdFromToken(authorization);
        if (userId == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "请先登录");
        return cartFavoriteService.addToFavorite(userId, productId);
    }

    @DeleteMapping("/favorite/remove/{productId}")
    public Result removeFromFavorite(@RequestHeader(value = "Authorization", required = false) String authorization,
                                     @PathVariable Long productId) {
        Long userId = getUserIdFromToken(authorization);
        if (userId == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "请先登录");
        return cartFavoriteService.removeFromFavorite(userId, productId);
    }

    @GetMapping("/favorite/list")
    public Result getFavoriteList(@RequestHeader(value = "Authorization", required = false) String authorization) {
        Long userId = getUserIdFromToken(authorization);
        if (userId == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "请先登录");
        return cartFavoriteService.getFavoriteList(userId);
    }

    @GetMapping("/favorite/check/{productId}")
    public Result checkFavorite(@RequestHeader(value = "Authorization", required = false) String authorization,
                                @PathVariable Long productId) {
        Long userId = getUserIdFromToken(authorization);
        if (userId == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "请先登录");
        return cartFavoriteService.checkFavorite(userId, productId);
    }

    private Long getUserIdFromToken(String authorization) {
        try {
            return jwtService.parseUserId(authorization);
        } catch (Exception e) {
            return null;
        }
    }
}
