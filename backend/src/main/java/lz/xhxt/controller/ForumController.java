package lz.xhxt.controller;

import lz.xhxt.common.Result;
import lz.xhxt.common.ResultCode;
import lz.xhxt.service.ForumService;
import lz.xhxt.service.JwtService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/api/forum")
public class ForumController {

    @Resource
    private ForumService forumService;

    @Resource
    private JwtService jwtService;

    @GetMapping("/posts")
    public Result posts(@RequestHeader(value = "Authorization", required = false) String authorization,
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false, defaultValue = "false") Boolean mine) {
        Long userId = getUserIdFromToken(authorization);
        if (mine != null && mine && userId == null) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "请先登录");
        }
        return forumService.listPosts(userId, keyword, mine != null && mine);
    }

    @GetMapping("/admin/posts")
    public Result adminPosts(@RequestParam(required = false) String keyword) {
        return forumService.listPosts(null, keyword, true);
    }

    @GetMapping("/post/{id}")
    public Result postDetail(@RequestHeader(value = "Authorization", required = false) String authorization,
                             @PathVariable Long id) {
        Long userId = getUserIdFromToken(authorization);
        return forumService.postDetail(userId, id);
    }

    @PostMapping("/post")
    public Result savePost(@RequestHeader(value = "Authorization", required = false) String authorization,
                           @RequestBody Map<String, Object> body) {
        Long userId = getUserIdFromToken(authorization);
        if (userId == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "请先登录");
        Long id = toLong(body.get("id"));
        Long productId = toLong(body.get("productId"));
        String title = toStr(body.get("title"));
        String content = toStr(body.get("content"));
        if (id == null) return forumService.createPost(userId, productId, title, content);
        return forumService.updatePost(userId, id, productId, title, content);
    }

    @PostMapping("/post/visibility")
    public Result postVisibility(@RequestHeader(value = "Authorization", required = false) String authorization,
                                 @RequestParam Long id,
                                 @RequestParam Integer status) {
        Long userId = getUserIdFromToken(authorization);
        if (userId == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "请先登录");
        return forumService.togglePostVisibility(userId, id, status);
    }

    @DeleteMapping("/post/{id}")
    public Result deleteMyPost(@RequestHeader(value = "Authorization", required = false) String authorization,
                               @PathVariable Long id) {
        Long userId = getUserIdFromToken(authorization);
        if (userId == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "请先登录");
        return forumService.deletePost(userId, id, false);
    }

    @GetMapping("/my-posts")
    public Result myPosts(@RequestHeader(value = "Authorization", required = false) String authorization) {
        Long userId = getUserIdFromToken(authorization);
        if (userId == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "请先登录");
        return forumService.listMyPosts(userId);
    }

    @DeleteMapping("/admin/post/{id}")
    public Result delPostByAdmin(@PathVariable Long id) {
        return forumService.deletePost(null, id, true);
    }

    @PostMapping("/admin/post/update")
    public Result updatePostByAdmin(@RequestBody Map<String, Object> body) {
        Long id = toLong(body.get("id"));
        if (id == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "参数不完整");
        Integer isTop = toInt(body.get("isTop"));
        Integer isBest = toInt(body.get("isBest"));
        Integer status = toInt(body.get("status"));
        return forumService.adminPatchPost(id, isTop, isBest, status);
    }

    @GetMapping("/comments")
    public Result comments(@RequestHeader(value = "Authorization", required = false) String authorization,
                           @RequestParam Long targetId) {
        Long userId = getUserIdFromToken(authorization);
        return forumService.listComments(userId, targetId);
    }

    @PostMapping("/comment")
    public Result addComment(@RequestHeader(value = "Authorization", required = false) String authorization,
                             @RequestBody Map<String, Object> body) {
        Long userId = getUserIdFromToken(authorization);
        if (userId == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "请先登录");
        Long targetId = toLong(body.get("targetId"));
        Long parentId = toLong(body.get("parentId"));
        String content = toStr(body.get("content"));
        return forumService.addComment(userId, targetId, parentId, content);
    }

    @DeleteMapping("/comment/{id}")
    public Result deleteComment(@RequestHeader(value = "Authorization", required = false) String authorization,
                                @PathVariable Long id) {
        Long userId = getUserIdFromToken(authorization);
        if (userId == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "请先登录");
        return forumService.deleteComment(userId, id, false);
    }

    @DeleteMapping("/admin/comment/{id}")
    public Result deleteCommentByAdmin(@PathVariable Long id) {
        return forumService.deleteComment(null, id, true);
    }

    @PostMapping("/action")
    public Result toggleAction(@RequestHeader(value = "Authorization", required = false) String authorization,
                               @RequestParam Long targetId,
                               @RequestParam String targetType,
                               @RequestParam String actionType) {
        Long userId = getUserIdFromToken(authorization);
        if (userId == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "请先登录");
        return forumService.toggleAction(userId, targetId, targetType, actionType);
    }

    private Long getUserIdFromToken(String authorization) {
        try {
            return jwtService.parseUserId(authorization);
        } catch (Exception e) {
            return null;
        }
    }

    private Long toLong(Object v) {
        if (v == null) return null;
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (Exception e) {
            return null;
        }
    }

    private Integer toInt(Object v) {
        if (v == null) return null;
        try {
            return Integer.parseInt(String.valueOf(v));
        } catch (Exception e) {
            return null;
        }
    }

    private String toStr(Object v) {
        return v == null ? null : String.valueOf(v);
    }
}