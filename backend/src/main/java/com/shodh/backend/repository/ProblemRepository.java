package com.shodh.backend.repository;

import com.shodh.backend.model.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {
    List<Problem> findByContestId(Long contestId);
    
    @Query("SELECT p FROM Problem p LEFT JOIN FETCH p.testCases WHERE p.id = ?1")
    Problem findByIdWithTestCases(Long id);
}

