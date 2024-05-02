package com.moli.oauth2.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moli.oauth2.auth.domain.User;
import org.springframework.stereotype.Repository;

/**
 * @author moli
 * @time 2024-04-12 16:54:55
 * @description 用户mapper
 */
@Repository
public interface UserMapper extends BaseMapper<User> {
}
