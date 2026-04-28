package edu.convergence.entity.upload;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "upload_status")
public class UploadStatusEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String uploadId;

    @Column(nullable = false)
    private String status; // PENDING, IN_PROGRESS, COMPLETED, FAILED

    private Integer totalRecords;
    private Integer processedRecords;
    private String errorMessage;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;
}

