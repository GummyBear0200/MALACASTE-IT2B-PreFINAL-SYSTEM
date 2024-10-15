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
            System.out.println("1. Register Borrower                  |");
            System.out.println("2. View Borrowers                     |");
            System.out.println("3. Update Borrower                    |");
            System.out.println("4. Delete Borrower                    |");
            System.out.println("5. Back to Main Menu                  |");
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
        borrowerId = getValidIntegerInput("Enter Borrower ID: ");
        
        if (!idExists("tbl_borrowers", "br_id", borrowerId)) {
            break; 
        }

        System.out.println("Borrower ID already exists. Please enter a different ID.");
    }

    System.out.print("Enter Borrower Name: ");
    String name = scanner.nextLine().trim();
    if (name.isEmpty()) {
        System.out.println("Borrower name cannot be empty.");
        return;
    }

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
    int borrowerId;

    while (true) {
        borrowerId = getValidIntegerInput("Enter Borrower ID to edit: ");
        
        if (idExists("tbl_borrowers", "br_id", borrowerId)) {
            break; 
        }

        System.out.println("Borrower ID does not exist. Please enter again.");
    }

    System.out.print("Enter new name: ");
    String newName = scanner.nextLine().trim();
    if (newName.isEmpty()) {
        System.out.println("New name cannot be empty.");
        return;
    }

    String sql = "UPDATE tbl_borrowers SET br_name = ? WHERE br_id = ?";
    dbConfig.addRecord(sql, newName, borrowerId);
    System.out.println("Borrower updated successfully.");
}

    private void deleteBorrower() {
    int borrowerId;

    while (true) {
        borrowerId = getValidIntegerInput("Enter Borrower ID to delete: ");
        
        if (idExists("tbl_borrowers", "br_id", borrowerId)) {
            break; 
        }

        System.out.println("Borrower ID does not exist. Please enter again.");
    }

    System.out.print("Are you sure you want to delete this borrower? (yes/no): ");
    String confirmation = scanner.nextLine();
    
    if (!confirmation.equalsIgnoreCase("yes")) {
        System.out.println("Deletion cancelled.");
        return;
    }

    String sql = "DELETE FROM tbl_borrowers WHERE br_id = ?";
    dbConfig.addRecord(sql, borrowerId);
    System.out.println("Borrower deleted successfully.");
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
    
    private void viewBooks() {
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

    private void borrowBook() {
    System.out.print("Enter Borrower ID: ");
    int borrowerId = scanner.nextInt();
    scanner.nextLine(); 

    System.out.print("Enter Book ID: ");
    int bookId = scanner.nextInt();
    scanner.nextLine(); 

    
    if (!idExists("tbl_borrowers", "br_id", borrowerId)) {
        System.out.println("Borrower ID does not exist.");
        return;
    }

   
    if (!idExists("tbl_books", "b_id", bookId)) {
        System.out.println("Book ID does not exist.");
        return;
    }

    
    String checkBorrowedSql = "SELECT COUNT(*) FROM tbl_borrowed WHERE b_id = ?";
    try (Connection conn = dbConfig.connectDB();
         PreparedStatement pstmt = conn.prepareStatement(checkBorrowedSql)) {
        pstmt.setInt(1, bookId);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next() && rs.getInt(1) > 0) {
            System.out.println("Book not available, This book is already borrowed by another borrower.");
            return;
        }
    } catch (SQLException e) {
        System.out.println("Error checking borrow status: " + e.getMessage());
        return;
    }

    
    String sql = "INSERT INTO tbl_borrowed (br_id, b_id) VALUES (?, ?)";
    dbConfig.addRecord(sql, borrowerId, bookId);
    System.out.println("Book borrowed successfully.");
}

private void returnBook() {
    System.out.print("Enter Borrower ID: ");
    int borrowerId = scanner.nextInt();
    scanner.nextLine(); 

    System.out.print("Enter Book ID: ");
    int bookId = scanner.nextInt();
    scanner.nextLine(); 

    
    if (!idExists("tbl_borrowers", "br_id", borrowerId)) {
        System.out.println("Borrower ID does not exist.");
        return;
    }

    
    if (!idExists("tbl_books", "b_id", bookId)) {
        System.out.println("Book ID does not exist.");
        return;
    }

    
    String checkSql = "SELECT COUNT(*) FROM tbl_borrowed WHERE br_id = ? AND b_id = ?";
    try (Connection conn = dbConfig.connectDB();
         PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
        pstmt.setInt(1, borrowerId);
        pstmt.setInt(2, bookId);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next() && rs.getInt(1) == 0) {
            System.out.println("This book was not borrowed by this borrower.");
            return;
        }
    } catch (SQLException e) {
        System.out.println("Error checking borrow status: " + e.getMessage());
        return;
    }

    
    String sql = "DELETE FROM tbl_borrowed WHERE br_id = ? AND b_id = ?";
    dbConfig.addRecord(sql, borrowerId, bookId);
    System.out.println("Book returned successfully.");
}
   private void displayBorrowedBooksWithNames() {
    String sqlQuery = "SELECT bb.br_id, b.br_name AS borrower_name, bb.b_id, bk.b_title AS book_title " +
                      "FROM tbl_borrowed bb " +
                      "JOIN tbl_borrowers b ON bb.br_id = b.br_id " +
                      "JOIN tbl_books bk ON bb.b_id = bk.b_id";

    String[] columnHeaders = {"Borrower ID", "Borrower Name", "Book ID", "Book Title"};
    String[] columnNames = {"br_id", "borrower_name", "b_id", "book_title"};
    dbConfig.viewRecords(sqlQuery, columnHeaders, columnNames);
}
private void displayBooksWithAvailability() {
    String sqlQuery = "SELECT b.b_id, b.b_title, b.b_author, " +
                      "CASE WHEN bb.b_id IS NULL THEN 'Available' ELSE 'Not Available' END AS availability " +
                      "FROM tbl_books b " +
                      "LEFT JOIN tbl_borrowed bb ON b.b_id = bb.b_id";

    String[] columnHeaders = {"Book ID", "Title", "Author", "Availability"};
    String[] columnNames = {"b_id", "b_title", "b_author", "availability"};
    dbConfig.viewRecords(sqlQuery, columnHeaders, columnNames);
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
    public void mainMenu() {
    int choice;
    do {
        System.out.println("----------------  Library  ----------------");
        System.out.println("1. Books                                  |");
        System.out.println("2. Borrowers                              |");  
        System.out.println("3. Borrow Book                            |"); 
        System.out.println("4. Return Book                            |"); 
        System.out.println("5. Display Borrowers with borrowed Books  |");
        System.out.println("6. Display Books  Availability            |"); 
        System.out.println("7. Exit                                   |");
        System.out.println("-------------------------------------------");
        System.out.print("Enter your choice:                        |\n");
        System.out.println("-------------------------------------------");
        choice = scanner.nextInt();

        if (choice == 1) {
            bookMenu();
        } else if (choice == 2) {
            borrowerMenu();
        } else if (choice == 3) {
            displayBooksWithAvailability();
            borrowBook(); 
        } else if (choice == 4) {
            displayBorrowedBooksWithNames(); 
            returnBook(); 
        } else if (choice == 5) {
            displayBorrowedBooksWithNames(); 
        } else if (choice == 6) { 
            displayBooksWithAvailability();
        }
    } while (choice != 7);

    System.out.println("Exiting... Salamat po mwaa!");
    scanner.close();
}
}