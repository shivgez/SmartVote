import java.sql.*;

public class Main {

    // ---------------- STUDENT LOGIN ----------------
    // Returns: "FIRST_LOGIN", "OK", "ALREADY_VOTED", "INVALID"
    public static String studentLoginStatus(int id, String pass) {
        try {
            Connection con = DBConnection.getConnection();
            String query = "SELECT * FROM students WHERE id=? AND password=?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, id);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                boolean hasVoted = rs.getBoolean("hasVoted");
                boolean isFirstLogin = rs.getBoolean("isFirstLogin");
                if (isFirstLogin) return "FIRST_LOGIN";
                if (hasVoted) return "ALREADY_VOTED";
                return "OK";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "INVALID";
    }

    // Keep old method for compatibility
    public static boolean studentLogin(int id, String pass) {
        String status = studentLoginStatus(id, pass);
        return status.equals("OK") || status.equals("FIRST_LOGIN");
    }

    // ---------------- MARK FIRST LOGIN DONE ----------------
    public static boolean clearFirstLogin(int studentId) {
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                "UPDATE students SET isFirstLogin = FALSE WHERE id=?"
            );
            ps.setInt(1, studentId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ---------------- ADMIN LOGIN ----------------
    public static boolean adminLogin(String user, String pass) {
        try {
            Connection con = DBConnection.getConnection();

            String query = "SELECT * FROM admin WHERE username=? AND password=?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, user);
            ps.setString(2, pass);

            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ---------------- GET CANDIDATES ----------------
    public static ResultSet getCandidates() {
        try {
            Connection con = DBConnection.getConnection();

            String query = "SELECT id, name, description, image, votes FROM candidates";
            Statement st = con.createStatement();

            return st.executeQuery(query);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ---------------- ADD CANDIDATE ----------------
    public static boolean addCandidate(String name, String desc, String imagePath) {
        try {
            Connection con = DBConnection.getConnection();

            String query = "INSERT INTO candidates(name, description, image) VALUES(?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(query);

            ps.setString(1, name);
            ps.setString(2, desc);
            ps.setString(3, imagePath);

            ps.executeUpdate();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ---------------- UPDATE CANDIDATE ----------------
    public static boolean updateCandidate(int id, String name, String desc, String imagePath) {
        try {
            Connection con = DBConnection.getConnection();

            String query = "UPDATE candidates SET name=?, description=?, image=? WHERE id=?";
            PreparedStatement ps = con.prepareStatement(query);

            ps.setString(1, name);
            ps.setString(2, desc);
            ps.setString(3, imagePath);
            ps.setInt(4, id);

            int rows = ps.executeUpdate();

            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ---------------- DELETE CANDIDATE (NEW) ----------------
    public static boolean deleteCandidate(int id) {
        try {
            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM candidates WHERE id=?"
            );
            ps.setInt(1, id);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ---------------- RESET VOTES ----------------
    public static boolean resetVotes() {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            Statement st1 = con.createStatement();
            st1.executeUpdate("UPDATE candidates SET votes = 0");

            Statement st2 = con.createStatement();
            st2.executeUpdate("UPDATE students SET hasVoted = FALSE");

            con.commit();
            return true;

        } catch (Exception e) {
            try {
                if (con != null) con.rollback();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
        return false;
    }

    // ---------------- CAST VOTE ----------------
    public static boolean castVote(int studentId, int candidateId) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            PreparedStatement ps1 = con.prepareStatement(
                    "UPDATE candidates SET votes = votes + 1 WHERE id=?"
            );
            ps1.setInt(1, candidateId);
            ps1.executeUpdate();

            PreparedStatement ps2 = con.prepareStatement(
                    "UPDATE students SET hasVoted = TRUE WHERE id=?"
            );
            ps2.setInt(1, studentId);
            ps2.executeUpdate();

            con.commit();
            return true;

        } catch (Exception e) {
            try {
                if (con != null) con.rollback();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
        return false;
    }

    // ---------------- RESET PASSWORD ----------------
    public static boolean resetPassword(int studentId, String oldPass, String newPass) {
        try {
            Connection con = DBConnection.getConnection();

            String checkQuery = "SELECT * FROM students WHERE id=? AND password=?";
            PreparedStatement ps1 = con.prepareStatement(checkQuery);
            ps1.setInt(1, studentId);
            ps1.setString(2, oldPass);

            ResultSet rs = ps1.executeQuery();

            if (rs.next()) {
                String updateQuery = "UPDATE students SET password=? WHERE id=?";
                PreparedStatement ps2 = con.prepareStatement(updateQuery);
                ps2.setString(1, newPass);
                ps2.setInt(2, studentId);

                ps2.executeUpdate();

                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}