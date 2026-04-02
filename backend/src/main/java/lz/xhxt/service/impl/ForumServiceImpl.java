package lz.xhxt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lz.xhxt.common.Result;
import lz.xhxt.common.ResultCode;
import lz.xhxt.entity.Comment;
import lz.xhxt.entity.ForumPost;
import lz.xhxt.entity.ProductInfo;
import lz.xhxt.entity.User;
import lz.xhxt.entity.UserAction;
import lz.xhxt.mapper.CommentMapper;
import lz.xhxt.mapper.ForumPostMapper;
import lz.xhxt.mapper.ProductInfoMapper;
import lz.xhxt.mapper.UserActionMapper;
import lz.xhxt.mapper.UserMapper;
import lz.xhxt.service.ForumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ForumServiceImpl implements ForumService {

    @Autowired
    private ForumPostMapper postMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserActionMapper actionMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductInfoMapper productMapper;

    @Override
    public Result listPosts(Long userId, String keyword, boolean includeInvisibleMine) {
        LambdaQueryWrapper<ForumPost> query = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) query.like(ForumPost::getTitle, keyword.trim());
        query.orderByDesc(ForumPost::getIsTop).orderByDesc(ForumPost::getCreateTime);

        List<ForumPost> all = postMapper.selectList(query);
        List<ForumPost> visible = all.stream().filter(p -> {
            if (p.getStatus() != null && p.getStatus() == 1) return true;
            return includeInvisibleMine && (userId == null || userId.equals(p.getUserId()));
        }).collect(Collectors.toList());

        List<Map<String, Object>> rows = new ArrayList<>();
        for (ForumPost p : visible) {
            User u = userMapper.selectById(p.getUserId());
            ProductInfo product = p.getProductId() == null ? null : productMapper.selectById(p.getProductId());
            Integer commentCount = commentMapper.selectCount(new LambdaQueryWrapper<Comment>()
                    .eq(Comment::getTargetType, "POST")
                    .eq(Comment::getTargetId, p.getId())
                    .eq(Comment::getStatus, 1));
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId());
            m.put("title", p.getTitle());
            m.put("content", p.getContent());
            m.put("viewCount", p.getViewCount());
            m.put("isTop", p.getIsTop());
            m.put("isBest", p.getIsBest());
            m.put("status", p.getStatus());
            m.put("createTime", p.getCreateTime());
            m.put("userId", p.getUserId());
            m.put("userName", u == null ? "匿名用户" : (u.getNickname() == null ? u.getAccount() : u.getNickname()));
            m.put("productId", p.getProductId());
            m.put("productName", product == null ? null : product.getProductName());
            m.put("commentCount", commentCount == null ? 0 : commentCount);
            rows.add(m);
        }
        return Result.ok(rows);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result postDetail(Long userId, Long id) {
        ForumPost p = postMapper.selectById(id);
        if (p == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "帖子不存在");
        if ((p.getStatus() == null || p.getStatus() != 1) && (userId == null || !userId.equals(p.getUserId()))) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "帖子不可见");
        }

        Integer view = p.getViewCount() == null ? 0 : p.getViewCount();
        p.setViewCount(view + 1);
        postMapper.updateById(p);

        User u = userMapper.selectById(p.getUserId());
        ProductInfo product = p.getProductId() == null ? null : productMapper.selectById(p.getProductId());

        Map<String, Object> data = new HashMap<>();
        data.put("id", p.getId());
        data.put("title", p.getTitle());
        data.put("content", p.getContent());
        data.put("viewCount", p.getViewCount());
        data.put("isTop", p.getIsTop());
        data.put("isBest", p.getIsBest());
        data.put("status", p.getStatus());
        data.put("createTime", p.getCreateTime());
        data.put("updateTime", p.getUpdateTime());
        data.put("userId", p.getUserId());
        data.put("userName", u == null ? "匿名用户" : (u.getNickname() == null ? u.getAccount() : u.getNickname()));
        data.put("product", toProductCard(product));
        return Result.ok(data);
    }

    @Override
    public Result createPost(Long userId, Long productId, String title, String content) {
        if (userId == null || title == null || title.trim().isEmpty() || content == null || content.trim().isEmpty()) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "参数不完整");
        }
        if (productId != null && productMapper.selectById(productId) == null) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "关联商品不存在");
        }
        Date now = new Date();
        ForumPost p = new ForumPost();
        p.setUserId(userId);
        p.setCategoryId(1L);
        p.setProductId(productId);
        p.setTitle(title.trim());
        p.setContent(content.trim());
        p.setViewCount(0);
        p.setIsTop(0);
        p.setIsBest(0);
        p.setStatus(1);
        p.setCreateTime(now);
        p.setUpdateTime(now);
        postMapper.insert(p);
        return Result.ok(p.getId());
    }

    @Override
    public Result updatePost(Long userId, Long id, Long productId, String title, String content) {
        ForumPost p = postMapper.selectById(id);
        if (p == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "帖子不存在");
        if (userId == null || !userId.equals(p.getUserId())) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "无权限修改");
        if (productId != null && productMapper.selectById(productId) == null) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "关联商品不存在");
        }
        p.setTitle(title == null ? p.getTitle() : title.trim());
        p.setContent(content == null ? p.getContent() : content.trim());
        p.setProductId(productId);
        p.setUpdateTime(new Date());
        postMapper.updateById(p);
        return Result.ok(null);
    }

    @Override
    public Result togglePostVisibility(Long userId, Long id, Integer status) {
        ForumPost p = postMapper.selectById(id);
        if (p == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "帖子不存在");
        if (userId == null || !userId.equals(p.getUserId())) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "无权限操作");
        p.setStatus(status != null && status == 0 ? 0 : 1);
        p.setUpdateTime(new Date());
        postMapper.updateById(p);
        return Result.ok(null);
    }

    @Override
    public Result adminPatchPost(Long id, Integer isTop, Integer isBest, Integer status) {
        ForumPost p = postMapper.selectById(id);
        if (p == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "帖子不存在");
        if (isTop != null) p.setIsTop(isTop);
        if (isBest != null) p.setIsBest(isBest);
        if (status != null) p.setStatus(status);
        p.setUpdateTime(new Date());
        postMapper.updateById(p);
        return Result.ok(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deletePost(Long userId, Long id, boolean asAdmin) {
        ForumPost p = postMapper.selectById(id);
        if (p == null) return Result.ok(null);
        if (!asAdmin && (userId == null || !userId.equals(p.getUserId()))) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "无权限删除");
        }
        postMapper.deleteById(id);
        commentMapper.delete(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getTargetType, "POST")
                .eq(Comment::getTargetId, id));
        return Result.ok(null);
    }

    @Override
    public Result listMyPosts(Long userId) {
        if (userId == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "请先登录");
        return listPosts(userId, null, true);
    }

    @Override
    public Result listComments(Long userId, Long postId) {
        List<Comment> comments = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getTargetType, "POST")
                .eq(Comment::getTargetId, postId)
                .eq(Comment::getStatus, 1)
                .orderByAsc(Comment::getCreateTime));
        List<Map<String, Object>> roots = new ArrayList<>();
        Map<Long, List<Map<String, Object>>> childMap = new HashMap<>();

        for (Comment c : comments) {
            User u = userMapper.selectById(c.getUserId());
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("content", c.getContent());
            m.put("userId", c.getUserId());
            m.put("userName", u == null ? "匿名用户" : (u.getNickname() == null ? u.getAccount() : u.getNickname()));
            m.put("parentId", c.getParentId());
            m.put("createTime", c.getCreateTime());
            m.put("canDelete", userId != null && userId.equals(c.getUserId()));
            if (c.getParentId() == null || c.getParentId() == 0) {
                m.put("replies", new ArrayList<>());
                roots.add(m);
            } else {
                childMap.computeIfAbsent(c.getParentId(), k -> new ArrayList<>()).add(m);
            }
        }

        for (Map<String, Object> root : roots) {
            Long rid = (Long) root.get("id");
            root.put("replies", childMap.getOrDefault(rid, new ArrayList<>()));
        }
        return Result.ok(roots);
    }

    @Override
    public Result addComment(Long userId, Long postId, Long parentId, String content) {
        if (userId == null || postId == null || content == null || content.trim().isEmpty()) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "参数不完整");
        }
        ForumPost post = postMapper.selectById(postId);
        if (post == null || post.getStatus() == null || post.getStatus() != 1) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "帖子不可评论");
        }
        if (parentId != null && parentId > 0) {
            Comment parent = commentMapper.selectById(parentId);
            if (parent == null || !"POST".equals(parent.getTargetType()) || !postId.equals(parent.getTargetId())) {
                return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "父评论不存在");
            }
            if (parent.getParentId() != null && parent.getParentId() > 0) {
                parentId = parent.getParentId();
            }
        }
        Comment c = new Comment();
        c.setTargetType("POST");
        c.setTargetId(postId);
        c.setUserId(userId);
        c.setParentId(parentId == null ? 0L : parentId);
        c.setContent(content.trim());
        c.setIsStaff(0);
        c.setStatus(1);
        c.setCreateTime(new Date());
        commentMapper.insert(c);
        return Result.ok(null);
    }

    @Override
    public Result deleteComment(Long userId, Long commentId, boolean asAdmin) {
        Comment c = commentMapper.selectById(commentId);
        if (c == null) return Result.ok(null);
        if (!asAdmin && (userId == null || !userId.equals(c.getUserId()))) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "无权限删除");
        }
        commentMapper.deleteById(commentId);
        commentMapper.delete(new LambdaQueryWrapper<Comment>().eq(Comment::getParentId, commentId));
        return Result.ok(null);
    }

    @Override
    public Result toggleAction(Long userId, Long targetId, String targetType, String actionType) {
        if (userId == null || targetId == null || targetType == null || actionType == null) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "参数不完整");
        }
        LambdaQueryWrapper<UserAction> query = new LambdaQueryWrapper<UserAction>()
                .eq(UserAction::getUserId, userId)
                .eq(UserAction::getTargetId, targetId)
                .eq(UserAction::getTargetType, targetType)
                .eq(UserAction::getActionType, actionType);

        Integer count = actionMapper.selectCount(query);
        if (count != null && count > 0) actionMapper.delete(query);
        else {
            UserAction action = new UserAction();
            action.setUserId(userId);
            action.setTargetId(targetId);
            action.setTargetType(targetType);
            action.setActionType(actionType);
            action.setCreateTime(new Date());
            actionMapper.insert(action);
        }
        return Result.ok(null);
    }

    private Map<String, Object> toProductCard(ProductInfo p) {
        if (p == null) return null;
        Map<String, Object> m = new HashMap<>();
        m.put("id", p.getId());
        m.put("name", p.getProductName());
        m.put("price", p.getPrice());
        m.put("coverImg", p.getCoverImg());
        return m;
    }
}