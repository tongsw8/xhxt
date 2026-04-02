package lz.xhxt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lz.xhxt.common.Result;
import lz.xhxt.entity.Banner;
import lz.xhxt.entity.NewsInfo;
import lz.xhxt.entity.Notice;
import lz.xhxt.mapper.BannerMapper;
import lz.xhxt.service.ContentService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/content")
public class ContentController {

    @Resource
    private ContentService contentService;

    @Resource
    private BannerMapper bannerMapper;

    @GetMapping("/banners")
    public List<Banner> banners() {
        return contentService.getActiveBanners();
    }

    @GetMapping("/admin/banner/list")
    public Result adminBannerList() {
        List<Banner> list = bannerMapper.selectList(new LambdaQueryWrapper<Banner>()
                .orderByAsc(Banner::getSortNo)
                .orderByDesc(Banner::getCreateTime));
        return Result.ok(list);
    }

    @PostMapping("/admin/banner/save")
    public Result saveBanner(@RequestBody Banner banner) {
        if (banner.getCreateTime() == null) banner.setCreateTime(new Date());
        contentService.saveBanner(banner);
        return Result.ok(null);
    }

    @PostMapping("/admin/banner/status")
    public Result bannerStatus(@RequestParam Long id, @RequestParam Integer status) {
        Banner b = new Banner();
        b.setId(id);
        b.setStatus(status);
        bannerMapper.updateById(b);
        return Result.ok(null);
    }

    @DeleteMapping("/admin/banner/delete/{id}")
    public Result delBanner(@PathVariable Long id) {
        contentService.deleteBanner(id);
        return Result.ok(null);
    }

    @GetMapping("/notices")
    public Result notices() {
        return Result.ok(contentService.getPublishedNotices());
    }

    @GetMapping("/notice/{id}")
    public Result noticeDetail(@PathVariable Long id) {
        return Result.ok(contentService.getPublishedNoticeDetail(id));
    }

    @GetMapping("/admin/notice/list")
    public Result adminNoticeList() {
        return Result.ok(contentService.getAdminNoticeList());
    }

    @PostMapping("/admin/notice/save")
    public Result saveNotice(@RequestBody Notice notice) {
        contentService.saveNotice(notice);
        return Result.ok(null);
    }

    @PostMapping("/admin/notice/status")
    public Result noticeStatus(@RequestParam Long id, @RequestParam Integer status) {
        contentService.updateNoticeStatus(id, status);
        return Result.ok(null);
    }

    @PostMapping("/admin/notice/top")
    public Result noticeTop(@RequestParam Long id, @RequestParam Integer isTop) {
        contentService.updateNoticeTop(id, isTop);
        return Result.ok(null);
    }

    @DeleteMapping("/admin/notice/delete/{id}")
    public Result deleteNotice(@PathVariable Long id) {
        contentService.deleteNotice(id);
        return Result.ok(null);
    }

    @GetMapping("/news")
    public List<NewsInfo> news(@RequestParam(required = false) Long categoryId) {
        return contentService.getNewsList(categoryId);
    }

    @GetMapping("/news/{id}")
    public NewsInfo newsDetail(@PathVariable Long id) {
        return contentService.getNewsDetail(id);
    }

    @PostMapping("/admin/news")
    public void saveNews(@RequestBody NewsInfo news) {
        contentService.saveNews(news);
    }
}