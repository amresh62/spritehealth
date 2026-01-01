package com.spritehealth.servlets;

import com.google.gson.Gson;
import com.spritehealth.models.User;
import com.spritehealth.services.interfaces.IUserDatastoreService;
import com.spritehealth.services.impl.InMemoryDatastoreServiceImpl;
import com.spritehealth.utils.GsonProvider;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MultipartConfig(maxFileSize = 10 * 1024 * 1024, // 10MB
        maxRequestSize = 20 * 1024 * 1024 // 20MB
)
public class UploadServlet extends HttpServlet {
    // Use in-memory storage for local testing - change to CloudDatastoreServiceImpl
    // for production
    private final IUserDatastoreService datastoreService = new InMemoryDatastoreServiceImpl();
    private final Gson gson = GsonProvider.getGson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> result = new HashMap<>();

        try {
            // Get the uploaded file
            Part filePart = request.getPart("file");

            if (filePart == null) {
                result.put("success", false);
                result.put("message", "No file uploaded");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(result));
                return;
            }

            // Parse Excel file
            List<User> users = parseExcelFile(filePart.getInputStream());

            if (users.isEmpty()) {
                result.put("success", false);
                result.put("message", "No valid users found in the Excel file");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(result));
                return;
            }

            // Save users to Datastore
            List<User> savedUsers = datastoreService.createUsers(users);

            result.put("success", true);
            result.put("message", "Successfully uploaded " + savedUsers.size() + " users");
            result.put("count", savedUsers.size());
            result.put("users", savedUsers);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(result));

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Error uploading file: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(result));
        }
    }

    private List<User> parseExcelFile(InputStream inputStream) throws IOException {
        List<User> users = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            // Skip header row
            boolean isFirstRow = true;

            for (Row row : sheet) {
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }

                // Skip empty rows
                if (isRowEmpty(row)) {
                    continue;
                }

                try {
                    User user = new User();

                    // Column 0: Name
                    Cell nameCell = row.getCell(0);
                    if (nameCell != null) {
                        user.setName(getCellValueAsString(nameCell));
                    }

                    // Column 1: DOB
                    Cell dobCell = row.getCell(1);
                    if (dobCell != null) {
                        LocalDate dob = getCellValueAsDate(dobCell);
                        user.setDateOfBirth(dob);
                    }

                    // Column 2: Email
                    Cell emailCell = row.getCell(2);
                    if (emailCell != null) {
                        user.setEmail(getCellValueAsString(emailCell));
                    }

                    // Column 3: Password
                    Cell passwordCell = row.getCell(3);
                    if (passwordCell != null) {
                        user.setPassword(getCellValueAsString(passwordCell));
                    }

                    // Column 4: Phone
                    Cell phoneCell = row.getCell(4);
                    if (phoneCell != null) {
                        user.setPhone(getCellValueAsString(phoneCell));
                    }

                    // Column 5: Gender
                    Cell genderCell = row.getCell(5);
                    if (genderCell != null) {
                        user.setGender(getCellValueAsString(genderCell));
                    }

                    // Column 6: Address
                    Cell addressCell = row.getCell(6);
                    if (addressCell != null) {
                        user.setAddress(getCellValueAsString(addressCell));
                    }

                    users.add(user);

                } catch (Exception e) {
                    // Skip invalid rows
                    System.err.println("Error parsing row: " + e.getMessage());
                }
            }
        }

        return users;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date date = cell.getDateCellValue();
                    return date.toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private LocalDate getCellValueAsDate(Cell cell) {
        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            Date date = cell.getDateCellValue();
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return LocalDate.parse(cell.getStringCellValue().trim());
            } catch (Exception e) {
                return null;
            }
        }

        return null;
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }

        for (int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++) {
            Cell cell = row.getCell(cellNum);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }

        return true;
    }
}
