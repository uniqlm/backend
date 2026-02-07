package com.uniqlm.persistence;

import com.uniqlm.persistence.entity.MstWorld;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MstWorldRepository extends JpaRepository<MstWorld, Integer> {
    List<MstWorld> findByCourseId(Integer courseId);
}