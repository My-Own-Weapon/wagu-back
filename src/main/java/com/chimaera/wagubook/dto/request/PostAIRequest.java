package com.chimaera.wagubook.dto.request;

import com.chimaera.wagubook.entity.Category;
import lombok.Data;

@Data
public class PostAIRequest {
    private Category postCategory;
    private String menuName;
}
