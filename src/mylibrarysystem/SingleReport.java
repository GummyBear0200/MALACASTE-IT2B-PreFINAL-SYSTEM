package mylibrarysystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class SingleReport {
    private static Scanner scanner = new Scanner(System.in);
    private static config dbConfig = new config(); 
    private static BookMenu bookMenu = new BookMenu();
    private static BorrowerMenu borrowerMenu = new BorrowerMenu();

                         //--------------------------------------//
                        //            SPECIFIC REPORT           //
                       //--------------------------------------//

    public static void specificReport() {
        System.out.println("\n--------------- Specific Report ---------------");
        System.out.println("Select a specific report:");
        System.out.println("1.               Borrower Report");
        System.out.println("2.               Book Report");
        System.out.println("0.               Back to Main Menu");
        System.out.println("\n------------------------------------------------");
        System.out.print("Enter your choice: ");

        int reportChoice = -1;
        while (true) {
            try {
                reportChoice = scanner.nextInt();
                if (reportChoice < 0 || reportChoice > 2) {
                    System.out.println("Invalid choice. Please select 0, 1, or 2.");
                } else {
                    break; 
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter an integer.");
                scanner.next(); 
            }
        }

        System.out.println("------------------------------------------------");

        switch (reportChoice) {
            case 1:
                System.out.println("       Select an ID please:           ");
                borrowerMenu.viewBorrowers();
                borrowerReport();
                break;
            case 2:
                System.out.println("       Please Select the ID:          ");
                bookMenu.viewBooks();
                bookReport();
                break;
            case 0:
                System.out.println("Prompting Main Menu...");
                break;
            default:
                System.out.println("Invalid choice. Please select 1 or 2.");
        }
        
        System.out.println("------------------------------------------------");
        System.out.println("Thank you for using me, I mean the system hehe!");
    }

    private static void borrowerReport() {
        int borrowerId = getValidIntegerInput("Enter Borrower ID: ");

        String sql = "SELECT b.br_id, b.br_name, b.br_cnumber AS contact_number, " +
                     "COALESCE(p.penalty_status, 'None') AS penalty_status, " +
                     "CASE WHEN br.b_id IS NOT NULL THEN 'Has Borrowed Books' ELSE 'No Books Borrowed' END AS borrower_status " +
                     "FROM tbl_borrowers b " +
                     "LEFT JOIN tbl_penalties p ON b.br_id = p.br_id " +
                     "LEFT JOIN tbl_borrowedbooks br ON b.br_id = br.br_id " +
                     "WHERE b.br_id = ?";

        try (Connection conn = dbConfig.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, borrowerId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("\n--------------- Borrower Report ---------------");
                System.out.printf("%-15s: %s%n", "Borrower ID", rs.getInt("br_id"));
                System.out.printf("%-15s: %s%n", "Name", rs.getString("br_name"));
                System.out.printf("%-15s: %s%n", "Contact Number", rs.getString("contact_number"));
                System.out.printf("%-15s: %s%n", "Penalty Status", rs.getString("penalty_status"));
                System.out.printf("%-15s: %s%n", "Borrower Status", rs.getString("borrower_status"));
                System.out.println("------------------------------------------------");
            } else {
                System.out.println("No borrower found with ID: " + borrowerId);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving borrower report: " + e.getMessage());
        }
    }

    private static void bookReport() {
        int bookId = getValidIntegerInput("Enter Book ID: ");

        String sql = "SELECT bk.b_id, bk.b_title, bk.b_author, " +
                     "COALESCE(br.br_id, 'No IDs to show because this book is not currently borrowed') AS borrower_id, " +  
                     "COALESCE(b.br_name, 'No borrower has borrowed this book yet') AS borrower_name " +   
                     "FROM tbl_books bk " +
                     "LEFT JOIN tbl_borrowedbooks br ON bk.b_id = br.b_id " +
                     "LEFT JOIN tbl_borrowers b ON br.br_id = b.br_id " +
                     "WHERE bk.b_id = ?";

        try (Connection conn = dbConfig.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("\n--------------- Book Report ---------------");
                System.out.printf("%-15s: %s%n", "Book ID", rs.getInt("b_id"));
                System.out.printf("%-15s: %s%n", "Title", rs.getString("b_title"));
                System.out.printf("%-15s: %s%n", "Author", rs.getString("b_author")); 
                System.out.println("----------------- Book Status ---------------");
                System.out.printf("%-15s: %s%n", "Borrower ID", rs.getString("borrower_id"));  
                System.out.printf("%-15s: %s%n", "Borrower", rs.getString("borrower_name"));  
                System.out.println("------------------------------------------------");
            } else {
                System.out.println("No book found with ID: " + bookId);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving book report: " + e.getMessage());
        }
    }

    private static int getValidIntegerInput(String prompt) {
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