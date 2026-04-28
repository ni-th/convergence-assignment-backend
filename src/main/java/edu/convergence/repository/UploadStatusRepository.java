package edu.convergence.repository;

import edu.convergence.entity.upload.UploadStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UploadStatusRepository extends JpaRepository<UploadStatusEntity, Long> {
    Optional<UploadStatusEntity> findByUploadId(String uploadId);
}

