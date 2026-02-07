package com.uniqlm.persistence.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "mst_world")
public class MstWorld {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @JsonBackReference // Prevents infinite loop back to Course
    private MstCourse course;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "theme_color")
    private String themeColor;

    @Column(name = "is_locked")
    private Boolean isLocked = true;

    @Column(name = "is_expanded")
    private Boolean isExpanded = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "final_exam_json")
    private String finalExamJson;

    @OneToMany(mappedBy = "world", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference // Includes lessons in the JSON
    private List<MstLesson> lessons;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}