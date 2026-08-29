package com.example.MpApp.service.officestaff;

import com.example.MpApp.config.JwtService;
import com.example.MpApp.dto.Attendance.AttendanceResponseDTO;
import com.example.MpApp.dto.officestaff.CheckInRequestDTO;
import com.example.MpApp.entity.officestaff.OfficeStaff;
import com.example.MpApp.entity.officestaff.OfficeStaffAttendance;
import com.example.MpApp.entity.officestaff.OfficeStaffPermission;
import com.example.MpApp.repository.officestaff.OfficeStaffAttendanceRepository;
import com.example.MpApp.repository.officestaff.OfficeStaffPermissionRepository;
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
    private final JwtService jwtService;
    private final OfficeStaffPermissionRepository permissionRepository;

    private static final LocalTime CUTOFF_CHECKIN = LocalTime.of(9, 3); // 09:03 AM
    private static final LocalTime CLOSING_TIME = LocalTime.of(18, 0);   // 06:00 PM
    private static final double MAX_ALLOWED_DISTANCE_METERS = 100.0;     // 100m tracking radius

    public String extractEmail(String authHeader){
        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            throw new RuntimeException("Token Required");
        }

        String token = authHeader.substring(7);

        return jwtService.extractUsername(token);
    }

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
    public OfficeStaffAttendance checkIn(String authHeader, CheckInRequestDTO locationRequest) {
        LocalDate today = LocalDate.now(clock);
        LocalTime now = LocalTime.now(clock);

        String email = extractEmail(authHeader);

        // 1. Fetch Staff info and verify their assigned branch
        OfficeStaff staff = staffRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Staff member not found."));

        LocalTime CheckInTime = staff.getShiftStartTime();

        if (CheckInTime == null) {
            throw new IllegalStateException("Shift start time not configured for this staff member.");
        }


        LocalTime checkInDeadline = CheckInTime.plusMinutes(3);

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


        Optional<OfficeStaffAttendance> existingAttendance =
                attendanceRepository.findByStaffIdAndAttendanceDate(staff.getId(), today);

        if (existingAttendance.isPresent()) {
            throw new IllegalStateException("Already checked in for today!");
        }

        Optional<OfficeStaffPermission> approvedPermission =
                permissionRepository.findByStaffIdAndPermissionDateAndStatus(staff.getId(), today, "APPROVED");

        if (approvedPermission.isPresent()) {
            checkInDeadline = checkInDeadline.plusHours(approvedPermission.get().getDurationHours());
        }

        if (now.isAfter(checkInDeadline)) {
            throw new IllegalStateException("Check-in period has closed for today! You cannot check in after ." + checkInDeadline);
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
    public OfficeStaffAttendance checkOut(String authHeader) {
        LocalDate today = LocalDate.now(clock);
        LocalTime now = LocalTime.now(clock);

        String email = extractEmail(authHeader);

        OfficeStaff staff = staffRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("Staff member not found.")
        );


        LocalTime CheckOutTime = staff.getShiftEndTime();




        OfficeStaffAttendance attendance = attendanceRepository.findByStaffIdAndAttendanceDate(staff.getId(), today)
                .orElseThrow(() -> new IllegalStateException("No Check-In entry logged for today. You cannot check out without checking in first."));

        if (!"PRESENT".equals(attendance.getStatus())) {
            throw new IllegalStateException("Check-out rejected. You were not marked as PRESENT today.");
        }

        if (attendance.getCheckOutTime() != null) {
            throw new IllegalStateException("Already checked out for today.");
        }

        attendance.setCheckOutTime(now);

        if (now.isBefore(CheckOutTime)) {
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

    public List<OfficeStaffAttendance> getAttendanceHistory(Long staffId) {

        return attendanceRepository.findByStaffId(staffId);
    }



    public List<AttendanceResponseDTO> getStaffAttendanceHistory(String authHeader) {

        String email = extractEmail(authHeader);

        OfficeStaff staff = staffRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Staff member not found."));

        List<OfficeStaffAttendance> attendance =
                attendanceRepository.findByStaffId(staff.getId());

        return attendance.stream()
                .map(att -> {

                    AttendanceResponseDTO dto = new AttendanceResponseDTO();

                    dto.setId(att.getId());
                    dto.setAttendanceDate(att.getAttendanceDate());
                    dto.setCheckInTime(att.getCheckInTime());
                    dto.setCheckOutTime(att.getCheckOutTime());
                    dto.setStatus(att.getStatus());

                    // Staff details
                    dto.setStaffName(att.getStaff().getName());
                    dto.setBranch(att.getStaff().getBranch());

                    return dto;
                })
                .toList();
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
        List<OfficeStaffAttendance> history = getAttendanceHistory(staffId);
        if (history.isEmpty()) return 0.0;

        long totalDays = history.size();
        long presentDays = history.stream()
                .filter(a -> "PRESENT".equalsIgnoreCase(a.getStatus())) // Adjust field name if necessary
                .count();

        return ((double) presentDays / totalDays) * 100;
    }
    /*
    ===================================
    4. GET LEAVES GROUPED BY DATE
    ===================================
    */
    public Map<LocalDate, java.util.List<Map<String, Object>>> getLeavesByDateRange(LocalDate start, LocalDate end) {
        // Fetch all attendance entries marked as absent/on leave within the calendar viewing boundary
        List<OfficeStaffAttendance> records = attendanceRepository.findAll(); // Or create a date-bounded repository query

        Map<LocalDate, java.util.List<Map<String, Object>>> leaveCalendar = new java.util.HashMap<>();

        records.stream()
                .filter(a -> a.getAttendanceDate() != null &&
                        !a.getAttendanceDate().isBefore(start) && !a.getAttendanceDate().isAfter(end))
                .filter(a -> "LEAVE".equalsIgnoreCase(a.getStatus()) || "ABSENT".equalsIgnoreCase(a.getStatus()))
                .forEach(record -> {
                    LocalDate date = record.getAttendanceDate();
                    leaveCalendar.putIfAbsent(date, new java.util.ArrayList<>());

                    Map<String, Object> staffDetails = new java.util.HashMap<>();
                    staffDetails.put("staffId", record.getStaff().getId());
                    staffDetails.put("name", record.getStaff().getName());
                    staffDetails.put("category", record.getStaff().getCategory());
                    staffDetails.put("status", record.getStatus());

                    leaveCalendar.get(date).add(staffDetails);
                });

        return leaveCalendar;
    }

    /*
    ===================================
    5. FORCE SATURDAY AS WORKING DAY
    ===================================
    */
    @Transactional
    public String configureSaturdayAsWorkingDay(LocalDate targetSaturday) {
        if (targetSaturday.getDayOfWeek() != java.time.DayOfWeek.SATURDAY) {
            throw new IllegalArgumentException("The specified date must fall explicitly on a Saturday.");
        }

        // Clean out any existing structural default "OD" or auto-holiday indicators mapped to this target date window
        Optional<List<OfficeStaffAttendance>> existingHolidays =
                Optional.ofNullable(attendanceRepository.findByAttendanceDate(targetSaturday)); // Depends on repo mapping

        existingHolidays.ifPresent(records -> records.stream()
                .filter(a -> "OD".equalsIgnoreCase(a.getStatus()))
                .forEach(attendanceRepository::delete));

        return "Saturday schedule modified successfully! " + targetSaturday + " is registered as an active mandatory business operations shift.";
    }
}