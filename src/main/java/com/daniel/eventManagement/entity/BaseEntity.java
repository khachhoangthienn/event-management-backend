package com.daniel.eventManagement.entity;

import jakarta.persistence.MappedSuperclass;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@FieldDefaults(level = AccessLevel.PRIVATE)
@MappedSuperclass
@Data
public abstract class BaseEntity {
    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;
}
