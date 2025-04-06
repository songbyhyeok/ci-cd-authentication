package com.project.cicd_auth.repository;

import com.project.cicd_auth.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Integer> {
    Boolean existsByUsername(String username);

    Member findByUsername(String username);
}
