package lz.xhxt.controller;

import lz.xhxt.common.Result;
import lz.xhxt.common.ResultCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/upload")
public class UploadController {

    @Value("${page.flowerImg:E:/Download/temp2/}")
    private String flowerDir;

    @Value("${page.noticeImg:E:/Download/temp2/pageImg/notice/}")
    private String noticeDir;

    @Value("${page.postImg:E:/Download/temp2/pageImg/post/}")
    private String postDir;

    @PostMapping("/image")
    public Result uploadImage(@RequestParam("file") MultipartFile file,
                              @RequestParam(required = false, defaultValue = "flower") String bizType,
                              HttpServletRequest request) {
        if (file == null || file.isEmpty()) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "上传文件不能为空");
        }
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (ext == null || ext.trim().isEmpty()) ext = "jpg";
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;

        String uploadDir = flowerDir;
        String uriPrefix = "/file/flower/";
        if ("notice".equalsIgnoreCase(bizType)) {
            uploadDir = noticeDir;
            uriPrefix = "/file/notice/";
        } else if ("post".equalsIgnoreCase(bizType)) {
            uploadDir = postDir;
            uriPrefix = "/file/post/";
        }

        File dir = new File(uploadDir);
        if (!dir.exists() && !dir.mkdirs()) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "创建上传目录失败");
        }
        File target = new File(dir, fileName);
        try {
            file.transferTo(target);
        } catch (IOException e) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "上传失败: " + e.getMessage());
        }

        String base = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String relative = request.getContextPath() + uriPrefix + fileName;

        Map<String, Object> data = new HashMap<>();
        data.put("fileName", fileName);
        data.put("path", target.getAbsolutePath());
        data.put("url", base + relative);
        data.put("relativeUrl", relative);
        return Result.ok(data);
    }
}