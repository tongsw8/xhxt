package lz.xhxt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 前后端联调时开放 CORS。
 * 后续可把 allowedOrigins 收敛到具体的前端地址（用户端/管理端）。
 */
@Configuration
public class WebCorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("*"); // scaffold 默认先放开，后续收敛
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 由于本项目后端存在 context-path（/xhxt），这里先全量放开，避免联调时出现 CORS 不匹配
        source.registerCorsConfiguration("/**", configuration);

        return new CorsFilter(source);
    }
}

