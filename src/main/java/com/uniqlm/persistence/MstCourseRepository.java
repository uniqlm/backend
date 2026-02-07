package com.uniqlm.persistence; // MUST MATCH THE FOLDER

import com.uniqlm.persistence.entity.MstCourse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MstCourseRepository extends JpaRepository<MstCourse, Integer> {

    @Query("SELECT c FROM MstCourse c WHERE " +
            "(:topic IS NULL OR LOWER(CAST(c.topic as string)) LIKE LOWER(CONCAT('%', CAST(:topic as string), '%'))) AND " +
            "(:mode IS NULL OR c.mode = :mode)")
    Page<MstCourse> searchCourses(@Param("topic") String topic,
                                  @Param("mode") String mode,
                                  Pageable pageable);
}