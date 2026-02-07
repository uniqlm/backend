package com.uniqlm.service.dto;

import lombok.Data;

@Data
public class CourseRequest {
    private String query;
    private int page = 0;
    private int size = 10;
}
