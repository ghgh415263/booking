package com.example.booking.domain;

import java.util.Optional;

public interface MemberPointDao {

    Optional<MemberPointSummary> findByMemberId(long id);
}
