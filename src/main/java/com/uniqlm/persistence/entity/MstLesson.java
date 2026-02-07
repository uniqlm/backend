package com.uniqlm.persistence.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "mst_lesson")
public class MstLesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "world_id", nullable = false)
    @JsonBackReference // Prevents infinite loop back to World
    private MstWorld world;

    private String title;

    @Column(name = "concept_title")
    private String conceptTitle;

    @Column(name = "concept_id")
    private String conceptId;

    private String status;

    @Column(name = "is_audio")
    private Boolean isAudio = false;

    @Column(name = "is_review")
    private Boolean isReview = false;

    @Column(name = "is_final_exam")
    private Boolean isFinalExam = false;

    private Integer phase;

    @Column(name = "is_partial")
    private Boolean isPartial = false;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference // Includes cards in the JSON
    private List<MstCard> cards;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}