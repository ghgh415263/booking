package com.example.booking.infra;

import com.example.booking.domain.MemberPointDao;
import com.example.booking.domain.MemberPointSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcMemberPointDao implements MemberPointDao {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<MemberPointSummary> findByMemberId(long memberId) {
        return Optional.ofNullable(
                jdbcTemplate.queryForObject("""
                        SELECT (balance - reserved) as point
                        FROM member_point
                        WHERE member_id = ?
                        """,
                        (rs, rowNum) -> new MemberPointSummary(rs.getBigDecimal("point")),
                        memberId
                )
        );
    }
}