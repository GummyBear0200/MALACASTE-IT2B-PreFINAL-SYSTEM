package mylibrarysystem;

import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.sql.Date;
import java.util.InputMismatchException;
import java.util.Scanner;

public class SystemMenu {
     public static void main(String[] args) {
        SystemMenu library = new SystemMenu();
        library.mainMenu();
    }
   private Scanner scanner = new Scanner(System.in);
    private config dbConfig = new config(); 
    
     private BookMenu bookMenu = new BookMenu();
     private BorrowerMenu borrowerMenu = new BorrowerMenu();
     private BorrowerMenu viewBorrowers = new BorrowerMenu();
public void mainMenu() {
    int choice = -1; 
    Scanner scanner = new Scanner(System.in);

    do {
                                                                    
        System.out.println("---------------   Library   ---------------                                      | Choice Selection:                 ");
        System.out.println(" Books                                  |                                        | Select 1 - 9 for various options  ");
        System.out.println(" Borrowers                              |                                        | Select 0 for Exit                 ");
        System.out.println("---------------  Functions  ---------------                                      | Still confused? Heres the Manual! ");
        System.out.println(" Borrow Book                            |                                        | Manual:                           "); 
        System.out.println(" Return Book                            |                                        | Select 1 - 2 for  Library options "); 
        System.out.println(" Assign Penalties                       |                                        | Select 3 - 5 for  Functions       ");
        System.out.println("---------------   Reports   ---------------                                      | Select 6 - 8 for  Reports         ");
        System.out.println(" Borrowed Books                         |");
        System.out.println(" Books Availability                     |");     
        System.out.println(" View Penalties                         |");
        System.out.println("-------------------------------------------");
        System.out.println(" Exit                                   |");
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
    System.out.println("Available Borrowers:");
    borrowerMenu.viewBorrowers(); 

    System.out.println("\nAvailable Books:");
    displayBooksWithAvailability(); 

    int borrowerId = getValidIntegerInput("Enter Borrower ID: ");
    int bookId = getValidIntegerInput("Enter Book ID: ");
    int borrowDays = getValidIntegerInput("Enter Borrow Days: ");

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
                      "bb.date_borrowed, bb.borrow_days, bb.b_status, " +
                      "CASE WHEN julianday('now') > julianday(bb.date_borrowed) + bb.borrow_days THEN 'overdue' " +
                      "ELSE 'active' END AS calculated_status " +
                      "FROM tbl_borrowedbooks AS bb " +
                      "JOIN tbl_borrowers AS b ON bb.br_id = b.br_id " +
                      "JOIN tbl_books AS bk ON bb.b_id = bk.b_id";

    try (Connection conn = dbConfig.connectDB();
         PreparedStatement pstmt = conn.prepareStatement(sqlQuery);
         ResultSet rs = pstmt.executeQuery()) {

        if (!rs.isBeforeFirst()) { 
            System.out.println("\n");
            System.out.println("                ||-------------------------------------------------||");
            System.out.println("                ||           No borrowed books found.              ||");
            System.out.println("                ||-------------------------------------------------||");
            System.out.println("\n");
            return;
        }

        System.out.println("-------------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-12s | %-15s | %-10s | %-20s | %-20s | %-12s | %-10s%n", 
                          "Borrower ID", "Borrower Name", "Book ID", "Book Title", "Date Borrowed", "Borrow Days", "Status");
        System.out.println(new String(new char[110]).replace('\0', '-'));

        while (rs.next()) {
            int borrowerId = rs.getInt("br_id");
            String borrowerName = rs.getString("borrower_name");
            int bookId = rs.getInt("b_id");
            String bookTitle = rs.getString("book_title");
            String dateBorrowed = rs.getString("date_borrowed");
            int borrowDays = rs.getInt("borrow_days");
            String status = rs.getString("calculated_status");

            System.out.printf("%-12d | %-15s | %-10d | %-20s | %-20s | %-12d | %-10s%n", 
                              borrowerId, borrowerName, bookId, bookTitle, dateBorrowed, borrowDays, status);
            System.out.println("\n");
        }

    } catch (SQLException e) {
        System.out.println("Error retrieving borrowed books: " + e.getMessage());
    }
}

private void displayBooksWithAvailability() {
    String sqlQuery = "SELECT b.b_id, b.b_title, b.b_author, " +
                      "CASE WHEN bb.b_id IS NULL THEN 'Available' ELSE 'Not Available' END AS availability " +
                      "FROM tbl_books b " +
                      "LEFT JOIN tbl_borrowedbooks bb ON b.b_id = bb.b_id";
    
    try (Connection conn = dbConfig.connectDB();
         PreparedStatement pstmt = conn.prepareStatement(sqlQuery);
         ResultSet rs = pstmt.executeQuery()) {
        
        
        if (!rs.isBeforeFirst()) { 
            System.out.println("No books added recently.");
            return;
        }
        
        
        System.out.printf("%-10s %-30s %-20s %-15s%n", "Book ID", "Title", "Author", "Availability");
        System.out.println(new String(new char[75]).replace('\0', '-')); 

       
        while (rs.next()) {
            int bookId = rs.getInt("b_id");
            String title = rs.getString("b_title");
            String author = rs.getString("b_author");
            String availability = rs.getString("availability");

            System.out.printf("%-10d %-30s %-20s %-15s%n", bookId, title, author, availability);
        }

    } catch (SQLException e) {
        System.out.println("Error retrieving book availability: " + e.getMessage());
    }
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

private int calculateOverdueDays(int borrowerId, int bookId) {
    String sqlQuery = "SELECT date_borrowed, borrow_days FROM tbl_borrowedbooks WHERE br_id = ? AND b_id = ?";

    try (Connection conn = dbConfig.connectDB();
         PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {
        pstmt.setInt(1, borrowerId);
        pstmt.setInt(2, bookId);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            
            String dateBorrowed = rs.getString("date_borrowed");
            int borrowDays = rs.getInt("borrow_days");

           
            java.util.Date currentDate = new java.util.Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String currentDateString = sdf.format(currentDate); 

            
            int overdueDays = calculateDateDifference(dateBorrowed, currentDateString) - borrowDays;

            
            if (borrowDays == 0) {
                if (calculateDateDifference(dateBorrowed, currentDateString) > 0) {
                    overdueDays = calculateDateDifference(dateBorrowed, currentDateString);
                } else {
                    overdueDays = 0;
                }
            }

            
            if (overdueDays > 0) {
                return overdueDays; 
            }
        }
    } catch (SQLException e) {
        System.out.println("Error calculating overdue days: " + e.getMessage());
    }
    return 0; 
}

public void assignPenalty(int borrowerId) {
    String sqlQuery = "SELECT b_id, status FROM tbl_borrowedbooks WHERE br_id = ?";

    try (Connection conn = dbConfig.connectDB();
         PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {
        pstmt.setInt(1, borrowerId);
        ResultSet rs = pstmt.executeQuery();

        boolean hasOverdueBook = false;

        while (rs.next()) {
            int bookId = rs.getInt("b_id");
            String status = rs.getString("status");

            if ("overdue".equals(status)) {
                hasOverdueBook = true;
                
                System.out.println("Penalty assigned for overdue book ID: " + bookId);
            }
        }

        if (!hasOverdueBook) {
            System.out.println("No overdue books found for Borrower ID: " + borrowerId);
        }

    } catch (SQLException e) {
        System.out.println("Error assigning penalty: " + e.getMessage());
    }
}

private int calculateDateDifference(String startDate, String endDate) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); 
    try {
        
        java.util.Date start = null;
        java.util.Date end = null;

        // mag check if the startDate kay CURRENT_DATE
        if (startDate.equals("CURRENT_DATE")) {
            start = new java.util.Date(); 
        } else {
            start = sdf.parse(startDate); 
        }

        // Check if ang endDate kay CURRENTDATE
        if (endDate.equals("CURRENT_DATE")) {
            end = new java.util.Date(); 
        } else {
            end = sdf.parse(endDate); 
        }

        // Converter sa java.util.Date to java.sql.Date eme eme rajud ni kay way laing solution haysss
        java.sql.Date sqlStartDate = new java.sql.Date(start.getTime());
        java.sql.Date sqlEndDate = new java.sql.Date(end.getTime());

        
        long diffInMillies = sqlEndDate.getTime() - sqlStartDate.getTime();
        
       
        long diffInDays = diffInMillies / (1000 * 60 * 60 * 24);
        
        return (int) diffInDays; 
    } catch (Exception e) {
        System.out.println("Error parsing dates: " + e.getMessage());
    }
    return 0; 
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

               
                String penaltyCheckQuery = "SELECT COUNT(*) FROM tbl_penalties WHERE br_id = ? AND b_id = ?";
                try (PreparedStatement penaltyCheckStmt = conn.prepareStatement(penaltyCheckQuery)) {
                    penaltyCheckStmt.setInt(1, borrowerId);
                    penaltyCheckStmt.setInt(2, bookId);
                    ResultSet penaltyCheckRs = penaltyCheckStmt.executeQuery();
                    
                    
                    if (penaltyCheckRs.next() && penaltyCheckRs.getInt(1) == 0) {
                        
                        String penaltySql = "INSERT INTO tbl_penalties (br_id, b_id, date_assigned, penalty_status) " +
                                            "VALUES (?, ?, CURRENT_TIMESTAMP, 'overdue')";
                                   
                        try (PreparedStatement penaltyStmt = conn.prepareStatement(penaltySql)) {
                            penaltyStmt.setInt(1, borrowerId);
                            penaltyStmt.setInt(2, bookId);
                            int rowsAffected = penaltyStmt.executeUpdate();

                            if (rowsAffected > 0) {
                                System.out.println("Penalty assigned to Borrower ID: " + borrowerId + " for book: " + bookTitle);
                            } else {
                                System.out.println("Failed to assign penalty to Borrower ID: " + borrowerId + " for book: " + bookTitle);
                            }
                        }
                    } else {
                        System.out.println("Penalty already exists for Borrower ID: " + borrowerId + " for book: " + bookTitle);
                    }
                }
            } else {
               
                System.out.println("Penalty cannot be recorded because the book '" + bookTitle + "' is not overdue.");
            }
        }

