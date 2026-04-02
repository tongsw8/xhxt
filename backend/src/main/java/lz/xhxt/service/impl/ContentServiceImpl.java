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

import java.util.Date;
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
        return bannerMapper.selectList(new LambdaQueryWrapper<Banner>()
                .eq(Banner::getStatus, 1)
                .orderByAsc(Banner::getSortNo));
    }

    @Override
    public void saveBanner(Banner banner) {
        if (banner.getId() == null) bannerMapper.insert(banner);
        else bannerMapper.updateById(banner);
    }

    @Override
    public void deleteBanner(Long id) {
        bannerMapper.deleteById(id);
    }

    @Override
    public List<Notice> getPublishedNotices() {
        return noticeMapper.selectList(new LambdaQueryWrapper<Notice>()
                .eq(Notice::getStatus, 1)
                .orderByDesc(Notice::getIsTop)
                .orderByDesc(Notice::getPublishTime)
                .orderByDesc(Notice::getCreateTime));
    }

    @Override
    public Notice getPublishedNoticeDetail(Long id) {
        return noticeMapper.selectOne(new LambdaQueryWrapper<Notice>()
                .eq(Notice::getId, id)
                .eq(Notice::getStatus, 1));
    }

    @Override
    public List<Notice> getAdminNoticeList() {
        return noticeMapper.selectList(new LambdaQueryWrapper<Notice>()
                .orderByDesc(Notice::getIsTop)
                .orderByDesc(Notice::getUpdateTime)
                .orderByDesc(Notice::getCreateTime));
    }

    @Override
    public void saveNotice(Notice notice) {
        Date now = new Date();
        if (notice.getId() == null) {
            notice.setCreateTime(now);
            notice.setUpdateTime(now);
            if (notice.getStatus() != null && notice.getStatus() == 1 && notice.getPublishTime() == null) notice.setPublishTime(now);
            if (notice.getIsTop() == null) notice.setIsTop(0);
            if (notice.getStatus() == null) notice.setStatus(0);
            noticeMapper.insert(notice);
        } else {
            Notice old = noticeMapper.selectById(notice.getId());
            notice.setUpdateTime(now);
            if (notice.getStatus() != null && notice.getStatus() == 1 && old != null && old.getStatus() != null && old.getStatus() == 0) {
                if (notice.getPublishTime() == null) notice.setPublishTime(now);
            }
            noticeMapper.updateById(notice);
        }
    }

    @Override
    public void updateNoticeStatus(Long id, Integer status) {
        Notice n = noticeMapper.selectById(id);
        if (n == null) return;
        n.setStatus(status);
        n.setUpdateTime(new Date());
        if (status != null && status == 1 && n.getPublishTime() == null) n.setPublishTime(new Date());
        noticeMapper.updateById(n);
    }

    @Override
    public void updateNoticeTop(Long id, Integer isTop) {
        Notice n = new Notice();
        n.setId(id);
        n.setIsTop(isTop);
        n.setUpdateTime(new Date());
        noticeMapper.updateById(n);
    }

    @Override
    public void deleteNotice(Long id) {
        noticeMapper.deleteById(id);
    }

    @Override
    public List<NewsInfo> getNewsList(Long categoryId) {
        LambdaQueryWrapper<NewsInfo> query = new LambdaQueryWrapper<NewsInfo>().eq(NewsInfo::getStatus, 1);
        if (categoryId != null) query.eq(NewsInfo::getCategoryId, categoryId);
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
        if (news.getId() == null) newsInfoMapper.insert(news);
        else newsInfoMapper.updateById(news);
    }
}