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
                updateBorrower();
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
            System.out.println("3. Edit Book                    |");
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
                updateBook();
            } else if (choice == 4) {
                deleteBook();
            }
        } while (choice != 5);
    }

    private void addBorrower() {
        System.out.print("Enter Borrower ID: ");
        int borrowerId = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Enter Borrower Name: ");
        String name = scanner.nextLine();

        // Add to the database
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

        // Update in the database
        String sql = "UPDATE tbl_borrowers SET name = ? WHERE br_id = ?";
        dbConfig.addRecord(sql, newName, borrowerId);

        System.out.println("Borrower updated successfully.");
    }

    private void deleteBorrower() {
        System.out.print("Enter Borrower ID to delete: ");
        int borrowerId = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        // Delete from the database
        String sql = "DELETE FROM tbl_borrowers WHERE br_id = ?";
        dbConfig.addRecord(sql, borrowerId);

        System.out.println("Borrower deleted successfully.");
    }

    private void addBook() {
        System.out.print("Enter Book ID: ");
        int bookId = scanner.nextInt();
        scanner.nextLine(); 
        System.out.print("Enter Book Title: ");
        String title = scanner.nextLine();
        System.out.print("Enter Author: ");
        String author = scanner.nextLine();

        // Add to the database
        String sql = "INSERT INTO tbl_books (b_id, b_title, b_author) VALUES (?, ?, ?)";
        dbConfig.addRecord(sql, bookId, title, author);
        System.out.println("Book added successfully.");
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

        // Update in the database
        String sql = "UPDATE tbl_books SET b_title = ?, b_author = ? WHERE b_id = ?";
        dbConfig.addRecord(sql, newTitle, newAuthor, bookId);

        System.out.println("Book updated successfully.");
    }

    private void deleteBook() {
        System.out.print("Enter Book ID to delete: ");
        int bookId = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        // Delete from the database
        String sql = "DELETE FROM tbl_books WHERE b_id = ?";
        dbConfig.addRecord(sql, bookId);

        System.out.println("Book deleted successfully.");
    }

    private void borrowBook() {
        System.out.print("Enter Book ID to borrow: ");
        int bookId = scanner.nextInt();
        scanner.nextLine(); 

        String sqlCheck = "SELECT isBorrowed FROM tbl_books WHERE b_id = ?";
        try (Connection conn = dbConfig.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sqlCheck)) {
            pstmt.setInt(1, bookId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                boolean isBorrowed = rs.getBoolean("isBorrowed");
                if (!isBorrowed) {
                    String sqlUpdate = "UPDATE tbl_books SET isBorrowed = ? WHERE b_id = ?";
                    dbConfig.addRecord(sqlUpdate, true, bookId);
                    System.out.println("Book borrowed successfully.");
                } else {
                    System.out.println("Book is already borrowed.");
                }
            } else {
                System.out.println("Book not found.");
            }
        } catch (SQLException e) {
            System.out.println("Error borrowing book: " + e.getMessage());
        }
    }

    private void returnBook() {
        System.out.print("Enter Book ID to return: ");
        int bookId = scanner.nextInt();
        scanner.nextLine(); 

        String sqlCheck = "SELECT isBorrowed FROM tbl_books WHERE b_id = ?";
        try (Connection conn = dbConfig.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sqlCheck)) {
            pstmt.setInt(1, bookId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                boolean isBorrowed = rs.getBoolean("isBorrowed");
                if (isBorrowed) {
                    String sqlUpdate = "UPDATE tbl_books SET isBorrowed = ? WHERE b_id = ?";
                    dbConfig.addRecord(sqlUpdate, false, bookId);
                    System.out.println("Book returned successfully.");
                } else {
                    System.out.println("Book was not borrowed.");
                }
            } else {
                System.out.println("Book not found.");
            }
        } catch (SQLException e) {
            System.out.println("Error returning book: " + e.getMessage());
        }
    }

    private void viewBorrowedBooks() {
        String sqlQuery = "SELECT * FROM tbl_books WHERE isBorrowed = true";
        String[] columnHeaders = {"Book ID", "Title", "Author"};
        String[] columnNames = {"b_id", "b_title", "b_author"};
        dbConfig.viewRecords(sqlQuery, columnHeaders, columnNames);
    }

    private void viewReturnedBooks() {
        String sqlQuery = "SELECT * FROM tbl_books WHERE isBorrowed = false";
        String[] columnHeaders = {"Book ID", "Title", "Author"};
        String[] columnNames = {"b_id", "b_title", "b_author"};
        dbConfig.viewRecords(sqlQuery, columnHeaders, columnNames);
    }

    private void viewPenalties() {
        System.out.println("----------- Penalties -----------");
        int penaltyAmount = 5; 
        System.out.println("Each overdue book incurs a penalty of $" + penaltyAmount + ".");
    }

    public void mainMenu() {
        int choice;
        do {
            System.out.println("----------- Main Menu -----------");
            System.out.println("1. Books                        |");
            System.out.println("2. Borrowers                    |");
            System.out.println("3. Borrow Books                 |");
            System.out.println("4. Return Books                 |");
            System.out.println("5. View Borrowed Books          |");
            System.out.println("6. View Returned Books          |");
            System.out.println("7. View Penalties               |");
            System.out.println("8. Exit                         |");
            System.out.println("---------------------------------");
            System.out.print("Enter your choice:              |\n");
            System.out.println("---------------------------------");
            choice = scanner.nextInt();

            if (choice == 1) {
                bookMenu();
            } else if (choice == 2) {
                borrowerMenu();
            } else if (choice == 3) {
                borrowBook();
            } else if (choice == 4) {
                returnBook();
            } else if (choice == 5) {
                viewBorrowedBooks();
            } else if (choice == 6) {
                viewReturnedBooks();
            } else if (choice == 7) {
                viewPenalties();
            }
        } while (choice != 8);

        System.out.println("Exiting... Thank you for using the system!");
        scanner.close();
    }
}