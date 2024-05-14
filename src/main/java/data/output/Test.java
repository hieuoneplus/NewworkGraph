package data.output;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.Scanner;

public class Test {
    public static String name = "HV_average_K40.txt";
    public static String path = "src/main/java/data/output/";
    public static void main(String[] args) throws InvalidFormatException {
        // Tạo một workbook mới
        try (Workbook workbook = WorkbookFactory.create(new FileInputStream("example.xlsx"))) {
            // Tạo một trang mới
            Sheet sheet = workbook.createSheet(name);
            sheet.setColumnWidth(0, 6000);
            sheet.setColumnWidth(1, 4000);


            // Dữ liệu mảng 2D
            Scanner sc = new Scanner(new File(path+name));
            // In dữ liệu vào các hàng và cột
            int rowNum = 0;
            while (sc.hasNextLine()) {
                Row row = sheet.createRow(rowNum++);
                int colNum = 0;
                var field = sc.nextLine().split("\s+");
                Cell cell = row.createCell(colNum++);
                cell.setCellValue(field[0]);
                Cell cell1 = row.createCell(colNum++);
                cell1.setCellValue(field[1]);
                Cell cell2 = row.createCell(colNum++);
                cell2.setCellValue(field[2]);
                }

            // Lưu workbook vào một tệp Excel

            try (FileOutputStream outputStream = new FileOutputStream("example1.xlsx")) {
                workbook.write(outputStream);
            }

            System.out.println("Tạo tệp Excel thành công!");
    } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
