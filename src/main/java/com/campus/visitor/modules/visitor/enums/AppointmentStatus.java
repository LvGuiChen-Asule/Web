package com.campus.visitor.modules.visitor.enums;

import java.util.Set;

public enum AppointmentStatus {
    PENDING,
    FIRST_APPROVED,
    APPROVED,
    REJECTED,
    CANCELLED,
    CHECKED_IN,
    CHECKED_OUT;

    public static final Set<String> VALID_FOR_LIMIT = Set.of(PENDING.name(), FIRST_APPROVED.name(), APPROVED.name());
}
