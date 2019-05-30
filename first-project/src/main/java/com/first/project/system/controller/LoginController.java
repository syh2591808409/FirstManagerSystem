package com.first.project.system.controller;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.first.project.common.annotation.Limit;
import com.first.project.common.authentication.JWTToken;
import com.first.project.common.authentication.JWTUtil;
import com.first.project.common.domain.ActiveUser;
import com.first.project.common.domain.FirstConstant;
import com.first.project.common.domain.FirstResponse;
import com.first.project.common.exception.FirstException;
import com.first.project.common.properties.FirstProperties;
import com.first.project.common.service.RedisService;
import com.first.project.common.utils.*;
import com.first.project.system.UserManager;
import com.first.project.system.dao.LoginLogMapper;
import com.first.project.system.dao.UserRoleMapper;
import com.first.project.system.domain.LoginLog;
import com.first.project.system.domain.User;
import com.first.project.system.domain.UserConfig;
import com.first.project.system.domain.UserRole;
import com.first.project.system.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.lionsoul.ip2region.DbSearcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Validated
@RestController
public class LoginController {

    @Autowired
    private RedisService redisService;
    @Autowired
    private UserManager userManager;
    @Autowired
    private UserService userService;
    @Autowired
    private DeptService deptService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private LoginLogService loginLogService;
    @Autowired
    private LoginLogMapper loginLogMapper;
    @Autowired
    private UserRoleMapper userRoleMapper;
    @Autowired
    private FirstProperties properties;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private UserConfigService userConfigService;

    HttpSession session;


    @PostMapping("/login")
    @Limit(key = "login", period = 60, count = 20, name = "登录接口", prefix = "limit")
    public FirstResponse login(
            @NotBlank(message = "{required}") String username,
            @NotBlank(message = "{required}") String password, HttpServletRequest request) throws Exception {
        username = StringUtils.lowerCase(username);
        password = MD5Util.encrypt(username, password);

        final String errorMessage = "用户名或密码错误";
        User user = this.userManager.getUser(username);

        if (user == null){
            throw new FirstException(errorMessage);
        }
        if (!StringUtils.equals(user.getPassword(), password)){
            throw new FirstException(errorMessage);
        }
        if (User.STATUS_LOCK.equals(user.getStatus())){
            throw new FirstException("账号已被锁定,请联系管理员！");
        }

        // 更新用户登录时间
        this.userService.updateLoginTime(username);
        // 保存登录记录
        LoginLog loginLog = new LoginLog();
        loginLog.setUsername(username);
        this.loginLogService.saveLoginLog(loginLog);

        String token = FirstUtil.encryptToken(JWTUtil.sign(username, password));
        LocalDateTime expireTime = LocalDateTime.now().plusSeconds(properties.getShiro().getJwtTimeOut());
        String expireTimeStr = DateUtil.formatFullTime(expireTime);
        JWTToken jwtToken = new JWTToken(token, expireTimeStr);

        String userId = this.saveTokenToRedis(user, jwtToken, request);
        user.setId(userId);

        Map<String, Object> userInfo = this.generateUserInfo(jwtToken, user);
        return new FirstResponse().message("认证成功").data(userInfo);
    }

//    @GetMapping('login/{mobile}')
//    @Limit(key = "login", period = 60, count = 20, name = "登录接口", prefix = "limit")
//    public FirstResponse login(@PathVariable("mobile")String mobile){
//
//    }
//    @GetMapping("/mobilevalidate")
//    public void getmobilevalidate(HttpServletRequest request,HttpServletResponse response){
//
//    }

    @GetMapping("index/{username}")
    public FirstResponse index(@NotBlank(message = "{required}") @PathVariable String username) {
        Map<String, Object> data = new HashMap<>();
        // 获取系统访问记录
        Long totalVisitCount = loginLogMapper.findTotalVisitCount();
        data.put("totalVisitCount", totalVisitCount);
        Long todayVisitCount = loginLogMapper.findTodayVisitCount();
        data.put("todayVisitCount", todayVisitCount);
        Long todayIp = loginLogMapper.findTodayIp();
        data.put("todayIp", todayIp);
        // 获取近期系统访问记录
        List<Map<String, Object>> lastSevenVisitCount = loginLogMapper.findLastSevenDaysVisitCount(null);
        data.put("lastSevenVisitCount", lastSevenVisitCount);
        User param = new User();
        param.setUsername(username);
        List<Map<String, Object>> lastSevenUserVisitCount = loginLogMapper.findLastSevenDaysVisitCount(param);
        data.put("lastSevenUserVisitCount", lastSevenUserVisitCount);
        return new FirstResponse().data(data);
    }

    @RequiresPermissions("user:online")
    @GetMapping("online")
    public FirstResponse userOnline(String username) throws Exception {
        String now = DateUtil.formatFullTime(LocalDateTime.now());
        Set<String> userOnlineStringSet = redisService.zrangeByScore(FirstConstant.ACTIVE_USERS_ZSET_PREFIX, now, "+inf");
        List<ActiveUser> activeUsers = new ArrayList<>();
        for (String userOnlineString : userOnlineStringSet) {
            ActiveUser activeUser = mapper.readValue(userOnlineString, ActiveUser.class);
            activeUser.setToken(null);
            if (StringUtils.isNotBlank(username)) {
                if (StringUtils.equalsIgnoreCase(username, activeUser.getUsername()))
                    activeUsers.add(activeUser);
            } else {
                activeUsers.add(activeUser);
            }
        }
        return new FirstResponse().data(activeUsers);
    }

