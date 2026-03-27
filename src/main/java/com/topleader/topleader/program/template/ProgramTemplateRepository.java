package com.topleader.topleader.program.template;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface ProgramTemplateRepository extends ListCrudRepository<ProgramTemplate, Long> {

    @Query("""
            SELECT * FROM program_template
            WHERE active = true
              AND (company_id IS NULL OR company_id = :companyId)
            ORDER BY company_id NULLS FIRST, name
            """)
    List<ProgramTemplate> findAvailable(Long companyId);
}
