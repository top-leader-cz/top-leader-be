package com.topleader.topleader.program.participant.assessment;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface AssessmentQuestionRepository extends ListCrudRepository<AssessmentQuestion, Long> {

    @Query("SELECT * FROM assessment_question WHERE focus_area_key = :focusAreaKey ORDER BY question_order")
    List<AssessmentQuestion> findByFocusAreaKey(String focusAreaKey);
}
