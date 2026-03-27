package com.topleader.topleader.program.category;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@Table("focus_area_category_mapping")
public class FocusAreaCategoryMapping {

    private String focusAreaKey;

    private String categoryKey;
}
