package com.spritehealth.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Random;

/**
 * Utility class to generate sample Excel file with user data for testing
 */
public class SampleDataGenerator {
    
    private static final String[] FIRST_NAMES = {
        "John", "Jane", "Michael", "Emily", "David", "Sarah", "Robert", "Lisa",
        "William", "Jennifer", "James", "Linda", "Richard", "Patricia", "Joseph", "Elizabeth",
        "Thomas", "Mary", "Charles", "Susan", "Daniel", "Jessica", "Matthew", "Karen",
        "Christopher", "Nancy", "Andrew", "Betty", "Joshua", "Margaret"
    };
    
    private static final String[] LAST_NAMES = {
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
        "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas",
        "Taylor", "Moore", "Jackson", "Martin", "Lee", "Perez", "Thompson", "White",
        "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson"
    };
    
    private static final String[] GENDERS = {"Male", "Female", "Other"};
    
    private static final String[] CITIES = {
        "New York", "Los Angeles", "Chicago", "Houston", "Phoenix", "Philadelphia",
        "San Antonio", "San Diego", "Dallas", "San Jose"
    };
    
    private static final Random random = new Random();

    public static void main(String[] args) {
        try {
            generateSampleExcel("sample_users.xlsx", 100);
            System.out.println("Sample Excel file generated successfully: sample_users.xlsx");
        } catch (IOException e) {
            System.err.println("Error generating sample file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void generateSampleExcel(String filename, int recordCount) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Users");

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Name", "DOB", "Email", "Password", "Phone", "Gender", "Address"};
        
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Create data rows
        CellStyle dateStyle = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd"));

        for (int i = 1; i <= recordCount; i++) {
            Row row = sheet.createRow(i);
            
            String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
            String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
            String fullName = firstName + " " + lastName;
            
            // Name
            row.createCell(0).setCellValue(fullName);
            
            // DOB
            LocalDate dob = LocalDate.of(
                1960 + random.nextInt(43), // Year between 1960-2002
                1 + random.nextInt(12),    // Month 1-12
                1 + random.nextInt(28)     // Day 1-28
            );
            Cell dobCell = row.createCell(1);
            dobCell.setCellValue(dob.toString());
            
            // Email
            String email = firstName.toLowerCase() + "." + lastName.toLowerCase() + 
                          random.nextInt(1000) + "@example.com";
            row.createCell(2).setCellValue(email);
            
            // Password
            String password = "Password" + (1000 + random.nextInt(9000));
            row.createCell(3).setCellValue(password);
            
            // Phone
            String phone = String.format("(%03d) %03d-%04d",
                200 + random.nextInt(800),
                200 + random.nextInt(800),
                random.nextInt(10000));
            row.createCell(4).setCellValue(phone);
            
            // Gender
            String gender = GENDERS[random.nextInt(GENDERS.length)];
            row.createCell(5).setCellValue(gender);
            
            // Address
            String address = (1 + random.nextInt(9999)) + " " +
                           LAST_NAMES[random.nextInt(LAST_NAMES.length)] + " St, " +
                           CITIES[random.nextInt(CITIES.length)] + ", " +
                           "NY " + (10000 + random.nextInt(90000));
            row.createCell(6).setCellValue(address);
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write to file
        try (FileOutputStream fileOut = new FileOutputStream(filename)) {
            workbook.write(fileOut);
        }

        workbook.close();
    }
}
