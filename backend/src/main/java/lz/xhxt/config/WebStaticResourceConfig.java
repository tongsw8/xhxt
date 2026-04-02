package lz.xhxt.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebStaticResourceConfig implements WebMvcConfigurer {

    @Value("${page.flowerImg:E:/Download/temp2/}")
    private String flowerImgDir;

    @Value("${page.noticeImg:E:/Download/temp2/pageImg/notice/}")
    private String noticeImgDir;

    @Value("${page.postImg:E:/Download/temp2/pageImg/post/}")
    private String postImgDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/file/flower/**").addResourceLocations(toLocation(flowerImgDir));
        registry.addResourceHandler("/file/notice/**").addResourceLocations(toLocation(noticeImgDir));
        registry.addResourceHandler("/file/post/**").addResourceLocations(toLocation(postImgDir));
    }

    private String toLocation(String dir) {
        String location = "file:/" + dir.replace("\\", "/");
        if (!location.endsWith("/")) location = location + "/";
        return location;
    }
}