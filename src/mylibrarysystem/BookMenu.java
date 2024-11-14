
package mylibrarysystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.Scanner;


public class BookMenu {
    private Scanner scanner = new Scanner(System.in);
    private config dbConfig = new config(); 
    public void  bookMenu() {
    int choice = -1; 
    Scanner scanner = new Scanner(System.in);

    do {
        System.out.println("----------- Book Menu -----------");
        System.out.println("1. Add Book                     |");
        System.out.println("2. View Books                   |");
        System.out.println("3. Update Book                  |");
        System.out.println("4. Delete Book                  |");
        System.out.println("5. Back to Main Menu            |");
        System.out.println("---------------------------------");
        System.out.print("Enter your choice:              |\n");

      
       
        while (true) {
            try {
                choice = scanner.nextInt();
                if (choice < 1 || choice > 5) {
                    System.out.println("Invalid choice. Please enter a number between 1 and 5.");
                } else {
                    break; 
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter an integer.");
                scanner.next(); 
            }
        }

        switch (choice) {
            case 1:
                addBook();
                break;
            case 2:
                viewBooks();
                break;
            case 3:
                viewBooks();
                updateBook();
                viewBooks();
                break;
            case 4:
                deleteBook();
                break;
        }
    } while (choice != 5);
}
    private void addBook() {
    int bookId;

    while (true) {
        bookId = getValidIntegerInput("Enter Book ID: ");
        
        if (!idExists("tbl_books", "b_id", bookId)) {
            break; 
        }

        System.out.println("Book ID already exists. Please enter a different ID.");
    }

    System.out.print("Enter Book Title: ");
    String title = scanner.nextLine().trim();
    if (title.isEmpty()) {
        System.out.println("Book title cannot be empty.");
        return;
    }

    System.out.print("Enter Author: ");
    String author = scanner.nextLine().trim();
    if (author.isEmpty()) {
        System.out.println("Author name cannot be empty.");
        return;
    }

    String sql = "INSERT INTO tbl_books (b_id, b_title, b_author) VALUES (?, ?, ?)";
    dbConfig.addRecord(sql, bookId, title, author);
    System.out.println("Book added successfully.");
}

    private boolean idExists(String tableName, String columnName, int id) {
    String sqlQuery = "SELECT COUNT(*) FROM " + tableName + " WHERE " + columnName + " = ?";
    try (Connection conn = dbConfig.connectDB();
         PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {
        pstmt.setInt(1, id);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) > 0; 
        }
    } catch (SQLException e) {
        System.out.println("Error checking ID existence: " + e.getMessage());
    }
    return false; 
}
    
    public void viewBooks() {
        String sqlQuery = "SELECT * FROM tbl_books";
        String[] columnHeaders = {"Book ID", "Title", "Author"};
        String[] columnNames = {"b_id", "b_title", "b_author"};
        dbConfig.viewRecords(sqlQuery, columnHeaders, columnNames);
    }

    private void updateBook() {
    int bookId;
    
    while (true) {
        bookId = getValidIntegerInput("Enter Book ID to edit: ");

        if (idExists("tbl_books", "b_id", bookId)) {
            break; 
        }

        System.out.println("Book ID does not exist. Please enter again.");
    }

    System.out.print("Enter new title: ");
    String newTitle = scanner.nextLine().trim();
    if (newTitle.isEmpty()) {
        System.out.println("New title cannot be empty.");
        return;
    }

    System.out.print("Enter new author: ");
    String newAuthor = scanner.nextLine().trim();
    if (newAuthor.isEmpty()) {
        System.out.println("New author name cannot be empty.");
        return;
    }

    String sql = "UPDATE tbl_books SET b_title = ?, b_author = ? WHERE b_id = ?";
    dbConfig.addRecord(sql, newTitle, newAuthor, bookId);
    System.out.println("Book updated successfully.");
}

    private void deleteBook() {
    int bookId;

    while (true) {
        bookId = getValidIntegerInput("Enter Book ID to delete: ");
        
        if (idExists("tbl_books", "b_id", bookId)) {
            break; 
        }

        System.out.println("Book ID does not exist. Please enter again.");
    }

    System.out.print("Are you sure you want to delete this book? (yes/no): ");
    String confirmation = scanner.nextLine();
    
    if (!confirmation.equalsIgnoreCase("yes")) {
        System.out.println("Deletion cancelled.");
        return;
    }

    String sql = "DELETE FROM tbl_books WHERE b_id = ?";
    dbConfig.addRecord(sql, bookId);
    System.out.println("Book deleted successfully.");
}
    private int getValidIntegerInput(String prompt) {
    int value = -1;
    while (true) {
        System.out.print(prompt);
        if (scanner.hasNextInt()) {
            value = scanner.nextInt();
            scanner.nextLine(); 
            return value;
        } else {
            System.out.println("Invalid input. Please enter a valid integer.");
            scanner.nextLine(); 
        }
        
    }
    
    
}
}
