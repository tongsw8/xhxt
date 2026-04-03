package lz.xhxt.controller;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/staff/reply")
public class StaffReplyController {

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ForumPostMapper forumPostMapper;

    @Resource
    private ProductInfoMapper productInfoMapper;

    @Resource
    private UserActionMapper userActionMapper;

    @GetMapping("/pending-count")
    public Result pendingCount() {
        List<Comment> roots = listRootComments(null, null);
        int count = 0;
        for (Comment c : roots) {
            if (!isResolved(c)) count++;
        }
        return Result.ok(count);
    }

    @GetMapping("/list")
    public Result list(@RequestParam(required = false) String source,
                       @RequestParam(required = false) Integer handled,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Integer mineHandled,
                       @RequestParam(required = false) Long staffUserId) {
        List<Comment> roots = listRootComments(source, keyword);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Comment c : roots) {
            boolean hasReply = hasStaffReply(c);
            boolean handledMark = hasHandledMark(c.getId());
            boolean resolved = hasReply || handledMark;
            if (handled != null) {
                if (handled == 0 && resolved) continue;
                if (handled == 1 && !resolved) continue;
            }
            if (mineHandled != null && mineHandled == 1 && (staffUserId == null || !handledByStaff(c, staffUserId))) {
                continue;
            }
            User u = userMapper.selectById(c.getUserId());
            Map<String, Object> row = new HashMap<>();
            row.put("id", c.getId());
            row.put("content", c.getContent());
            row.put("source", normalizeSource(c.getTargetType()));
            row.put("sourceTitle", sourceTitle(c.getTargetType(), c.getTargetId()));
            row.put("targetId", c.getTargetId());
            row.put("sourcePath", sourcePath(c.getTargetType(), c.getTargetId()));
            row.put("userId", c.getUserId());
            row.put("userName", u == null ? "匿名用户" : (u.getNickname() == null ? u.getAccount() : u.getNickname()));
            row.put("status", c.getStatus());
            row.put("hasStaffReply", hasReply);
            row.put("handledMark", handledMark);
            row.put("resolved", resolved);
            row.put("createTime", c.getCreateTime());
            rows.add(row);
        }
        return Result.ok(rows);
    }

    @GetMapping("/thread")
    public Result thread(@RequestParam Long rootId) {
        Comment root = commentMapper.selectById(rootId);
        if (root == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "评论不存在");
        Long realRootId = resolveRootId(root);

        List<Comment> comments = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getStatus, 1)
                .eq(Comment::getTargetType, root.getTargetType())
                .eq(Comment::getTargetId, root.getTargetId())
                .and(q -> q.eq(Comment::getId, realRootId).or().eq(Comment::getParentId, realRootId))
                .orderByAsc(Comment::getCreateTime));

        List<Map<String, Object>> list = comments.stream().map(this::toCommentRow).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("source", normalizeSource(root.getTargetType()));
        data.put("sourceTitle", sourceTitle(root.getTargetType(), root.getTargetId()));
        data.put("targetId", root.getTargetId());
        data.put("sourcePath", sourcePath(root.getTargetType(), root.getTargetId()));
        data.put("rows", list);
        return Result.ok(data);
    }

    @PostMapping("/reply")
    public Result reply(@RequestBody Map<String, Object> body) {
        Long rootId = toLong(body.get("rootId"));
        Long staffUserId = toLong(body.get("staffUserId"));
        String content = body.get("content") == null ? null : String.valueOf(body.get("content")).trim();
        if (rootId == null || staffUserId == null || content == null || content.isEmpty()) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "参数不完整");
        }

        Comment root = commentMapper.selectById(rootId);
        if (root == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "评论不存在");
        Long realRootId = resolveRootId(root);

        Comment c = new Comment();
        c.setTargetType(root.getTargetType());
        c.setTargetId(root.getTargetId());
        c.setUserId(staffUserId);
        c.setParentId(realRootId);
        c.setContent(content);
        c.setIsStaff(1);
        c.setStatus(1);
        c.setCreateTime(new Date());
        commentMapper.insert(c);

        clearHandledMark(realRootId);
        return Result.ok(null);
    }

    @PostMapping("/handled")
    public Result handled(@RequestBody Map<String, Object> body) {
        Long rootId = toLong(body.get("rootId"));
        Integer handled = toInt(body.get("handled"));
        Long staffUserId = toLong(body.get("staffUserId"));
        if (rootId == null || handled == null || staffUserId == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "参数不完整");
        Comment root = commentMapper.selectById(rootId);
        if (root == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "评论不存在");

        if (handled == 1) {
            if (!hasHandledMark(rootId)) {
                UserAction mark = new UserAction();
                mark.setUserId(staffUserId);
                mark.setTargetId(rootId);
                mark.setTargetType("STAFF_REPLY");
                mark.setActionType("HANDLED");
                mark.setCreateTime(new Date());
                userActionMapper.insert(mark);
            }
        } else {
            clearHandledMark(rootId);
        }
        return Result.ok(null);
    }

    @PostMapping("/status")
    public Result status(@RequestBody Map<String, Object> body) {
        Long id = toLong(body.get("id"));
        Integer status = toInt(body.get("status"));
        if (id == null || status == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "参数不完整");
        Comment c = commentMapper.selectById(id);
        if (c == null) return Result.ok(null);
        c.setStatus(status);
        commentMapper.updateById(c);
        return Result.ok(null);
    }

    private List<Comment> listRootComments(String source, String keyword) {
        List<Comment> comments = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getStatus, 1)
                .eq(Comment::getIsStaff, 0)
                .in(Comment::getTargetType, "POST", "ORDER_PRODUCT", "PRODUCT_REVIEW")
                .orderByDesc(Comment::getCreateTime));

        return comments.stream().filter(c -> {
            if (!isRootComment(c)) return false;
            if (source != null && !source.trim().isEmpty()) {
                String s = source.trim();
                if ("ORDER_PRODUCT".equals(s)) {
                    if (!("ORDER_PRODUCT".equals(c.getTargetType()) || "PRODUCT_REVIEW".equals(c.getTargetType()))) return false;
                } else if (!s.equals(c.getTargetType())) {
                    return false;
                }
            }
            return keyword == null || keyword.trim().isEmpty() || (c.getContent() != null && c.getContent().contains(keyword.trim()));
        }).collect(Collectors.toList());
    }

    private boolean isRootComment(Comment c) {
        if ("PRODUCT_REVIEW".equals(c.getTargetType())) return true;
        return c.getParentId() == null || c.getParentId() == 0;
    }

    private Long resolveRootId(Comment c) {
        if ("PRODUCT_REVIEW".equals(c.getTargetType())) return c.getId();
        if (c.getParentId() == null || c.getParentId() == 0) return c.getId();
        return c.getParentId();
    }

    private boolean hasStaffReply(Comment root) {
        Long realRootId = resolveRootId(root);
        Integer cnt = commentMapper.selectCount(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getStatus, 1)
                .eq(Comment::getTargetType, root.getTargetType())
                .eq(Comment::getTargetId, root.getTargetId())
                .eq(Comment::getParentId, realRootId)
                .eq(Comment::getIsStaff, 1));
        return cnt != null && cnt > 0;
    }

    private boolean hasHandledMark(Long rootId) {
        Integer cnt = userActionMapper.selectCount(new LambdaQueryWrapper<UserAction>()
                .eq(UserAction::getTargetType, "STAFF_REPLY")
                .eq(UserAction::getActionType, "HANDLED")
                .eq(UserAction::getTargetId, rootId));
        return cnt != null && cnt > 0;
    }

    private boolean handledByStaff(Comment root, Long staffUserId) {
        Long realRootId = resolveRootId(root);
        Integer replyCnt = commentMapper.selectCount(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getStatus, 1)
                .eq(Comment::getTargetType, root.getTargetType())
                .eq(Comment::getTargetId, root.getTargetId())
                .eq(Comment::getParentId, realRootId)
                .eq(Comment::getIsStaff, 1)
                .eq(Comment::getUserId, staffUserId));
        if (replyCnt != null && replyCnt > 0) return true;
        Integer markCnt = userActionMapper.selectCount(new LambdaQueryWrapper<UserAction>()
                .eq(UserAction::getTargetType, "STAFF_REPLY")
                .eq(UserAction::getActionType, "HANDLED")
                .eq(UserAction::getTargetId, root.getId())
                .eq(UserAction::getUserId, staffUserId));
        return markCnt != null && markCnt > 0;
    }

    private boolean isResolved(Comment root) {
        return hasStaffReply(root) || hasHandledMark(root.getId());
    }

    private void clearHandledMark(Long rootId) {
        userActionMapper.delete(new LambdaQueryWrapper<UserAction>()
                .eq(UserAction::getTargetType, "STAFF_REPLY")
                .eq(UserAction::getActionType, "HANDLED")
                .eq(UserAction::getTargetId, rootId));
    }

    private String sourceTitle(String source, Long targetId) {
        if ("POST".equals(source)) {
            ForumPost p = forumPostMapper.selectById(targetId);
            return p == null ? "论坛帖子" : p.getTitle();
        }
        if ("ORDER_PRODUCT".equals(source) || "PRODUCT_REVIEW".equals(source)) {
            ProductInfo p = productInfoMapper.selectById(targetId);
            return p == null ? "商品评价" : p.getProductName();
        }
        return "-";
    }

    private String sourcePath(String source, Long targetId) {
        if ("POST".equals(source)) return "/community/detail/" + targetId;
        if ("ORDER_PRODUCT".equals(source) || "PRODUCT_REVIEW".equals(source)) return "/shop/detail/" + targetId;
        return "/";
    }

    private String normalizeSource(String source) {
        if ("POST".equals(source)) return "POST";
        if ("ORDER_PRODUCT".equals(source) || "PRODUCT_REVIEW".equals(source)) return "ORDER_PRODUCT";
        return source;
    }

    private Map<String, Object> toCommentRow(Comment c) {
        User u = userMapper.selectById(c.getUserId());
        Map<String, Object> row = new HashMap<>();
        row.put("id", c.getId());
        row.put("content", c.getContent());
        row.put("userId", c.getUserId());
        row.put("userName", u == null ? "匿名用户" : (u.getNickname() == null ? u.getAccount() : u.getNickname()));
        row.put("isStaff", c.getIsStaff());
        row.put("createTime", c.getCreateTime());
        return row;
    }

    private Long toLong(Object v) {
        if (v == null) return null;
        try { return Long.parseLong(String.valueOf(v)); } catch (Exception e) { return null; }
    }

    private Integer toInt(Object v) {
        if (v == null) return null;
        try { return Integer.parseInt(String.valueOf(v)); } catch (Exception e) { return null; }
    }
}