        if (!hasOverdueBooks) {
            System.out.println("No overdue books found for Borrower ID: " + borrowerId);
        }
    } catch (SQLException e) {
        System.out.println("Error calculating penalties: " + e.getMessage());
    }
}





private void viewPenalties() {
    String sqlQuery = "SELECT p.penalty_id, p.br_id, b.br_name, p.b_id, bk.b_title, p.date_assigned, p.penalty_status " +
                      "FROM tbl_penalties p " +
                      "JOIN tbl_borrowers b ON p.br_id = b.br_id " +
                      "JOIN tbl_books bk ON p.b_id = bk.b_id";

    try (Connection conn = dbConfig.connectDB();
         PreparedStatement pstmt = conn.prepareStatement(sqlQuery);
         ResultSet rs = pstmt.executeQuery()) {

        
        if (!rs.isBeforeFirst()) { 
            System.out.println("                ||----------------------------------------------------------||");
            System.out.println("                ||           No penalty record added recently.              ||");
            System.out.println("                ||----------------------------------------------------------||");
            return;
        }

        
        System.out.printf("%-12s | %-10s | %-20s | %-10s | %-30s | %-20s | %-15s%n", 
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

            System.out.printf("%-12d | %-10d | %-20s | %-10d | %-30s | %-20s | %-15s%n", 
                              penaltyId, borrowerId, borrowerName, bookId, bookTitle, dateAssigned, penaltyStatus);
        }
    } catch (SQLException e) {
        System.out.println("Error viewing penalties: " + e.getMessage());
        return;
    }
    System.out.print("Do you want to clear a penalty record? (yes/no): ");
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