package com.artinus.subscription.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.artinus.subscription.api.entity.CellPhoneNumber;
import com.artinus.subscription.api.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByCellPhoneNumber(CellPhoneNumber cellPhoneNumber);
}
