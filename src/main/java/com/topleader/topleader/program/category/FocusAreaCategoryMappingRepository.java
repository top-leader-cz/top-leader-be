package com.topleader.topleader.program.category;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface FocusAreaCategoryMappingRepository extends ListCrudRepository<FocusAreaCategoryMapping, String> {

    @Query("SELECT * FROM focus_area_category_mapping WHERE focus_area_key IN (:focusAreaKeys)")
    List<FocusAreaCategoryMapping> findByFocusAreaKeyIn(Iterable<String> focusAreaKeys);
}
