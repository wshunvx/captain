package com.netflix.eureka.http.slots.block;

public enum RollingParamEvent {
    /**
     * Indicates that the request successfully passed the slot chain (entry).
     */
    REQUEST_PASSED,
    /**
     * Indicates that the request is blocked by a specific slot.
     */
    REQUEST_BLOCKED
}
