package com.uniqlm.persistence;

import com.uniqlm.persistence.entity.MstLesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MstLessonRepository extends JpaRepository<MstLesson, Integer> {
    List<MstLesson> findByWorldId(Integer worldId);
}