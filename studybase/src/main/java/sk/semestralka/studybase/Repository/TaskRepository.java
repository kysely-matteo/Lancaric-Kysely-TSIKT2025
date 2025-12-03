package sk.semestralka.studybase.Repository;

import sk.semestralka.studybase.Entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByGroupId(Long groupId);
    List<Task> findByGroupIdAndStatus(Long groupId, String status);
    List<Task> findByCreatedBy(Long createdBy);
    List<Task> findByDeadlineBeforeAndStatusNot(LocalDateTime deadline, String status);

    // Pridané metódy pre analýzu
    long countByGroupIdAndCreatedBy(Long groupId, Long createdBy);
    long countByGroupIdAndCreatedByAndStatus(Long groupId, Long createdBy, String status);
}