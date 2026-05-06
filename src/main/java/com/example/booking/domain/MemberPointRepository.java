package com.example.booking.domain;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface MemberPointRepository extends JpaRepository<MemberPoint, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<MemberPoint> findByMemberId(long memberId);


}
