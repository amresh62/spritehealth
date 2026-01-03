package com.spritehealth.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Random;

/**
 * Utility class to generate a sample Excel file with user data for testing.
 */
public class SampleDataGenerator {
    
    // Sample first names for random user generation
    private static final String[] FIRST_NAMES = {
        "John", "Jane", "Michael", "Emily", "David", "Sarah", "Robert", "Lisa",
        "William", "Jennifer", "James", "Linda", "Richard", "Patricia", "Joseph", "Elizabeth",
        "Thomas", "Mary", "Charles", "Susan", "Daniel", "Jessica", "Matthew", "Karen",
        "Christopher", "Nancy", "Andrew", "Betty", "Joshua", "Margaret"
    };
    
    // Sample last names for random user generation
    private static final String[] LAST_NAMES = {
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
        "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas",
        "Taylor", "Moore", "Jackson", "Martin", "Lee", "Perez", "Thompson", "White",
        "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson"
    };
    
    // Possible gender values
    private static final String[] GENDERS = {"Male", "Female", "Other"};
    
    // Sample city names for address generation
    private static final String[] CITIES = {
        "New York", "Los Angeles", "Chicago", "Houston", "Phoenix", "Philadelphia",
        "San Antonio", "San Diego", "Dallas", "San Jose"
    };
    
    // Random instance for generating random values
    private static final Random random = new Random();

    /**
     * Main method to generate the sample Excel file.
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        try {
            generateSampleExcel("sample_users.xlsx", 100);
            System.out.println("Sample Excel file generated successfully: sample_users.xlsx");
        } catch (IOException e) {
            System.err.println("Error generating sample file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generates an Excel file with random user data.
     * @param filename Name of the Excel file to create
     * @param recordCount Number of user records to generate
     * @throws IOException If an I/O error occurs
     */
    public static void generateSampleExcel(String filename, int recordCount) throws IOException {
        // Create a new workbook and sheet
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Users");

        // Create header row with column names
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Name", "DOB", "Email", "Password", "Phone", "Gender", "Address"};
        
        // Style for header cells (bold font)
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        
        // Set header cell values and styles
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Style for date cells (not used in this code, but created for possible future use)
        CellStyle dateStyle = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd"));

        // Generate random user data rows
        for (int i = 1; i <= recordCount; i++) {
            Row row = sheet.createRow(i);
            
            // Generate random first and last name
            String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
            String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
            String fullName = firstName + " " + lastName;
            
            // Name
            row.createCell(0).setCellValue(fullName);
            
            // Generate random date of birth between 1960 and 2002
            LocalDate dob = LocalDate.of(
                1960 + random.nextInt(43), // Year between 1960-2002
                1 + random.nextInt(12),    // Month 1-12
                1 + random.nextInt(28)     // Day 1-28
            );
            Cell dobCell = row.createCell(1);
            dobCell.setCellValue(dob.toString());
            
            // Generate random email address
            String email = firstName.toLowerCase() + "." + lastName.toLowerCase() + 
                          random.nextInt(1000) + "@example.com";
            row.createCell(2).setCellValue(email);
            
            // Generate random password
            String password = "Password" + (1000 + random.nextInt(9000));
            row.createCell(3).setCellValue(password);
            
            // Generate random phone number in (XXX) XXX-XXXX format
            String phone = String.format("(%03d) %03d-%04d",
                200 + random.nextInt(800),
                200 + random.nextInt(800),
                random.nextInt(10000));
            row.createCell(4).setCellValue(phone);
            
            // Randomly select gender
            String gender = GENDERS[random.nextInt(GENDERS.length)];
            row.createCell(5).setCellValue(gender);
            
            // Generate random address
            String address = (1 + random.nextInt(9999)) + " " +
                           LAST_NAMES[random.nextInt(LAST_NAMES.length)] + " St, " +
                           CITIES[random.nextInt(CITIES.length)] + ", " +
                           "NY " + (10000 + random.nextInt(90000));
            row.createCell(6).setCellValue(address);
        }

        // Auto-size all columns for better readability
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write the workbook to the specified file
        try (FileOutputStream fileOut = new FileOutputStream(filename)) {
            workbook.write(fileOut);
        }

        // Close the workbook to free resources
        workbook.close();
    }
}
