package com.shodh.backend.repository;

import com.shodh.backend.model.Contest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContestRepository extends JpaRepository<Contest, Long> {
    List<Contest> findByEndTimeAfter(LocalDateTime dateTime);
    List<Contest> findByStartTimeBeforeAndEndTimeAfter(LocalDateTime start, LocalDateTime end);
}

