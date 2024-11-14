
package mylibrarysystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.Scanner;


public class BorrowerMenu {
     private Scanner scanner = new Scanner(System.in);
    private config dbConfig = new config(); 

   public void borrowerMenu() {
    int choice = -1; 
    Scanner scanner = new Scanner(System.in);

    do {
        System.out.println("----------- Borrower Menu -----------");
        System.out.println("1. Register Borrower                  |");
        System.out.println("2. View Borrowers                     |");
        System.out.println("3. Update Borrower                    |");
        System.out.println("4. Delete Borrower                    |");
        System.out.println("5. Back to Main Menu                  |");
        System.out.println("-------------------------------------");
        System.out.print("Enter your choice:                  |\n");

        
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
                addBorrower();
                break;
            case 2:
                viewBorrowers();
                break;
            case 3:
                viewBorrowers();
                updateBorrower();
                viewBorrowers();
                break;
            case 4:
                deleteBorrower();
                break;
        }
    } while (choice != 5);
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

    System.out.print("Enter Phone Number: ");
    String phoneNumber = scanner.nextLine().trim();
    if (phoneNumber.isEmpty()) {
        System.out.println("Phone number cannot be empty.");
        return;
    }

    System.out.print("Enter Address: ");
    String address = scanner.nextLine().trim();
    if (address.isEmpty()) {
        System.out.println("Address cannot be empty.");
        return;
    }

    String sql = "INSERT INTO tbl_borrowers (br_id, br_name, br_cnumber, br_address) VALUES (?, ?, ?, ?)";
    dbConfig.addRecord(sql, borrowerId, name, phoneNumber, address);
    System.out.println("Borrower added successfully.");
}


  void viewBorrowers() {
    String sqlQuery = "SELECT br_id, br_name, br_cnumber, br_address FROM tbl_borrowers";
    String[] columnHeaders = {"Borrower ID", "Name", "Phone Number", "Address"};
    String[] columnNames = {"br_id", "br_name", "br_cnumber", "br_address"};
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

    System.out.print("Enter new phone number: ");
    String newPhoneNumber = scanner.nextLine().trim();
    if (newPhoneNumber.isEmpty()) {
        System.out.println("Phone number cannot be empty.");
        return;
    }

    System.out.print("Enter new address: ");
    String newAddress = scanner.nextLine().trim();
    if (newAddress.isEmpty()) {
        System.out.println("Address cannot be empty.");
        return;
    }

    String sql = "UPDATE tbl_borrowers SET br_name = ?, br_cnumber = ?, br_address = ? WHERE br_id = ?";
    dbConfig.addRecord(sql, newName, newPhoneNumber, newAddress, borrowerId);
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
