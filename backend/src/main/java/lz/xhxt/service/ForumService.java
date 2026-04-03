package lz.xhxt.service;

import lz.xhxt.common.Result;

public interface ForumService {

    Result listPosts(Long userId, String keyword, boolean includeInvisibleMine);

    Result postDetail(Long userId, Long id);

    Result createPost(Long userId, Long productId, String title, String content);

    Result updatePost(Long userId, Long id, Long productId, String title, String content);

    Result togglePostVisibility(Long userId, Long id, Integer status);

    Result adminPatchPost(Long id, Integer isTop, Integer isBest, Integer status);

    Result deletePost(Long userId, Long id, boolean asAdmin);

    Result listMyPosts(Long userId);

    Result listComments(Long userId, Long postId);

    Result addComment(Long userId, Long postId, Long parentId, String content);

    Result deleteComment(Long userId, Long commentId, boolean asAdmin);

    Result toggleAction(Long userId, Long targetId, String targetType, String actionType);
}