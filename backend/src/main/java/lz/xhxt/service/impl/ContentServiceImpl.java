package lz.xhxt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lz.xhxt.entity.Banner;
import lz.xhxt.entity.NewsInfo;
import lz.xhxt.entity.Notice;
import lz.xhxt.mapper.BannerMapper;
import lz.xhxt.mapper.NewsInfoMapper;
import lz.xhxt.mapper.NoticeMapper;
import lz.xhxt.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContentServiceImpl implements ContentService {

    @Autowired
    private BannerMapper bannerMapper;

    @Autowired
    private NoticeMapper noticeMapper;

    @Autowired
    private NewsInfoMapper newsInfoMapper;

    @Override
    public List<Banner> getActiveBanners() {
        return bannerMapper.selectList(
                new LambdaQueryWrapper<Banner>()
                        .eq(Banner::getStatus, 1)
                        .orderByAsc(Banner::getSortNo)
        );
    }

    @Override
    public void saveBanner(Banner banner) {
        if (banner.getId() == null) {
            bannerMapper.insert(banner);
        } else {
            bannerMapper.updateById(banner);
        }
    }

    @Override
    public void deleteBanner(Long id) {
        bannerMapper.deleteById(id);
    }

    @Override
    public List<Notice> getLatestNotices() {
        return noticeMapper.selectList(
                new LambdaQueryWrapper<Notice>()
                        .eq(Notice::getStatus, 1)
                        .orderByDesc(Notice::getCreateTime)
        );
    }

    @Override
    public void saveNotice(Notice notice) {
        if (notice.getId() == null) {
            noticeMapper.insert(notice);
        } else {
            noticeMapper.updateById(notice);
        }
    }

    @Override
    public List<NewsInfo> getNewsList(Long categoryId) {
        LambdaQueryWrapper<NewsInfo> query = new LambdaQueryWrapper<NewsInfo>()
                .eq(NewsInfo::getStatus, 1);
        if (categoryId != null) {
            query.eq(NewsInfo::getCategoryId, categoryId);
        }
        query.orderByDesc(NewsInfo::getCreateTime);
        return newsInfoMapper.selectList(query);
    }

    @Override
    public NewsInfo getNewsDetail(Long id) {
        NewsInfo news = newsInfoMapper.selectById(id);
        if (news != null) {
            Integer viewCount = news.getViewCount() == null ? 0 : news.getViewCount();
            news.setViewCount(viewCount + 1);
            newsInfoMapper.updateById(news);
        }
        return news;
    }

    @Override
    public void saveNews(NewsInfo news) {
        if (news.getId() == null) {
            newsInfoMapper.insert(news);
        } else {
            newsInfoMapper.updateById(news);
        }
    }
}