    @DeleteMapping("kickout/{id}")
    @RequiresPermissions("user:kickout")
    public void kickout(@NotBlank(message = "{required}") @PathVariable String id) throws Exception {
        String now = DateUtil.formatFullTime(LocalDateTime.now());
        Set<String> userOnlineStringSet = redisService.zrangeByScore(FirstConstant.ACTIVE_USERS_ZSET_PREFIX, now, "+inf");
        ActiveUser kickoutUser = null;
        String kickoutUserString = "";
        for (String userOnlineString : userOnlineStringSet) {
            ActiveUser activeUser = mapper.readValue(userOnlineString, ActiveUser.class);
            if (StringUtils.equals(activeUser.getId(), id)) {
                kickoutUser = activeUser;
                kickoutUserString = userOnlineString;
            }
        }
        if (kickoutUser != null && StringUtils.isNotBlank(kickoutUserString)) {
            // 删除 zset中的记录
            redisService.zrem(FirstConstant.ACTIVE_USERS_ZSET_PREFIX, kickoutUserString);
            // 删除对应的 token缓存
            redisService.del(FirstConstant.TOKEN_CACHE_PREFIX + kickoutUser.getToken() + "." + kickoutUser.getIp());
        }
    }

    @GetMapping("logout/{id}")
    public void logout(@NotBlank(message = "{required}") @PathVariable String id) throws Exception {
        this.kickout(id);
    }

    @GetMapping("regist")
    public void regist(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try{
            response.setContentType("image/jpeg");//设置相应类型,告诉浏览器输出的内容为图片
            response.setHeader("Pragma", "No-cache");//设置响应头信息，告诉浏览器不要缓存此内容
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expire", 0);
            RandomValidateCodeUtil randomValidateCode = new RandomValidateCodeUtil();
            randomValidateCode.getRandcode(request, response);//输出验证码图片方法
            session = request.getSession();
//            System.out.println(session);
        }catch (Exception e){
            log.error("获取验证码失败>>>>   ", e);
        }

    }

    @PostMapping("regist")
    public Boolean regist(
            @NotBlank(message = "{required}") String username,
            @NotBlank(message = "{required}") String password,
            @NotBlank(message = "{required}") String randomvalidate) throws Exception {

            String inputStr = randomvalidate;
            String random = (String) session.getAttribute("RANDOMVALIDATECODEKEY");
            System.out.println(random);
            if(!inputStr.isEmpty()&&!random.isEmpty()&&inputStr.equals(random)){
                this.userService.regist(username,password);
//                User user = this.userService.findByName(username);
//
//                // 保存用户角色
//                String[] roles = user.getRoleId().split(StringPool.COMMA);
//                setUserRoles(user, roles);
//                // 创建用户默认的个性化配置
//                userConfigService.initDefaultUserConfig(String.valueOf(user.getUserId()));
//
//                // 将用户相关信息保存到 Redis中
//                userManager.loadUserRedisCache(user);
                return true;
            }
            return false;
    }

    private String saveTokenToRedis(User user, JWTToken token, HttpServletRequest request) throws Exception {
        String ip = IPUtil.getIpAddr(request);

        // 构建在线用户
        ActiveUser activeUser = new ActiveUser();
        activeUser.setUsername(user.getUsername());
        activeUser.setIp(ip);
        activeUser.setToken(token.getToken());
        activeUser.setLoginAddress(AddressUtil.getCityInfo(DbSearcher.BTREE_ALGORITHM, ip));

        // zset 存储登录用户，score 为过期时间戳
        this.redisService.zadd(FirstConstant.ACTIVE_USERS_ZSET_PREFIX, Double.valueOf(token.getExipreAt()), mapper.writeValueAsString(activeUser));
        // redis 中存储这个加密 token，key = 前缀 + 加密 token + .ip
        this.redisService.set(FirstConstant.TOKEN_CACHE_PREFIX + token.getToken() + StringPool.DOT + ip, token.getToken(), properties.getShiro().getJwtTimeOut() * 1000);

        return activeUser.getId();
    }

    /**
     * 生成前端需要的用户信息，包括：
     * 1. token
     * 2. Vue Router
     * 3. 用户角色
     * 4. 用户权限
     * 5. 前端系统个性化配置信息
     *
     * @param token token
     * @param user  用户信息
     * @return UserInfo
     */
    private Map<String, Object> generateUserInfo(JWTToken token, User user) {
        String username = user.getUsername();
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("token", token.getToken());
        userInfo.put("exipreTime", token.getExipreAt());

//        userInfo.put("dept",this.deptService.findByDeptId(user.getUserId()).getDeptName());

        Set<String> roles = this.userManager.getUserRoles(username);
        userInfo.put("roles", roles);
        StringBuilder sb = new StringBuilder();
        for(String role:roles){
            sb.append(role+" ");
        }
        Set<String> permissions = this.userManager.getUserPermissions(username);
        userInfo.put("permissions", permissions);

        UserConfig userConfig = this.userManager.getUserConfig(String.valueOf(user.getUserId()));
        userInfo.put("config", userConfig);

        user.setPassword("it's a secret");
        if(this.deptService.findByDeptId(user.getDeptId()) != null){
            user.setDeptName(this.deptService.findByDeptId(user.getDeptId()).getDeptName());
        }else {
            user.setDeptName("");
        }
        user.setRoleName(sb.toString().substring(0,sb.length()-1));
        userInfo.put("user", user);
        return userInfo;
    }
    private void setUserRoles(User user, String[] roles) {
        Arrays.stream(roles).forEach(roleId -> {
            UserRole ur = new UserRole();
            ur.setUserId(user.getUserId());
            ur.setRoleId(Long.valueOf(roleId));
            this.userRoleMapper.insert(ur);
        });
    }
}
