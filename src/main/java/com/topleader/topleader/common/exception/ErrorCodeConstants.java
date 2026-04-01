package com.topleader.topleader.common.exception;

/**
 * @author Daniel Slavik
 */
public final class ErrorCodeConstants {
    private ErrorCodeConstants() {
        //util class
    }

    public static final String FIELD_OUTSIDE_OF_FRAME = "field.outside.of.frame";
    public static final String MORE_THEN_24_EVENT = "event.longer.that.24";

    public static final String SESSION_IN_PAST = "session.in.past";
    public static final String TIME_NOT_AVAILABLE = "time.not.available";

    public static final String NOT_ENOUGH_CREDITS = "not.enough.credits";

    public static final String EMAIL_USED = "email.used";
    public static final String NOT_PART_OF_COMPANY = "not.part.of.company";

    public static final String INVALID_PASSWORD = "invalid.password";

    public static final String ALREADY_EXISTING = "already.existing";

    public static final String NOT_EMPTY = "field.cannot.be.empty";

    public static final String DIFFERENT_COACH_NOT_PERMITTED = "different.coach.not.permitted";

    public static final String FROM_ALREADY_SUBMITTED = "form.already.submitted";

    public static final String USER_NOT_FOUND = "user.not.found";
    public static final String UNABLE_TO_DELETE = "unable.to.delete";
    public static final String USER_NO_AUTHORIZED = "user.not.authorized";
    public static final String ALLOCATION_ALREADY_EXISTS = "allocation.already.exists";
    public static final String CAPACITY_EXCEEDED = "capacity.exceeded";
    public static final String PACKAGE_INACTIVE = "package.inactive";
    public static final String ALLOCATED_BELOW_CONSUMED = "allocation.below.consumed";
    public static final String NO_UNITS_AVAILABLE = "no.units.available";
    public static final String SESSION_CANCEL_TOO_LATE = "session.cancel.too.late";
    public static final String INVALID_PARAMETER = "invalid.parameter";

    public static final String PROGRAM_GOAL_REQUIRED = "program.goal.required";
    public static final String COACH_MODEL_REQUIRED = "program.coach.model.required";
    public static final String PARTICIPANTS_REQUIRED = "program.participants.required";
    public static final String PARTICIPANTS_NOT_FOUND = "program.participants.not.found";
    public static final String PARTICIPANT_ALREADY_IN_PROGRAM = "program.participant.already.in.program";

    public static final String PARTICIPANT_ENROLL_INVALID_STATUS = "participant.enroll.invalid.status";
    public static final String PARTICIPANT_ENROLL_FOCUS_AREA_INVALID = "participant.enroll.focus.area.invalid";
    public static final String PARTICIPANT_BASELINE_INVALID_STATUS = "participant.baseline.invalid.status";
    public static final String PARTICIPANT_CHECKIN_INVALID_STATUS = "participant.checkin.invalid.status";
    public static final String PARTICIPANT_MID_CYCLE_NOT_DUE = "participant.mid.cycle.not.due";
    public static final String PARTICIPANT_MID_CYCLE_ALREADY_COMPLETED = "participant.mid.cycle.already.completed";
    public static final String PARTICIPANT_MID_CYCLE_NO_BASELINE = "participant.mid.cycle.no.baseline";
    public static final String PARTICIPANT_FINAL_INVALID_STATUS = "participant.final.invalid.status";
    public static final String PARTICIPANT_FINAL_NOT_DUE = "participant.final.not.due";
    public static final String PARTICIPANT_FINAL_ALREADY_COMPLETED = "participant.final.already.completed";
    public static final String PARTICIPANT_FINAL_NPS_REQUIRED = "participant.final.nps.required";
    public static final String PARTICIPANT_GOAL_INVALID_STATUS = "participant.goal.invalid.status";
    public static final String ASSESSMENT_QUESTIONS_INCOMPLETE = "assessment.questions.incomplete";

}
