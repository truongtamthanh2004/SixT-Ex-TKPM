package com.example.sixt.helpers;

import com.example.sixt.models.StudentEntity;
import org.apache.commons.csv.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CsvHelper {
    public static String studentsToCsv(List<StudentEntity> students) {
        final StringWriter out = new StringWriter();
        try (CSVPrinter csvPrinter = new CSVPrinter(out, CSVFormat.DEFAULT
                .withHeader("ID", "StudentID", "FullName", "Email", "Faculty", "Status"))) {
            for (StudentEntity student : students) {
                csvPrinter.printRecord(
                        student.getId(),
                        student.getStudentId(),
                        student.getFullName(),
                        student.getEmail(),
                        student.getDepartment(),
                        student.getStatus()
                );
            }
        } catch (IOException e) {
            throw new RuntimeException("Error writing CSV", e);
        }
        return out.toString();
    }
}
