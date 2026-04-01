package lz.xhxt.service;

import lz.xhxt.entity.Comment;
import lz.xhxt.entity.ForumPost;

import java.util.List;

public interface ForumService {
    List<ForumPost> getPostList(Long categoryId, String keyword);

    ForumPost getPostDetail(Long id);

    void savePost(ForumPost post);

    void deletePost(Long id);

    List<Comment> getComments(Long targetId, String type);

    void addComment(Comment comment);

    void toggleAction(Long userId, Long targetId, String targetType, String actionType);
}