package com.fatigue.detection.repository;

import com.fatigue.detection.entity.DetectionRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface DetectionRecordRepository extends JpaRepository<DetectionRecord, Long> {

    Optional<DetectionRecord> findByRecordId(String recordId);

    List<DetectionRecord> findByUserIdOrderByCreatedAtDesc(String userId);

    Page<DetectionRecord> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    @Query("SELECT r FROM DetectionRecord r WHERE r.createdAt BETWEEN ?1 AND ?2 ORDER BY r.createdAt DESC")
    List<DetectionRecord> findByTimeRange(LocalDateTime start, LocalDateTime end);

    @Query("SELECT r.resultClass, COUNT(r) FROM DetectionRecord r WHERE r.createdAt >= ?1 GROUP BY r.resultClass")
    List<Object[]> countByResultClassSince(LocalDateTime since);

    // 统计最近N次的疲劳率
    @Query(value = "SELECT COUNT(*) * 1.0 / (SELECT COUNT(*) FROM detection_records WHERE user_id = ?1) " +
            "FROM detection_records WHERE user_id = ?1 AND result_class = 'drowsy'", nativeQuery = true)
    Double calculateDrowsyRate(String userId);

    @Query(value = "SELECT result_class, COUNT(*) as count FROM detection_records " +
            "WHERE user_id = ?1 AND created_at >= datetime('now', '-' || ?2 || ' days') " +
            "GROUP BY result_class", nativeQuery = true)
    List<Map<String, Object>> countByResultClassAndTime(String userId, int days);
}