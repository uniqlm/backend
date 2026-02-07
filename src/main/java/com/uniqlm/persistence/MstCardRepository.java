package com.uniqlm.persistence;

import com.uniqlm.persistence.entity.MstCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MstCardRepository extends JpaRepository<MstCard, Integer> {
    List<MstCard> findByLessonId(Integer lessonId);
}