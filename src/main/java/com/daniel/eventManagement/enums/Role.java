package com.daniel.eventManagement.enums;

public enum Role {
    ADMIN,
    ORGANIZER,
    ATTENDEE;

    public static boolean isValidRole(String role) {
        for (Role r : Role.values()) {
            if (r.name().equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }
}
