package lz.xhxt.controller;

import lz.xhxt.common.Result;
import lz.xhxt.common.ResultCode;
import lz.xhxt.dto.LoginRequest;
import lz.xhxt.dto.RegisterRequest;
import lz.xhxt.service.JwtService;
import lz.xhxt.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private static final long CROSS_TICKET_EXPIRE_MS = 60_000L;
    private static final Map<String, TicketData> CROSS_TICKET_STORE = new ConcurrentHashMap<>();

    @Resource
    private UserService userService;

    @Resource
    private JwtService jwtService;

    @PostMapping("/login")
    public Result login(@RequestBody LoginRequest req) {
        if (req == null || isBlank(req.getAccount()) || isBlank(req.getPassword())) {
            log.info("用户登录-请求参数校验失败：account={}, password=<empty>", req == null ? null : req.getAccount());
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "账号或密码不能为空");
        }
        String account = req.getAccount().trim();
        String password = req.getPassword();

        String masked = maskPassword(password);
        log.info("用户登录-请求参数：account={}, password={}", account, masked);

        Result result = userService.login(account, password);
        if (result != null && Boolean.TRUE.equals(result.getSuccess()) && result.getData() instanceof java.util.Map) {
            Object data = result.getData();
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> map = (java.util.Map<String, Object>) data;
            Object user = map.get("user");
            if (user instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> userMap = (java.util.Map<String, Object>) user;
                log.info("用户登录-成功：account={}, role={}", account, userMap.get("role"));
            } else {
                log.info("用户登录-成功：account={}", account);
            }
        } else {
            log.info("用户登录-失败：account={}, message={}", account, result == null ? null : result.getMessage());
        }
        return result;
    }

    @PostMapping("/register")
    public Result register(@RequestBody RegisterRequest req) {
        if (req == null || isBlank(req.getAccount()) || isBlank(req.getPassword())) {
            log.info("用户注册-请求参数校验失败：account={}, password=<empty>", req == null ? null : req.getAccount());
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "账号或密码不能为空");
        }
        String account = req.getAccount().trim();
        String password = req.getPassword();
        String masked = maskPassword(password);
        String nickname = req.getNickname() == null ? null : req.getNickname().trim();

        log.info("用户注册-请求参数：account={}, nickname={}, password={}", account, nickname, masked);
        Result result = userService.register(account, password, nickname);
        log.info("用户注册-结果：account={}, success={}, message={}",
                account,
                result != null && Boolean.TRUE.equals(result.getSuccess()),
                result == null ? null : result.getMessage());
        return result;
    }

    @PostMapping("/cross-ticket")
    public Result createCrossTicket(@RequestHeader(value = "Authorization", required = false) String authorization) {
        Long userId;
        try {
            userId = jwtService.parseUserId(authorization);
        } catch (Exception e) {
            userId = null;
        }
        if (userId == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "请先登录");

        String pureToken = pureBearer(authorization);
        if (isBlank(pureToken)) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "登录态无效");

        clearExpiredTickets();
        String ticket = UUID.randomUUID().toString().replace("-", "");
        CROSS_TICKET_STORE.put(ticket, new TicketData(pureToken, System.currentTimeMillis() + CROSS_TICKET_EXPIRE_MS));
        return Result.ok(ticket);
    }

    @PostMapping("/exchange-ticket")
    public Result exchangeTicket(@RequestParam String ticket) {
        if (isBlank(ticket)) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "参数不完整");
        TicketData data = CROSS_TICKET_STORE.remove(ticket.trim());
        if (data == null || data.expireAt < System.currentTimeMillis()) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "跳转凭证已失效");
        }
        return Result.ok(data.token);
    }

    private void clearExpiredTickets() {
        long now = System.currentTimeMillis();
        CROSS_TICKET_STORE.entrySet().removeIf(e -> e.getValue() == null || e.getValue().expireAt < now);
    }

    private String pureBearer(String authorization) {
        if (authorization == null) return null;
        String s = authorization.trim();
        if (s.startsWith("Bearer ")) return s.substring(7);
        return s;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String maskPassword(String password) {
        if (password == null) return "<null>";
        int len = password.length();
        if (len <= 2) return "**";
        return password.charAt(0) + "***(" + len + " chars)";
    }

    private static class TicketData {
        private final String token;
        private final long expireAt;

        private TicketData(String token, long expireAt) {
            this.token = token;
            this.expireAt = expireAt;
        }
    }
}