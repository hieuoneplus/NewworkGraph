import java.io.File;

public class JsonParse {
    public static void main(String[] args) {
        // Đường dẫn tới thư mục chứa các file cần đọc
        String directoryPath = "src/main/resources/network";

        // Tạo một đối tượng File đại diện cho thư mục
        File directory = new File(directoryPath);

        // Kiểm tra xem đường dẫn này là một thư mục không
        if (directory.isDirectory()) {
            // Lấy danh sách các file trong thư mục
            File[] files = directory.listFiles();

            // Duyệt qua từng file và in ra tên của nó
            if (files != null) {
                for (File file : files) {
                    System.out.println("Tên file: " + file.getName());
                    // Thực hiện các hoạt động đọc file ở đây
                }
            }
        } else {
            System.out.println("Đường dẫn không phải là một thư mục.");
        }
    }
}

