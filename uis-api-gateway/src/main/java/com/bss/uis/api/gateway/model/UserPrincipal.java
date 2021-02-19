package com.bss.uis.api.gateway.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserPrincipal implements UserDetails {

	private static final long serialVersionUID = 1L;
	
	private User mUser;
    
	public UserPrincipal() {
    }
    
	public UserPrincipal(User user) {
        mUser = user;
    }
    
	@Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
		List<GrantedAuthority> list = new ArrayList<>();
        list.add(new SimpleGrantedAuthority(mUser.getUserRole()));
        return list;
    }

    @Override
    public String getPassword() {
        return mUser.getPassword();
    }
    @Override
    public String getUsername() {
        return mUser.getEmail();
    }
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override
    public boolean isEnabled() {
        return true;
    }
}
