package com.uniqlm.service;

import com.uniqlm.persistence.MstCourseDaoService;
import com.uniqlm.persistence.MstCourseRepository;
import com.uniqlm.persistence.entity.MstCourse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseService {

    private final MstCourseDaoService courseDaoService;
    private final MstCourseRepository courseRepository;

    public Page<MstCourse> getCoursesWithFilter(String topic, String mode, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        // Uses the custom @Query search we defined in the Repository
        return courseRepository.searchCourses(topic, mode, pageRequest);
    }

    @Transactional
    public MstCourse createCourse(MstCourse course) {
        log.info("Saving new course structure for topic: {}", course.getTopic());

        // Bidirectional mapping for Worlds -> Course
        if (course.getWorlds() != null) {
            course.getWorlds().forEach(world -> {
                world.setCourse(course);
                // Bidirectional mapping for Lessons -> World
                if (world.getLessons() != null) {
                    world.getLessons().forEach(lesson -> {
                        lesson.setWorld(world);
                        // Bidirectional mapping for Cards -> Lesson
                        if (lesson.getCards() != null) {
                            lesson.getCards().forEach(card -> card.setLesson(lesson));
                        }
                    });
                }
            });
        }
        return courseDaoService.saveCourse(course);
    }

    @Transactional
    public MstCourse updateCourse(Integer id, MstCourse updatedData) {
        MstCourse existing = courseDaoService.getCourseById(id);
        if (existing == null) throw new RuntimeException("Course not found with id: " + id);

        existing.setTopic(updatedData.getTopic());
        existing.setPath(updatedData.getPath());
        existing.setMode(updatedData.getMode());

        return courseDaoService.saveCourse(existing);
    }

    @Transactional
    public void deleteCourse(Integer id) {
        courseDaoService.deleteCourse(id);
    }
}