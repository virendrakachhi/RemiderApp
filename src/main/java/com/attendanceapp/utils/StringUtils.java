package com.attendanceapp.utils;

import com.attendanceapp.models.Student;
import com.attendanceapp.models.User;

import java.util.Iterator;
import java.util.List;

public final class StringUtils {

    public static String getAllIdsFromStudentList(List<? extends User> studentList, char separator) {

        StringBuilder bodyBuilder = new StringBuilder();
        Iterator<? extends User> iterator = studentList.iterator();

        while (iterator.hasNext()) {
            User student = iterator.next();
            bodyBuilder.append(student.getUserId());

            if (iterator.hasNext()) {
                bodyBuilder.append(separator);
            }
        }

        return bodyBuilder.toString();
    }

    public static String getAllEmailsFromStudentList(List<? extends User> studentList, char separator) {

        StringBuilder bodyBuilder = new StringBuilder();
        Iterator<? extends User> iterator = studentList.iterator();

        while (iterator.hasNext()) {
            User student = iterator.next();
            bodyBuilder.append(student.getEmail());

            if (iterator.hasNext()) {
                bodyBuilder.append(separator);
            }
        }

        return bodyBuilder.toString();
    }

    public static String changeFirstLetterToUppercase(String textToChange) {
        if (textToChange == null) {
            return "";
        }
        if (textToChange.length() == 1) {
            return textToChange.toUpperCase();
        }
        textToChange = (String.valueOf(textToChange.charAt(0)).toUpperCase()) + textToChange.substring(1).toLowerCase();
        return textToChange;
    }

}
