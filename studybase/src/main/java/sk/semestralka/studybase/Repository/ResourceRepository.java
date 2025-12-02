package sk.semestralka.studybase.Repository;

import sk.semestralka.studybase.Entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    List<Resource> findByGroupId(Long groupId);
    List<Resource> findByUploadedBy(Long uploadedBy);

    // Pridané metódy pre analýzu
    long countByGroupIdAndUploadedBy(Long groupId, Long uploadedBy);
    long countByUploadedBy(Long uploadedBy);
}