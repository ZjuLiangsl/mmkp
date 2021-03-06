package org.platform.modules.system.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.base.service.BaseCommonService;
import org.platform.modules.system.service.ISysUserService;
import org.platform.modules.system.vo.SysUserOnlineVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.extern.slf4j.Slf4j;

/**
 * @Author: chenli
 * @Date: 2020-06-07
 * @Version: V1.0
 */
@RestController
@RequestMapping("/sys/online")
@Slf4j
public class SysUserOnlineController {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    public RedisTemplate redisTemplate;

    @Autowired
    public ISysUserService userService;

    @Autowired
    private ISysBaseAPI sysBaseAPI;

    @Resource
    private BaseCommonService baseCommonService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Result<Page<SysUserOnlineVO>> list(@RequestParam(name="username", required=false) String username, @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                              @RequestParam(name="pageSize", defaultValue="10") Integer pageSize) {
        Collection<String> keys = redisTemplate.keys(CommonConstant.PREFIX_USER_TOKEN + "*");
        SysUserOnlineVO online;
        List<SysUserOnlineVO> onlineList = new ArrayList<SysUserOnlineVO>();
        for (String key : keys) {
            online = new SysUserOnlineVO();
            String token = (String) redisUtil.get(key);
            if (!StringUtils.isEmpty(token)){
                online.setToken(token);
                LoginUser loginUser = sysBaseAPI.getUserByName(JwtUtil.getUsername(token));
                BeanUtils.copyProperties(loginUser, online);
                if (StringUtils.isNotEmpty(username)) {
                    if (StringUtils.equals(username, online.getUsername())) {
                        onlineList.add(online);
                    }
                } else {
                    onlineList.add(online);
                }
            }
        }

        Page<SysUserOnlineVO> page = new Page<SysUserOnlineVO>(pageNo, pageSize);
        int count = onlineList.size();
        List<SysUserOnlineVO> pages = new ArrayList<>();
        int currId = pageNo>1 ? (pageNo-1)*pageSize:0;
        for (int i=0; i<pageSize && i<count - currId;i++){
            pages.add(onlineList.get(currId+i));
        }
        page.setSize(pageSize);
        page.setCurrent(pageNo);
        page.setTotal(count);
        //?????????????????????
        page.setPages(count %10 == 0 ? count/10 :count/10+1);
        page.setRecords(pages);

        Collections.reverse(onlineList);
        onlineList.removeAll(Collections.singleton(null));
        Result<Page<SysUserOnlineVO>> result = new Result<Page<SysUserOnlineVO>>();
        result.setSuccess(true);
        result.setResult(page);
        return result;
    }

    /**
     * ????????????
     */
    @RequestMapping(value = "/forceLogout",method = RequestMethod.POST)
    public Result<Object> forceLogout(@RequestBody SysUserOnlineVO online) {
        //??????????????????
        if(oConvertUtils.isEmpty(online.getToken())) {
            return Result.error("?????????????????????");
        }
        String username = JwtUtil.getUsername(online.getToken());
        LoginUser sysUser = sysBaseAPI.getUserByName(username);
        if(sysUser!=null) {
            baseCommonService.addLog("??????: "+sysUser.getRealname()+"???????????????", CommonConstant.LOG_TYPE_1, null,sysUser);
            log.info(" ??????  "+sysUser.getRealname()+"??????????????? ");
            //??????????????????Token??????
            redisUtil.del(CommonConstant.PREFIX_USER_TOKEN + online.getToken());
            //??????????????????Shiro????????????
            redisUtil.del(CommonConstant.PREFIX_USER_SHIRO_CACHE + sysUser.getId());
            //????????????????????????????????????????????????????????????sys:cache:user::<username>
            redisUtil.del(String.format("%s::%s", CacheConstant.SYS_USERS_CACHE, sysUser.getUsername()));
            //??????shiro???logout
            SecurityUtils.getSubject().logout();
            return Result.ok("?????????????????????");
        }else {
            return Result.error("Token??????!");
        }
    }
}
