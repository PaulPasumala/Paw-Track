import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

public class VetClinicSystem {

    public record AppointmentDetails(
            String petName,
            String ownerName,
            String vetName,
            String date,
            String time,
            String username
    ) {}

    public static boolean processNewAppointment(AppointmentDetails details) {
        System.out.println("--- Clinic Backend System Received New Appointment ---");
        System.out.println("Pet: " + details.petName());
        System.out.println("User: " + details.username());

        // 1. Database Insertion (Dual Write)
        String sql = "INSERT INTO vet_appointments (pet_name, owner_name, vet_name, appt_date, appt_time, booking_time, status, username) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        DBDual.executeUpdateBoth(sql, stmt -> {
            stmt.setString(1, details.petName());
            stmt.setString(2, details.ownerName());
            stmt.setString(3, details.vetName());
            stmt.setString(4, details.date());
            stmt.setString(5, details.time());
            stmt.setTimestamp(6, new java.sql.Timestamp(new Date().getTime()));

            // [CHANGED] Status is now PENDING (Requires Vet Approval)
            stmt.setString(7, "PENDING");

            stmt.setString(8, details.username());
        });

        return true;
    }

    // Update Method (Used when editing an existing appointment)
    public static boolean updateAppointment(int apptId, AppointmentDetails details) {
        System.out.println("--- Clinic Backend Updating Appointment ID: " + apptId + " ---");

        String sql = "UPDATE vet_appointments SET pet_name=?, owner_name=?, vet_name=?, appt_date=?, appt_time=?, status=? WHERE appt_id=?";

        DBDual.executeUpdateBoth(sql, stmt -> {
            stmt.setString(1, details.petName());
            stmt.setString(2, details.ownerName());
            stmt.setString(3, details.vetName());
            stmt.setString(4, details.date());
            stmt.setString(5, details.time());

            // [CHANGED] Reset status to PENDING if user changes details
            stmt.setString(6, "PENDING");

            stmt.setInt(7, apptId);
        });

        return true;
    }
}