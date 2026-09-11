package com.MyProject.mediationplatformrcehandler.model.database;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;

import java.time.Instant;

@Data
public abstract class AuditMetadata {

    @CreatedDate
    Instant createdDate;

    @LastModifiedDate
    Instant lastModifiedDate;

    @Version
    Long version;
}