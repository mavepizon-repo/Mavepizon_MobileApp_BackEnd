package com.example.MpApp.service.officestaff;

import com.example.MpApp.dto.officestaff.CheckInRequestDTO;
import com.example.MpApp.entity.officestaff.OfficeStaff;
import com.example.MpApp.entity.officestaff.OfficeStaffAttendance;
import com.example.MpApp.repository.officestaff.OfficeStaffAttendanceRepository;
import com.example.MpApp.repository.officestaff.OfficeStaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OfficeStaffAttendanceService {

    private final OfficeStaffAttendanceRepository attendanceRepository;
    private final OfficeStaffRepository staffRepository;
    private final Clock clock;

    private static final LocalTime CUTOFF_CHECKIN = LocalTime.of(9, 3); // 09:03 AM
    private static final LocalTime CLOSING_TIME = LocalTime.of(18, 0);   // 06:00 PM
    private static final double MAX_ALLOWED_DISTANCE_METERS = 100.0;     // 100m tracking radius

    // ================= GEO-FENCE BRANCH REGISTRY =================
    // ================= GEO-FENCE BRANCH REGISTRY =================
    private static final Map<String, Coordinate> BRANCH_COORDINATES = Map.of(
            "TIRUNELVELI", new Coordinate(8.718412865303698, 77.73201179302228),
            "THISAYANVILLAI", new Coordinate(8.337947315572267, 77.86680851969136)
    );

    // Simple immutable helper class to hold coordinates
    private static class Coordinate {
        final double latitude;
        final double longitude;
        Coordinate(double lat, double lon) {
            this.latitude = lat;
            this.longitude = lon;
        }
    }

    /*
    ===================================
    1. STAFF CHECK-IN (DYNAMIC BRANCH GPS)
    ===================================
    */
    @Transactional
    public OfficeStaffAttendance checkIn(Long staffId, CheckInRequestDTO locationRequest) {
        LocalDate today = LocalDate.now(clock);
        LocalTime now = LocalTime.now(clock);

        // 1. Fetch Staff info and verify their assigned branch
        OfficeStaff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff member not found."));

        String staffBranch = staff.getBranch() != null ? staff.getBranch().toUpperCase() : "TIRUNELVELI";
        Coordinate targetBranchGeo = BRANCH_COORDINATES.get(staffBranch);

        if (targetBranchGeo == null) {
            throw new IllegalArgumentException("No geo-fence configuration found for branch: " + staffBranch);
        }

        // 2. Compute Distance against their explicit branch location coordinates
        double distanceInMeters = calculateHaversineDistance(
                locationRequest.getLatitude(), locationRequest.getLongitude(),
                targetBranchGeo.latitude, targetBranchGeo.longitude
        );

        if (distanceInMeters > MAX_ALLOWED_DISTANCE_METERS) {
            throw new IllegalArgumentException(String.format(
                    "Check-in rejected! You are %.2f meters away from your assigned %s branch. You must be within %.0f meters.",
                    distanceInMeters, staffBranch, MAX_ALLOWED_DISTANCE_METERS
            ));
        }

        // 3. Strict time validation (Reject completely after 9:03 AM)
        if (now.isAfter(CUTOFF_CHECKIN)) {
            throw new IllegalStateException("Check-in period has closed for today! You cannot check in after 9:03 AM.");
        }

        Optional<OfficeStaffAttendance> existingAttendance =
                attendanceRepository.findByStaffIdAndAttendanceDate(staffId, today);

        if (existingAttendance.isPresent()) {
            throw new IllegalStateException("Already checked in for today!");
        }

        OfficeStaffAttendance attendance = new OfficeStaffAttendance();
        attendance.setStaff(staff);
        attendance.setAttendanceDate(today);
        attendance.setCheckInTime(now);
        attendance.setStatus("PRESENT");

        return attendanceRepository.save(attendance);
    }

    /*
    ===================================
    2. STAFF CHECK-OUT (STATUS GUARDED)
    ===================================
    */
    @Transactional
    public OfficeStaffAttendance checkOut(Long staffId) {
        LocalDate today = LocalDate.now(clock);
        LocalTime now = LocalTime.now(clock);

        OfficeStaffAttendance attendance = attendanceRepository.findByStaffIdAndAttendanceDate(staffId, today)
                .orElseThrow(() -> new IllegalStateException("No Check-In entry logged for today. You cannot check out without checking in first."));

        if (!"PRESENT".equals(attendance.getStatus())) {
            throw new IllegalStateException("Check-out rejected. You were not marked as PRESENT today.");
        }

        if (attendance.getCheckOutTime() != null) {
            throw new IllegalStateException("Already checked out for today.");
        }

        attendance.setCheckOutTime(now);

        if (now.isBefore(CLOSING_TIME)) {
            attendance.setStatus("LEAVE_EARLY");
        }

        return attendanceRepository.save(attendance);
    }

    /*
    ===================================
    3. ADMIN PUBLIC HOLIDAY / OD BULK
    ===================================
    */
    @Transactional
    public String markHolidayOD(LocalDate holidayDate) {
        List<OfficeStaff> allStaff = staffRepository.findAll();

        for (OfficeStaff staff : allStaff) {
            Optional<OfficeStaffAttendance> existing = attendanceRepository.findByStaffIdAndAttendanceDate(staff.getId(), holidayDate);

            if (existing.isPresent()) {
                OfficeStaffAttendance attendance = existing.get();
                attendance.setStatus("OD");
                attendance.setCheckInTime(null);
                attendance.setCheckOutTime(null);
                attendanceRepository.save(attendance);
            } else {
                OfficeStaffAttendance holidayAttendance = new OfficeStaffAttendance(staff, holidayDate, "OD");
                attendanceRepository.save(holidayAttendance);
            }
        }
        return "Successfully registered OD status for all active office staff on " + holidayDate;
    }

    public List<OfficeStaffAttendance> getStaffAttendanceHistory(Long staffId) {
        return attendanceRepository.findByStaffId(staffId);
    }

    /*
    ===================================
    HAVERSINE MATHEMATICAL GEOGRAPHY UTILITY
    ===================================
    */
    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_METERS = 6371000;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c;
    }

    // Inside OfficeStaffAttendanceService.java

    public double calculateAttendancePercentage(Long staffId) {
        List<OfficeStaffAttendance> history = getStaffAttendanceHistory(staffId);
        if (history.isEmpty()) return 0.0;

        long totalDays = history.size();
        long presentDays = history.stream()
                .filter(a -> "PRESENT".equalsIgnoreCase(a.getStatus())) // Adjust field name if necessary
                .count();

        return ((double) presentDays / totalDays) * 100;
    }
}