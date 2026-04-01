package lz.xhxt.controller;

import lz.xhxt.entity.Comment;
import lz.xhxt.entity.ForumPost;
import lz.xhxt.service.ForumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/forum")
public class ForumController {

    @Autowired
    private ForumService forumService;

    @GetMapping("/posts")
    public List<ForumPost> posts(@RequestParam(required = false) Long categoryId,
                                 @RequestParam(required = false) String keyword) {
        return forumService.getPostList(categoryId, keyword);
    }

    @GetMapping("/post/{id}")
    public ForumPost postDetail(@PathVariable Long id) {
        return forumService.getPostDetail(id);
    }

    @PostMapping("/post")
    public void savePost(@RequestBody ForumPost post) {
        forumService.savePost(post);
    }

    @DeleteMapping("/admin/post/{id}")
    public void delPost(@PathVariable Long id) {
        forumService.deletePost(id);
    }

    @GetMapping("/comments")
    public List<Comment> comments(@RequestParam Long targetId, @RequestParam String type) {
        return forumService.getComments(targetId, type);
    }

    @PostMapping("/comment")
    public void addComment(@RequestBody Comment comment) {
        forumService.addComment(comment);
    }

    @PostMapping("/action")
    public void toggleAction(@RequestParam Long userId,
                             @RequestParam Long targetId,
                             @RequestParam String targetType,
                             @RequestParam String actionType) {
        forumService.toggleAction(userId, targetId, targetType, actionType);
    }
}