package mylibrarysystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class SystemMenu {
   private Scanner scanner = new Scanner(System.in);
    private config dbConfig = new config(); 
    
     private BookMenu bookMenu = new BookMenu();
     private BorrowerMenu borrowerMenu = new BorrowerMenu();
public void mainMenu() {
    int choice = -1; 
    Scanner scanner = new Scanner(System.in);

    do {
        System.out.println("---------------   Library   ---------------");
        System.out.println("1. Books                                  |");
        System.out.println("2. Borrowers                              |");     
        System.out.println("---------------  Functions  ---------------");
        System.out.println("3. Borrow Book                            |"); 
        System.out.println("4. Return Book                            |"); 
        System.out.println("5. Assign Penalties                       |");
        System.out.println("---------------   Reports   ---------------");
        System.out.println("6. Borrowed Books                         |");
        System.out.println("7. Books Availability                     |");     
        System.out.println("8. View Penalties                         |");
        System.out.println("-------------------------------------------");
        System.out.println("0. Exit                                   |");
        System.out.println("-------------------------------------------");
        System.out.print("Enter your choice:                        |\n");

        
        while (true) {
            try {
                choice = scanner.nextInt();
                if (choice < 0 || choice > 8) {
                    System.out.println("Invalid choice. Please enter a number between 0 and 8.");
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
                     bookMenu.bookMenu();
                    break;
                case 2:
                     borrowerMenu.borrowerMenu();
                    break;
                case 3:
                    displayBooksWithAvailability();
                     
                    borrowBook(); 
                    break;
                case 4:
                    displayBorrowedBooksWithNames(); 
                    returnBook(); 
                    break;
                case 5:
                    displayBorrowedBooksWithNames(); 
                    calculatePenalties(); 
                    break;
                case 6: 
                    displayBorrowedBooksWithNames(); 
                    break;
                case 7:                   
                    displayBooksWithAvailability();
                    
                    break;
                case 8:
                    viewPenalties(); 
                    break;
            }
    } while (choice != 0);

    System.out.println("Exiting... Salamat po mwaa!");
    scanner.close();
}
  private void borrowBook() {
    System.out.print("Enter Borrower ID: ");
    int borrowerId = scanner.nextInt();
    scanner.nextLine(); 

    System.out.print("Enter Book ID: ");
    int bookId = scanner.nextInt();
    scanner.nextLine(); 

    System.out.print("Enter Borrow Days: ");
    int borrowDays = scanner.nextInt();
    scanner.nextLine();

    if (!idExists("tbl_borrowers", "br_id", borrowerId)) {
        System.out.println("Borrower ID does not exist.");
        return;
    }

    if (!idExists("tbl_books", "b_id", bookId)) {
        System.out.println("Book ID does not exist.");
        return;
    }

    String checkBorrowedSql = "SELECT COUNT(*) FROM tbl_borrowedbooks WHERE b_id = ?";
    try (Connection conn = dbConfig.connectDB();
         PreparedStatement pstmt = conn.prepareStatement(checkBorrowedSql)) {
        pstmt.setInt(1, bookId);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next() && rs.getInt(1) > 0) {
            System.out.println("Book not available. This book is already borrowed by another borrower.");
            return;
        }
    } catch (SQLException e) {
        System.out.println("Error checking borrow status: " + e.getMessage());
        return;
    }

    String sql = "INSERT INTO tbl_borrowedbooks (br_id, b_id, date_borrowed, borrow_days, b_status) VALUES (?, ?, CURRENT_TIMESTAMP, ?, 'active')";
    dbConfig.addRecord(sql, borrowerId, bookId, borrowDays);
    System.out.println("Book borrowed successfully.");

    
    checkAndUpdateOverdueStatus(borrowerId, bookId, borrowDays);
}

private void checkAndUpdateOverdueStatus(int borrowerId, int bookId, int borrowDays) {
    String updateStatusSql = "UPDATE tbl_borrowedbooks SET b_status = 'overdue' " +
                              "WHERE br_id = ? AND b_id = ? AND julianday('now') - julianday(date_borrowed) > ?";

    try (Connection conn = dbConfig.connectDB();
         PreparedStatement pstmt = conn.prepareStatement(updateStatusSql)) {
        pstmt.setInt(1, borrowerId);
        pstmt.setInt(2, bookId);
        pstmt.setInt(3, borrowDays);
        int rowsUpdated = pstmt.executeUpdate();
        
        if (rowsUpdated > 0) {
            System.out.println("Borrowed book status updated to overdue.");
        }
    } catch (SQLException e) {
        System.out.println("Error updating borrowed book status: " + e.getMessage());
    }
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

    String checkSql = "SELECT COUNT(*) FROM tbl_borrowedbooks WHERE br_id = ? AND b_id = ?";
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

   
    String sql = "DELETE FROM tbl_borrowedbooks WHERE br_id = ? AND b_id = ?";
    dbConfig.addRecord(sql, borrowerId, bookId);
    System.out.println("Book returned successfully.");

   
    String updatePenaltySql = "UPDATE tbl_penalties SET penalty_status = 'resolved' " +
                               "WHERE br_id = ? AND b_id = ? AND penalty_status = 'overdue'";
    try (Connection conn = dbConfig.connectDB();
         PreparedStatement pstmt = conn.prepareStatement(updatePenaltySql)) {
        pstmt.setInt(1, borrowerId);
        pstmt.setInt(2, bookId);
        int rowsUpdated = pstmt.executeUpdate();
        
        if (rowsUpdated > 0) {
            System.out.println("Penalty status updated to resolved.");
        } else {
            System.out.println("No penalties to resolve for this return.");
        }
    } catch (SQLException e) {
        System.out.println("Error updating penalty status: " + e.getMessage());
    }
}


  private void displayBorrowedBooksWithNames() {
    String sqlQuery = "SELECT bb.br_id, b.br_name AS borrower_name, bb.b_id, bk.b_title AS book_title, " +
                      "bb.date_borrowed, bb.borrow_days, bb.b_status " +
                      "FROM tbl_borrowedbooks bb " +
                      "JOIN tbl_borrowers b ON bb.br_id = b.br_id " +
                      "JOIN tbl_books bk ON bb.b_id = bk.b_id";

    String[] columnHeaders = {"Borrower ID", "Borrower Name", "Book ID", "Book Title", "Date Borrowed", "Borrow Days", "Status"};
    String[] columnNames = {"br_id", "borrower_name", "b_id", "book_title", "date_borrowed", "borrow_days", "b_status"};
    dbConfig.viewRecords(sqlQuery, columnHeaders, columnNames);
}


private void displayBooksWithAvailability() {
    String sqlQuery = "SELECT b.b_id, b.b_title, b.b_author, " +
                      "CASE WHEN bb.b_id IS NULL THEN 'Available' ELSE 'Not Available' END AS availability " +
                      "FROM tbl_books b " +
                      "LEFT JOIN tbl_borrowedbooks bb ON b.b_id = bb.b_id";

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


private void calculatePenalties() {
    System.out.print("Enter Borrower ID to assign penalties: ");
    int borrowerId = scanner.nextInt();
    scanner.nextLine(); 

    String sqlQuery = "SELECT bb.b_id, bk.b_title " +
                      "FROM tbl_borrowedbooks bb " +
                      "JOIN tbl_books bk ON bb.b_id = bk.b_id " +
                      "WHERE bb.br_id = ?";

    try (Connection conn = dbConfig.connectDB();
         PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {
        pstmt.setInt(1, borrowerId);
        ResultSet rs = pstmt.executeQuery();
        
        boolean hasOverdueBooks = false;

        while (rs.next()) {
            int bookId = rs.getInt("b_id");
            String bookTitle = rs.getString("b_title");

            int overdueDays = calculateOverdueDays(borrowerId, bookId);
            if (overdueDays > 0) {
                hasOverdueBooks = true;

                String penaltySql = "INSERT INTO tbl_penalties (br_id, b_id, date_assigned, penalty_status) " +
                                    "VALUES (?, ?, CURRENT_TIMESTAMP, 'overdue')";
                                   
                try (PreparedStatement penaltyStmt = conn.prepareStatement(penaltySql)) {
                    penaltyStmt.setInt(1, borrowerId);
                    penaltyStmt.setInt(2, bookId);
                    penaltyStmt.executeUpdate();
                }

                System.out.println("Penalty assigned to Borrower ID: " + borrowerId + " for book: " + bookTitle);
            }
        }

        if (!hasOverdueBooks) {
            System.out.println("No overdue books found for Borrower ID: " + borrowerId);
        }
    } catch (SQLException e) {
        System.out.println("Error calculating penalties: " + e.getMessage());
    }
}


private int calculateOverdueDays(int borrowerId, int bookId) {
    
    final int BORROW_DURATION = 14; 

    
   
    int simulatedBorrowedDays = 20; 

    
    if (simulatedBorrowedDays > BORROW_DURATION) {
        return simulatedBorrowedDays - BORROW_DURATION;
    }
    
    return 0; 
}

private void viewPenalties() {
    String sqlQuery = "SELECT p.penalty_id, p.br_id, b.br_name, p.b_id, bk.b_title, p.date_assigned, p.penalty_status " +
                      "FROM tbl_penalties p " +
                      "JOIN tbl_borrowers b ON p.br_id = b.br_id " +
                      "JOIN tbl_books bk ON p.b_id = bk.b_id";

    try (Connection conn = dbConfig.connectDB();
         PreparedStatement pstmt = conn.prepareStatement(sqlQuery);
         ResultSet rs = pstmt.executeQuery()) {
        
        System.out.printf("%-12s %-10s %-20s %-10s %-30s %-20s %-15s%n", 
                          "Penalty ID", "Borrower ID", "Borrower Name", "Book ID", "Book Title", "Date Assigned", "Status");
        System.out.println(new String(new char[130]).replace('\0', '-')); 

        while (rs.next()) {
            int penaltyId = rs.getInt("penalty_id");
            int borrowerId = rs.getInt("br_id");
            String borrowerName = rs.getString("br_name");
            int bookId = rs.getInt("b_id");
            String bookTitle = rs.getString("b_title");
            String dateAssigned = rs.getString("date_assigned");
            String penaltyStatus = rs.getString("penalty_status");

            System.out.printf("%-12d %-10d %-20s %-10d %-30s %-20s %-15s%n", 
                              penaltyId, borrowerId, borrowerName, bookId, bookTitle, dateAssigned, penaltyStatus);
        }
    } catch (SQLException e) {
        System.out.println("Error viewing penalties: " + e.getMessage());
        return;
    }

    
    System.out.print("Do you want to clear the record? (yes/no): ");
    String response = scanner.nextLine().trim().toLowerCase();

    if (response.equals("yes")) {
        System.out.print("Enter the Penalty ID to clear: ");
        int penaltyIdToClear = scanner.nextInt();
        scanner.nextLine(); 

       
        String deleteSql = "DELETE FROM tbl_penalties WHERE penalty_id = ?";
        try (Connection conn = dbConfig.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
            pstmt.setInt(1, penaltyIdToClear);
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("Penalty record deleted successfully.");
            } else {
                System.out.println("No penalty record found with that ID.");
            }
        } catch (SQLException e) {
            System.out.println("Error deleting penalty record: " + e.getMessage());
        }
    } else {
        System.out.println("No records were cleared.");
    }
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




}