package mylibrarysystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class SystemMenu {
    private Scanner scanner = new Scanner(System.in);
    private config dbConfig = new config(); 

    public void borrowerMenu() {
        int choice;
        do {
            System.out.println("----------- Borrower Menu -----------");
            System.out.println("1. Add Borrower                     |");
            System.out.println("2. View Borrowers                   |");
            System.out.println("3. Update Borrower                    |");
            System.out.println("4. Delete Borrower                  |");
            System.out.println("5. Back to Main Menu                |");
            System.out.println("-------------------------------------");
            System.out.print("Enter your choice:                  |\n");
            
            choice = scanner.nextInt();
            scanner.nextLine(); 

        
            if (choice == 1) {
                addBorrower();
            } else if (choice == 2) {
                viewBorrowers();
            } else if (choice == 3) {
                viewBorrowers();
                updateBorrower();
                viewBorrowers();
            } else if (choice == 4) {
                deleteBorrower();
            }
        } while (choice != 5);
    }

    public void bookMenu() {
        int choice;
        do {
            System.out.println("----------- Book Menu -----------");
            System.out.println("1. Add Book                     |");
            System.out.println("2. View Books                   |");
            System.out.println("3. Update Book                    |");
            System.out.println("4. Delete Book                  |");
            System.out.println("5. Back to Main Menu            |");
            System.out.println("---------------------------------");
            System.out.print("Enter your choice:              |\n");
            choice = scanner.nextInt();
            scanner.nextLine(); 

            if (choice == 1) {
                addBook();
            } else if (choice == 2) {
                viewBooks();
            } else if (choice == 3) {
                viewBooks();
                updateBook();
                viewBooks();
            } else if (choice == 4) {
                deleteBook();
            }
        } while (choice != 5);
    }

    private void addBorrower() {
    int borrowerId;
    while (true) {
        System.out.print("Enter Borrower ID: ");
        borrowerId = scanner.nextInt();
        scanner.nextLine();

        
        if (idExists("tbl_borrowers", "br_id", borrowerId)) {
            System.out.println("Borrower ID already exists. Please enter a different ID.");
        } else {
            break; 
        }
    }

    System.out.print("Enter Borrower Name: ");
    String name = scanner.nextLine();

    String sql = "INSERT INTO tbl_borrowers (br_id, br_name) VALUES (?, ?)";
    dbConfig.addRecord(sql, borrowerId, name);
    System.out.println("Borrower added successfully.");
}

    private void viewBorrowers() {
        String sqlQuery = "SELECT * FROM tbl_borrowers";
        String[] columnHeaders = {"Borrower ID", "Name"};
        String[] columnNames = {"br_id", "br_name"};
        dbConfig.viewRecords(sqlQuery, columnHeaders, columnNames);
    }

    private void updateBorrower() {
        System.out.print("Enter Borrower ID to edit: ");
        int borrowerId = scanner.nextInt();
        scanner.nextLine(); 

        System.out.print("Enter new name: ");
        String newName = scanner.nextLine();

        
        String sql = "UPDATE tbl_borrowers SET br_name = ? WHERE br_id = ?";
        dbConfig.addRecord(sql, newName, borrowerId);

        System.out.println("Borrower updated successfully.");
    }

    private void deleteBorrower() {
        System.out.print("Enter Borrower ID to delete: ");
        int borrowerId = scanner.nextInt();
        scanner.nextLine(); 

        
        String sql = "DELETE FROM tbl_borrowers WHERE br_id = ?";
        dbConfig.addRecord(sql, borrowerId);

        System.out.println("Borrower deleted successfully.");
    }

    private void addBook() {
    int bookId;
    while (true) {
        System.out.print("Enter Book ID: ");
        bookId = scanner.nextInt();
        scanner.nextLine();

        
        if (idExists("tbl_books", "b_id", bookId)) {
            System.out.println("Book ID already exists. Please enter a different ID.");
        } else {
            break; 
        }
    }

    System.out.print("Enter Book Title: ");
    String title = scanner.nextLine();
    System.out.print("Enter Author: ");
    String author = scanner.nextLine();

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
    
    private void viewBooks() {
        String sqlQuery = "SELECT * FROM tbl_books";
        String[] columnHeaders = {"Book ID", "Title", "Author"};
        String[] columnNames = {"b_id", "b_title", "b_author"};
        dbConfig.viewRecords(sqlQuery, columnHeaders, columnNames);
    }

    private void updateBook() {
        System.out.print("Enter Book ID to edit: ");
        int bookId = scanner.nextInt();
        scanner.nextLine(); 

        System.out.print("Enter new title: ");
        String newTitle = scanner.nextLine();
        System.out.print("Enter new author: ");
        String newAuthor = scanner.nextLine();

        
        String sql = "UPDATE tbl_books SET b_title = ?, b_author = ? WHERE b_id = ?";
        dbConfig.addRecord(sql, newTitle, newAuthor, bookId);

        System.out.println("Book updated successfully.");
    }

    private void deleteBook() {
        System.out.print("Enter Book ID to delete: ");
        int bookId = scanner.nextInt();
        scanner.nextLine(); 

        
        String sql = "DELETE FROM tbl_books WHERE b_id = ?";
        dbConfig.addRecord(sql, bookId);

        System.out.println("Book deleted successfully.");
    }

    
   

    public void mainMenu() {
        int choice;
        do {
            System.out.println("-----------  Library  -----------");
            System.out.println("1. Books                        |");
            System.out.println("2. Borrowers                    |");          
            System.out.println("3. Exit                         |");
            System.out.println("---------------------------------");
            System.out.print("Enter your choice:              |\n");
            System.out.println("---------------------------------");
            choice = scanner.nextInt();

            if (choice == 1) {
                bookMenu();
            } else if (choice == 2) {
                borrowerMenu();
            }
        } while (choice != 3);

        System.out.println("Exiting... Thank you for using the system!");
        scanner.close();
    }
}