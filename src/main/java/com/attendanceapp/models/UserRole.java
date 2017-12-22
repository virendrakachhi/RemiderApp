package com.attendanceapp.models;

public enum UserRole {
    Teacher(1),
    Student(2),
    Parent(3),
    Manager(4),
    Employee(5),
    EventHost(6),
    Attendee(7),
    ManagerHC(10),
    EmployeeHC(11),
    Family(12);


    private int role;

    UserRole(int role) {
        this.role = role;
    }

    public int getRole() {
        return role;
    }

    public static UserRole valueOf(int userRole) {

        for (UserRole role : UserRole.values()) {
            if (role.getRole() == userRole) {
                return role;
            }
        }

        return null;
    }
}
