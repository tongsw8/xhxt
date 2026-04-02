package lz.xhxt.service;

import lz.xhxt.entity.Banner;
import lz.xhxt.entity.NewsInfo;
import lz.xhxt.entity.Notice;

import java.util.List;

public interface ContentService {
    List<Banner> getActiveBanners();

    void saveBanner(Banner banner);

    void deleteBanner(Long id);

    List<Notice> getPublishedNotices();

    Notice getPublishedNoticeDetail(Long id);

    List<Notice> getAdminNoticeList();

    void saveNotice(Notice notice);

    void updateNoticeStatus(Long id, Integer status);

    void updateNoticeTop(Long id, Integer isTop);

    void deleteNotice(Long id);

    List<NewsInfo> getNewsList(Long categoryId);

    NewsInfo getNewsDetail(Long id);

    void saveNews(NewsInfo news);
}