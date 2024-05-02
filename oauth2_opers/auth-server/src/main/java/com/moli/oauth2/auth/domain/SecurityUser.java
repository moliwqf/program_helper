package com.moli.oauth2.auth.domain;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author moli
 * @time 2024-04-11 23:08:11
 * @description 安全认证用户信息
 */
@Data
public class SecurityUser implements Serializable, UserDetails {
    private static final long serialVersionUID = 5538522337801286424L;

    private String userName;
    private String password;
    private Set<SimpleGrantedAuthority> authorities;

    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (CollectionUtils.isEmpty(authorities)) {
            authorities = new HashSet<>();
            authorities.add(new SimpleGrantedAuthority("admin"));
        }
        return this.authorities;
    }

    public String getPassword() {
        return this.password;
    }

    public String getUsername() {
        return this.userName;
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isEnabled() {
        return true;
    }
}
