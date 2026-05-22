package com.fatigue.detection.repository;

import com.fatigue.detection.entity.VideoDetection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideoDetectionRepository extends JpaRepository<VideoDetection, Long> {

    Optional<VideoDetection> findByVideoId(String videoId);
}
