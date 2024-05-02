package com.moli.oauth2.auth.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.oauth2.auth.domain.SecurityUser;
import com.moli.oauth2.auth.domain.User;
import com.moli.oauth2.auth.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author moli
 * @time 2024-04-11 23:09:14
 * @description 用户数据库访问服务
 */
@Service
public class UserDetailServiceImpl implements UserDetailsService {

    private PasswordEncoder passwordEncoder;

    @Resource
    private UserMapper userMapper;

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // TODO 实际开发中，这里请修改从数据库中查询...
        User domain = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
        );
        if (domain == null) {
            throw new RuntimeException("用户不存在");
        }
        SecurityUser user = new SecurityUser();
        user.setUserName(username);
        user.setPassword(passwordEncoder.encode("123456"));
        return user;
    }
}
