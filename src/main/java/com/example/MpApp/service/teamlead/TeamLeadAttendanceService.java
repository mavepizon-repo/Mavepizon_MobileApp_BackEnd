package com.example.MpApp.service.teamlead;

import com.example.MpApp.dto.officestaff.CheckInRequestDTO;
import com.example.MpApp.entity.teamlead.TeamLead;
import com.example.MpApp.entity.teamlead.TeamLeadAttendance;
import com.example.MpApp.repository.teamlead.TeamLeadAttendanceRepository;
import com.example.MpApp.repository.teamlead.TeamLeadRepository;
import com.example.MpApp.exception.ResourceNotFoundException;
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
public class TeamLeadAttendanceService {

    private final TeamLeadAttendanceRepository attendanceRepository;
    private final TeamLeadRepository teamLeadRepository;
    private final Clock clock;

    private static final LocalTime CUTOFF_CHECKIN = LocalTime.of(9, 3);
    private static final LocalTime CLOSING_TIME = LocalTime.of(18, 0);
    private static final double MAX_ALLOWED_DISTANCE_METERS = 100.0;

    // Reuse the same coordinate map
    private static final Map<String, Coordinate> BRANCH_COORDINATES = Map.of(
            "TIRUNELVELI", new Coordinate(8.718412865303698, 77.73201179302228),
            "THISAYANVILLAI", new Coordinate(8.337947315572267, 77.86680851969136)
    );

    private static class Coordinate {
        final double latitude; final double longitude;
        Coordinate(double lat, double lon) { this.latitude = lat; this.longitude = lon; }
    }

    @Transactional
    public TeamLeadAttendance checkIn(Long teamLeadId, CheckInRequestDTO locationRequest) {
        LocalDate today = LocalDate.now(clock);
        LocalTime now = LocalTime.now(clock);

        TeamLead tl = teamLeadRepository.findById(teamLeadId)
                .orElseThrow(() -> new ResourceNotFoundException("Team Lead not found."));

        String branch = tl.getBranch() != null ? tl.getBranch().toUpperCase() : "TIRUNELVELI";
        Coordinate geo = BRANCH_COORDINATES.get(branch);

        if (geo == null) throw new IllegalArgumentException("No geo-fence for: " + branch);

        double distance = calculateHaversineDistance(locationRequest.getLatitude(), locationRequest.getLongitude(), geo.latitude, geo.longitude);

        if (distance > MAX_ALLOWED_DISTANCE_METERS) {
            throw new IllegalArgumentException("Distance exceeds limit of " + MAX_ALLOWED_DISTANCE_METERS + "m");
        }

        if (now.isAfter(CUTOFF_CHECKIN)) throw new IllegalStateException("Check-in period closed.");

        if (attendanceRepository.findByTeamLeadIdAndAttendanceDate(teamLeadId, today).isPresent()) {
            throw new IllegalStateException("Already checked in today.");
        }

        TeamLeadAttendance attendance = new TeamLeadAttendance();
        attendance.setTeamLead(tl);
        attendance.setAttendanceDate(today);
        attendance.setCheckInTime(now);
        attendance.setStatus("PRESENT");

        return attendanceRepository.save(attendance);
    }

    @Transactional
    public TeamLeadAttendance checkOut(Long teamLeadId) {
        LocalDate today = LocalDate.now(clock);
        LocalTime now = LocalTime.now(clock);

        TeamLeadAttendance attendance = attendanceRepository.findByTeamLeadIdAndAttendanceDate(teamLeadId, today)
                .orElseThrow(() -> new IllegalStateException("No Check-In found."));

        if (!"PRESENT".equals(attendance.getStatus())) throw new IllegalStateException("Check-out rejected.");

        attendance.setCheckOutTime(now);
        if (now.isBefore(CLOSING_TIME)) attendance.setStatus("LEAVE_EARLY");

        return attendanceRepository.save(attendance);
    }

    public List<TeamLeadAttendance> getHistory(Long teamLeadId) {
        return attendanceRepository.findByTeamLeadId(teamLeadId);
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public double calculateAttendancePercentage(Long teamLeadId) {
        // 1. Fetch the history using your existing getHistory method
        List<TeamLeadAttendance> history = getHistory(teamLeadId);

        if (history.isEmpty()) {
            return 0.0;
        }

        long totalDays = history.size();

        // 2. Count days where status is 'PRESENT'
        long presentDays = history.stream()
                .filter(a -> "PRESENT".equalsIgnoreCase(a.getStatus()))
                .count();

        // 3. Return the calculated percentage
        return ((double) presentDays / totalDays) * 100;
    }
}