package com.uniqlm.controller;

import com.uniqlm.persistence.entity.MstCourse;
import com.uniqlm.service.CourseService;
import com.uniqlm.service.dto.PagingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Course Controller", description = "Endpoints for Master Course Data")
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "Get Courses", description = "Paginated list of courses with topic and mode filters")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping
    public ResponseEntity<PagingResponse<MstCourse>> listCourses(
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String mode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<MstCourse> coursePage = courseService.getCoursesWithFilter(topic, mode, page, size);
        return ResponseEntity.ok(new PagingResponse<>(coursePage));
    }

    @Operation(summary = "Create Course")
    @PostMapping
    public ResponseEntity<MstCourse> create(@RequestBody MstCourse course) {
        return ResponseEntity.ok(courseService.createCourse(course));
    }

    @Operation(summary = "Update Course")
    @PutMapping("/{id}")
    public ResponseEntity<MstCourse> update(@PathVariable Integer id, @RequestBody MstCourse course) {
        return ResponseEntity.ok(courseService.updateCourse(id, course));
    }

    @Operation(summary = "Delete Course")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
}