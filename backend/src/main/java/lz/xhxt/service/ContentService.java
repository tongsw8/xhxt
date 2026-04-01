package lz.xhxt.service;

import lz.xhxt.entity.Banner;
import lz.xhxt.entity.NewsInfo;
import lz.xhxt.entity.Notice;

import java.util.List;

public interface ContentService {
    List<Banner> getActiveBanners();

    void saveBanner(Banner banner);

    void deleteBanner(Long id);

    List<Notice> getLatestNotices();

    void saveNotice(Notice notice);

    List<NewsInfo> getNewsList(Long categoryId);

    NewsInfo getNewsDetail(Long id);

    void saveNews(NewsInfo news);
}