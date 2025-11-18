package utilities;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelWriter {

    public static void writeViolationsToExcel(String fileName, JSONArray violations) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Accessibility Violations");

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Rule ID", "Description", "Impact", "Elements", "Failure Summary", "WCAG Rule"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // Populate data rows
        int rowNum = 1;
        for (int i = 0; i < violations.length(); i++) {
            JSONObject violation = violations.getJSONObject(i);
            JSONArray nodes = violation.getJSONArray("nodes");
            JSONArray tags = violation.getJSONArray("tags");
            StringBuilder wcagRules = new StringBuilder();
            for (int k = 0; k < tags.length(); k++) {
                String tag = tags.getString(k);
                if (tag.startsWith("wcag")) {
                    if (wcagRules.length() > 0) {
                        wcagRules.append(", ");
                    }
                    wcagRules.append(tag);
                }
            }

            for (int j = 0; j < nodes.length(); j++) {
                JSONObject node = nodes.getJSONObject(j);
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(violation.getString("id"));
                row.createCell(1).setCellValue(violation.getString("description"));
                row.createCell(2).setCellValue(violation.getString("impact"));
                row.createCell(3).setCellValue(node.getString("html"));
                row.createCell(4).setCellValue(node.getString("failureSummary"));
                row.createCell(5).setCellValue(wcagRules.toString());
            }
        }

        // Ensure the directory exists before writing the file
        File directory = new File("Excel_Reports");
        if (!directory.exists()) {
            directory.mkdirs(); // Create the directory if it does not exist
        }

        // Write the output to a file
        try {
            try (FileOutputStream outputStream = new FileOutputStream("Excel_Reports/" + fileName)) {
                workbook.write(outputStream);
            }
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
