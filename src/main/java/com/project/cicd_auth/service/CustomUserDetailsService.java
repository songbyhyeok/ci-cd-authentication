package com.project.cicd_auth.service;

import com.project.cicd_auth.dto.CustomUserDetails;
import com.project.cicd_auth.entity.Member;
import com.project.cicd_auth.repository.MemberRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final MemberRepository userRepository;

    public CustomUserDetailsService(MemberRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = userRepository.findByUsername(username);
        if (member != null) {
            return new CustomUserDetails(member);
        }

        return null;
    }
}
