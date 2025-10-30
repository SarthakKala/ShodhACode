package com.shodh.backend.repository;

import com.shodh.backend.model.Submission;
import com.shodh.backend.model.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByUserId(Long userId);
    List<Submission> findByProblemId(Long problemId);
    List<Submission> findByContestId(Long contestId);
    List<Submission> findByContestIdAndUserId(Long contestId, Long userId);
    List<Submission> findByStatus(SubmissionStatus status);
    
    @Query("SELECT s FROM Submission s WHERE s.contest.id = ?1 AND s.status = 'ACCEPTED' " +
           "GROUP BY s.user.id, s.problem.id ORDER BY s.submittedAt")
    List<Submission> findAcceptedSubmissionsByContest(Long contestId);
    
    @Query("SELECT s FROM Submission s WHERE s.contest.id = ?1 AND s.user.id = ?2 AND s.problem.id = ?3 " +
           "AND s.status = 'ACCEPTED' ORDER BY s.submittedAt ASC")
    List<Submission> findFirstAcceptedSubmission(Long contestId, Long userId, Long problemId);
}

