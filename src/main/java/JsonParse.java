import java.io.File;

public class JsonParse {
    public static void main(String[] args) {
        // Dữ liệu cần in ra
        String[][] data = {
                {"topo", "NSGA", "GA"},
                {"rural", "0.2", "0.1"},
                {"cogentcenter", "0.2", "0.1"}
        };

        // In ra dữ liệu
        for (String[] row : data) {
            for (String value : row) {
                // Sử dụng String.format() để căn chỉnh và in ra dữ liệu
                System.out.printf("%-15s", value);
            }
            System.out.println(); // Xuống dòng sau khi in xong mỗi hàng
        }
    }
}

