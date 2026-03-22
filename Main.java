import java.sql.*;
import java.util.Scanner;

public class Main {
    static Scanner sc = new Scanner(System.in);

        public static void main(String[] args) {

            System.out.println("===== SMART VOTING SYSTEM =====");

            System.out.println("1. Student Login");
            System.out.println("2. Admin Login");

            int choice = sc.nextInt();

            if (choice == 1) {
                studentLogin();
            } else if (choice == 2) {
                adminLogin();
            } else {
                System.out.println("Invalid choice");
            }
        }




    public static void showCandidates(Connection con, int studentId) {
        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM candidates");

            System.out.println("\n=== Candidates ===");

            while (rs.next()) {
                System.out.println(rs.getInt("id") + ". " + rs.getString("name"));
            }

            System.out.print("Enter candidate ID: ");
            int cid = sc.nextInt();

            castVote(con, studentId, cid);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void studentLogin() {
        try {
            Connection con = DBConnection.getConnection();

            System.out.print("Enter Student ID: ");
            int id = sc.nextInt();

            System.out.print("Enter Password: ");
            String pass = sc.next();

            String query = "SELECT * FROM students WHERE id=? AND password=?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, id);
            ps.setString(2, pass);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                boolean hasVoted = rs.getBoolean("hasVoted");

                if (hasVoted) {
                    System.out.println("You have already voted!");
                } else {
                    showCandidates(con, id);
                }
            } else {
                System.out.println("Invalid Login!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void adminLogin() {
        try {
            Connection con = DBConnection.getConnection();

            System.out.print("Enter Admin Username: ");
            String user = sc.next();

            System.out.print("Enter Admin Password: ");
            String pass = sc.next();

            String query = "SELECT * FROM admin WHERE username=? AND password=?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, user);
            ps.setString(2, pass);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                System.out.println("Admin Login Successful!");
                adminMenu(con);
            } else {
                System.out.println("Invalid Admin Login!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

        public static void adminMenu(Connection con) {
            System.out.println("\n=== ADMIN PANEL ===");
            System.out.println("1. View Results");
            System.out.println("2. Add Candidate");

            int ch = sc.nextInt();

            if (ch == 1) {
                showResults(con);
            } else if (ch == 2) {
                addCandidate(con);
            }
            else {
                System.out.println("Invalid choice");
            }
        }
       public static void showResults(Connection con) {
        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM candidates");

            System.out.println("\n=== RESULTS ===");

            while (rs.next()) {
                System.out.println(
                        rs.getString("name") + " : " + rs.getInt("votes")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void addCandidate(Connection con) {
        System.out.print("Enter candidate name: ");
        String name = sc.next();

        try {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO candidates(name) VALUES(?)"
            );
            ps.setString(1, name);
            ps.executeUpdate();

            System.out.println("Candidate Added!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void castVote(Connection con, int studentId, int candidateId) {
        try {
            // increase vote
            PreparedStatement ps1 = con.prepareStatement(
                    "UPDATE candidates SET votes = votes + 1 WHERE id=?"
            );
            ps1.setInt(1, candidateId);
            ps1.executeUpdate();

            // mark student voted
            PreparedStatement ps2 = con.prepareStatement(
                    "UPDATE students SET hasVoted = TRUE WHERE id=?"
            );
            ps2.setInt(1, studentId);
            ps2.executeUpdate();

            System.out.println("Vote Cast Successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
