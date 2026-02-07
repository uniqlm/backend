package com.uniqlm.service.dto;

import lombok.Data;

import java.util.List;

@Data
public class CourseResponse {
    private Integer id;
    private String topic;
    private String path;
    private String mode;
    private Long generatedAt;
    private List<WorldDTO> worlds;

    @Data
    public static class WorldDTO {
        private String title;
        private String description;
        private List<LessonDTO> lessons;
    }

    @Data
    public static class LessonDTO {
        private String title;
        private String status;
    }
}
