package com.example.MpApp.service.officestaff;

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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OfficeStaffAttendanceService {

    private final OfficeStaffAttendanceRepository attendanceRepository;
    private final OfficeStaffRepository staffRepository;
    private final Clock clock;

    private static final LocalTime CUTOFF_CHECKIN = LocalTime.of(9, 3); // 09:03 AM
    private static final LocalTime CLOSING_TIME = LocalTime.of(18, 0);   // 06:00 PM

    // 1. STAFF CHECK-IN RULE
    @Transactional
    public OfficeStaffAttendance checkIn(Long staffId) {
        OfficeStaff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff member not found."));

        // Automatically capturing the current date and time via the injected clock
        LocalDate today = LocalDate.now(clock);
        LocalTime now = LocalTime.now(clock);

        Optional<OfficeStaffAttendance> existingAttendance =
                attendanceRepository.findByStaffIdAndAttendanceDate(staffId, today);

        if (existingAttendance.isPresent()) {
            throw new IllegalStateException("Already checked in for today!");
        }

        OfficeStaffAttendance attendance = new OfficeStaffAttendance();
        attendance.setStaff(staff);
        attendance.setAttendanceDate(today);
        attendance.setCheckInTime(now);

        // Enforce the strict 9:03 AM boundary rule automatically
        if (now.isAfter(CUTOFF_CHECKIN)) {
            attendance.setStatus("ABSENT");
        } else {
            attendance.setStatus("PRESENT");
        }

        return attendanceRepository.save(attendance);
    }

    // 2. STAFF CHECK-OUT RULE
    @Transactional
    public OfficeStaffAttendance checkOut(Long staffId) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        OfficeStaffAttendance attendance = attendanceRepository.findByStaffIdAndAttendanceDate(staffId, today)
                .orElseThrow(() -> new IllegalStateException("No Check-In entry logged for today."));

        if (attendance.getCheckOutTime() != null) {
            throw new IllegalStateException("Already checked out for today.");
        }

        attendance.setCheckOutTime(now);

        // Optional: If you want to penalize leaving early even if they checked in on time:
        if (now.isBefore(CLOSING_TIME)) {
            // Can degrade status or flags if they leave before 6:00 PM
            // e.g., attendance.setStatus("LEFT_EARLY");
        }

        return attendanceRepository.save(attendance);
    }

    // 3. ADMIN BUSINESS RULE: MARK HOLIDAY / ON-DUTY (OD) FOR ALL STAFF
    @Transactional
    public String markHolidayOD(LocalDate holidayDate) {
        List<OfficeStaff> allStaff = staffRepository.findAll();

        for (OfficeStaff staff : allStaff) {
            // Skip if record already initialized for that day
            Optional<OfficeStaffAttendance> existing = attendanceRepository.findByStaffIdAndAttendanceDate(staff.getId(), holidayDate);

            if (existing.isPresent()) {
                OfficeStaffAttendance attendance = existing.get();
                attendance.setStatus("OD");
                attendance.setCheckInTime(null);  // Overwritten as office closed
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
}