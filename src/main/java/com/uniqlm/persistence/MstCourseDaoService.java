package com.uniqlm.persistence;

import com.uniqlm.persistence.entity.MstCard;
import com.uniqlm.persistence.entity.MstCourse;
import com.uniqlm.persistence.entity.MstLesson;
import com.uniqlm.persistence.entity.MstWorld;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MstCourseDaoService {

    private final MstCourseRepository courseRepository;
    private final MstWorldRepository worldRepository;
    private final MstLessonRepository lessonRepository;
    private final MstCardRepository cardRepository;

    @Transactional(readOnly = true)
    public List<MstCourse> getAllCourses() {
        return courseRepository.findAll();
    }

    public MstCourse getCourseById(Integer id) {
        return courseRepository.findById(id).orElse(null);
    }

    @Transactional
    public MstCourse saveCourse(MstCourse course) {
        return courseRepository.save(course);
    }

    @Transactional
    public void deleteCourse(Integer id) {
        courseRepository.deleteById(id);
    }

    public List<MstWorld> getWorldsByCourse(Integer courseId) {
        return worldRepository.findByCourseId(courseId);
    }

    public List<MstLesson> getLessonsByWorld(Integer worldId) {
        return lessonRepository.findByWorldId(worldId);
    }

    public List<MstCard> getCardsByLesson(Integer lessonId) {
        return cardRepository.findByLessonId(lessonId);
    }
}