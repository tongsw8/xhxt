package lz.xhxt.service;

import lz.xhxt.common.Result;

public interface StaffReplyService {

    Result pendingCount();

    Result list(String type, String status, String keyword);

    Result thread(Long id);

    Result reply(Long staffId, Long parentCommentId, String content);

    Result markDone(Long staffId, Long parentCommentId);

    Result block(Long parentCommentId);
}