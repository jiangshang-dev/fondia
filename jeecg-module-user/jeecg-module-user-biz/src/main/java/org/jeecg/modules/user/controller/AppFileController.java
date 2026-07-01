package org.jeecg.modules.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.CommonUtils;
import org.jeecg.common.util.filter.SsrfFileTypeFilter;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.user.service.IFdUserService;
import org.jeecg.modules.user.vo.UploadFileVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;

import java.io.*;
import java.util.Set;

@Slf4j
@Tag(name = "App文件")
@RestController
public class AppFileController {

    private static final Set<String> ALLOWED_BIZ = Set.of("avatar", "feedback");

    @Resource
    private IFdUserService userService;

    @Value("${jeecg.path.upload}")
    private String uploadpath;

    @Value("${jeecg.uploadType:local}")
    private String uploadType;

    @Operation(summary = "App文件上传")
    @PostMapping("/user/upload")
    public Result<UploadFileVO> upload(@RequestHeader("Authorization") String authorization,
                                       HttpServletRequest request) {
        userService.getUserInfo(authorization);

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile file = multipartRequest.getFile("file");
        if (file == null || file.isEmpty()) {
            throw new JeecgBootException("上传文件不能为空");
        }

        String bizPath = request.getParameter("biz");
        if (oConvertUtils.isEmpty(bizPath)) {
            bizPath = "avatar";
        }
        if (!ALLOWED_BIZ.contains(bizPath)) {
            throw new JeecgBootException("不支持的上传目录");
        }

        try {
            SsrfFileTypeFilter.checkUploadFileType(file, bizPath);
        } catch (Exception e) {
            throw new JeecgBootException("上传文件类型错误");
        }

        String savePath;
        if (CommonConstant.UPLOAD_TYPE_LOCAL.equals(uploadType)) {
            savePath = uploadLocal(file, bizPath);
        } else {
            savePath = CommonUtils.upload(file, bizPath, uploadType);
        }

        if (oConvertUtils.isEmpty(savePath)) {
            throw new JeecgBootException("上传失败");
        }

        UploadFileVO vo = new UploadFileVO();
        vo.setPath(savePath);
        vo.setUrl(buildFileUrl(request, savePath));
        return Result.ok(vo);
    }

    @GetMapping("/user/common/static/**")
    public void viewStatic(HttpServletRequest request, HttpServletResponse response) {
        String imgPath = extractPathFromPattern(request, "/user/common/static/");
        if (oConvertUtils.isEmpty(imgPath) || CommonConstant.STRING_NULL.equals(imgPath)) {
            return;
        }

        try {
            imgPath = imgPath.replace("..", "").replace("../", "");
            if (imgPath.endsWith(SymbolConstant.COMMA)) {
                imgPath = imgPath.substring(0, imgPath.length() - 1);
            }
            SsrfFileTypeFilter.checkDownloadFileType(imgPath);

            String filePath = uploadpath + File.separator + imgPath;
            File file = new File(filePath);
            if (!file.exists()) {
                response.setStatus(404);
                log.warn("文件[{}]不存在", imgPath);
                return;
            }

            String contentType = resolveContentType(file.getName());
            response.setContentType(contentType);
            response.addHeader("Cache-Control", "public, max-age=86400");

            try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
                 OutputStream outputStream = response.getOutputStream()) {
                byte[] buf = new byte[8192];
                int len;
                while ((len = inputStream.read(buf)) != -1) {
                    outputStream.write(buf, 0, len);
                }
                outputStream.flush();
            }
        } catch (IOException e) {
            log.error("预览文件失败: {}", e.getMessage(), e);
            response.setStatus(404);
        }
    }

    private String uploadLocal(MultipartFile mf, String bizPath) {
        try {
            File dir = new File(uploadpath + File.separator + bizPath + File.separator);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String orgName = CommonUtils.getFileName(mf.getOriginalFilename());
            String fileName;
            if (orgName.contains(SymbolConstant.SPOT)) {
                fileName = orgName.substring(0, orgName.lastIndexOf('.'))
                        + "_" + System.currentTimeMillis()
                        + orgName.substring(orgName.lastIndexOf('.'));
            } else {
                fileName = orgName + "_" + System.currentTimeMillis();
            }

            File savefile = new File(dir, fileName);
            FileCopyUtils.copy(mf.getBytes(), savefile);

            String dbpath = bizPath + File.separator + fileName;
            if (dbpath.contains(SymbolConstant.DOUBLE_BACKSLASH)) {
                dbpath = dbpath.replace(SymbolConstant.DOUBLE_BACKSLASH, SymbolConstant.SINGLE_SLASH);
            }
            return dbpath;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return "";
        }
    }

    private String buildFileUrl(HttpServletRequest request, String savePath) {
        String normalized = savePath.replace("\\", "/");
        return CommonUtils.getBaseUrl(request) + "/user/common/static/" + normalized;
    }

    private String resolveContentType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".gif")) {
            return "image/gif";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        return "image/jpeg";
    }

    private String extractPathFromPattern(HttpServletRequest request, String prefix) {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        return new AntPathMatcher().extractPathWithinPattern(bestMatchPattern, path);
    }
}
