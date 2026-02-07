package com.uniqlm.persistence.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "mst_course")
public class MstCourse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "TEXT") // Force TEXT type to avoid bytea
    private String path;

    @Column(columnDefinition = "TEXT") // Fixes the "lower(bytea)" error
    private String topic;

    @Column(length = 50) // Specific length helps PG identify it as VARCHAR
    private String mode;

    @Column(name = "generated_at")
    private Long generatedAt;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<MstWorld> worlds;

    @CreationTimestamp // Automatically handles initialization
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp // Automatically handles onUpdate logic
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}