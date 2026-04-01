package lz.xhxt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lz.xhxt.entity.Comment;
import lz.xhxt.entity.ForumPost;
import lz.xhxt.entity.UserAction;
import lz.xhxt.mapper.CommentMapper;
import lz.xhxt.mapper.ForumPostMapper;
import lz.xhxt.mapper.UserActionMapper;
import lz.xhxt.service.ForumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ForumServiceImpl implements ForumService {

    @Autowired
    private ForumPostMapper postMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserActionMapper actionMapper;

    @Override
    public List<ForumPost> getPostList(Long categoryId, String keyword) {
        LambdaQueryWrapper<ForumPost> query = new LambdaQueryWrapper<ForumPost>()
                .eq(ForumPost::getStatus, 1);
        if (categoryId != null) {
            query.eq(ForumPost::getCategoryId, categoryId);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            query.like(ForumPost::getTitle, keyword.trim());
        }
        query.orderByDesc(ForumPost::getIsTop)
                .orderByDesc(ForumPost::getCreateTime);
        return postMapper.selectList(query);
    }

    @Override
    public ForumPost getPostDetail(Long id) {
        ForumPost post = postMapper.selectById(id);
        if (post != null) {
            Integer viewCount = post.getViewCount() == null ? 0 : post.getViewCount();
            post.setViewCount(viewCount + 1);
            postMapper.updateById(post);
        }
        return post;
    }

    @Override
    public void savePost(ForumPost post) {
        if (post.getId() == null) {
            postMapper.insert(post);
        } else {
            postMapper.updateById(post);
        }
    }

    @Override
    public void deletePost(Long id) {
        postMapper.deleteById(id);
    }

    @Override
    public List<Comment> getComments(Long targetId, String type) {
        return commentMapper.selectList(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getTargetId, targetId)
                        .eq(Comment::getTargetType, type)
                        .orderByAsc(Comment::getCreateTime)
        );
    }

    @Override
    public void addComment(Comment comment) {
        commentMapper.insert(comment);
    }

    @Override
    public void toggleAction(Long userId, Long targetId, String targetType, String actionType) {
        LambdaQueryWrapper<UserAction> query = new LambdaQueryWrapper<UserAction>()
                .eq(UserAction::getUserId, userId)
                .eq(UserAction::getTargetId, targetId)
                .eq(UserAction::getTargetType, targetType)
                .eq(UserAction::getActionType, actionType);

        Integer count = actionMapper.selectCount(query);
        if (count != null && count > 0) {
            actionMapper.delete(query);
        } else {
            UserAction action = new UserAction();
            action.setUserId(userId);
            action.setTargetId(targetId);
            action.setTargetType(targetType);
            action.setActionType(actionType);
            actionMapper.insert(action);
        }
    }
}