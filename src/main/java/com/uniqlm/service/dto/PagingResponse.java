package com.uniqlm.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Schema(description = "Standard Paginated Response Wrapper")
public class PagingResponse<T> {
    @Schema(description = "List of items for the current page")
    private List<T> data;

    @Schema(example = "0")
    private int page;

    @Schema(example = "10")
    private int size;

    @Schema(example = "100")
    private long totalElements;

    @Schema(example = "10")
    private int totalPages;

    @Schema(example = "true")
    private boolean last;

    public PagingResponse(Page<T> page) {
        this.data = page.getContent();
        this.page = page.getNumber();
        this.size = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.last = page.isLast();
    }
}