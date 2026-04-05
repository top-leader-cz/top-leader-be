package com.topleader.topleader.common.meeting;

import java.util.Optional;

import com.topleader.topleader.common.meeting.domain.MeetingInfo;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface MeetingInfoRepository extends CrudRepository<MeetingInfo, Long> {

    Optional<MeetingInfo> findByUsername(String username);

    @Modifying
    @Query("DELETE FROM meeting_info WHERE username = :username")
    void deleteByUsername(String username);

    @Modifying
    @Query("UPDATE meeting_info SET auto_generate = :autoGenerate WHERE username = :username")
    void updateAutoGenerate(String username, boolean autoGenerate);

    @Modifying
    @Query("UPDATE meeting_info SET status = :status WHERE username = :username")
    void updateStatus(String username, String status);

    @Modifying
    @Query("UPDATE meeting_info SET refresh_token = :refreshToken, access_token = :accessToken WHERE username = :username")
    void updateTokens(String username, String refreshToken, String accessToken);

    @Modifying
    @Query("UPDATE meeting_info SET email = :email WHERE username = :username")
    void updateEmail(String username, String email);

    @Modifying
    @Query("""
            INSERT INTO meeting_info (username, provider, refresh_token, access_token, email, auto_generate, status, created_at)
            VALUES (:username, :provider, :refreshToken, :accessToken, :email, true, 'OK', now())
            ON CONFLICT (username) DO UPDATE SET
                provider = :provider, refresh_token = :refreshToken,
                access_token = :accessToken, email = :email,
                auto_generate = true, status = 'OK', created_at = now()
            """)
    void upsertConnection(String username, String provider, String refreshToken, String accessToken, String email);
}
