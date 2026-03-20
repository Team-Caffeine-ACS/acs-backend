package com.caffeine.acs_backend.enums;

public enum VisitStatus {
    /** Visitor submitted intent to visit; not yet arrived */
    PRE_REGISTERED,

    /** Visitor has arrived; keycard issued */
    ACTIVE,

    /** Visitor has left; keycard released, exit time recorded */
    COMPLETED,

    /** Visit was cancelled before arrival */
    CANCELLED
}
