package com.school.erp.seed;

import com.school.erp.entity.*;
import com.school.erp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Profile("!test") // Don't run in tests
public class LoginAccessDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final UserActivityLogRepository activityLogRepository;
    private final UserLoginHistoryRepository loginHistoryRepository;
    private final AccountRequestRepository accountRequestRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (activityLogRepository.count() > 0) {
            return; // Already seeded
        }

        List<User> users = userRepository.findAll();
        List<School> schools = schoolRepository.findAll();

        if (users.isEmpty() || schools.isEmpty()) {
            return; // Can't seed without base data
        }

        Random rand = new Random();
        School mainSchool = schools.get(0);

        // Seed Activity Logs
        for (int i = 0; i < 50; i++) {
            User u = users.get(rand.nextInt(users.size()));
            UserActivityLog log = new UserActivityLog();
            log.setUser(u);
            log.setSchool(mainSchool);
            log.setRole(rand.nextBoolean() ? "ADMIN" : "TEACHER");
            log.setModule(new String[]{"USER_MANAGEMENT", "SETTINGS", "ACADEMICS", "SUPPORT"}[rand.nextInt(4)]);
            log.setAction(new String[]{"CREATED_RECORD", "UPDATED_SETTINGS", "DELETED_USER", "DOWNLOADED_REPORT"}[rand.nextInt(4)]);
            log.setIpAddress("192.168.1." + (rand.nextInt(200) + 1));
            log.setBrowser(new String[]{"Chrome", "Firefox", "Safari", "Edge"}[rand.nextInt(4)]);
            log.setDevice(new String[]{"Desktop", "Mobile", "Tablet"}[rand.nextInt(3)]);
            log.setStatus(rand.nextInt(10) > 8 ? "FAILED" : "SUCCESS");
            log.setTimestamp(LocalDateTime.now().minusHours(rand.nextInt(720)));
            activityLogRepository.save(log);
        }

        // Seed Login History
        for (int i = 0; i < 30; i++) {
            User u = users.get(rand.nextInt(users.size()));
            UserLoginHistory hist = new UserLoginHistory();
            hist.setUser(u);
            hist.setSchool(mainSchool);
            hist.setDevice(new String[]{"MacBook Pro", "Windows PC", "iPhone 13", "iPad"}[rand.nextInt(4)]);
            hist.setBrowser(new String[]{"Chrome 114", "Safari 16", "Firefox 112"}[rand.nextInt(3)]);
            hist.setIpAddress("10.0.0." + (rand.nextInt(200) + 1));
            hist.setStatus(rand.nextInt(10) > 8 ? "FAILED" : "SUCCESS");
            LocalDateTime loginTime = LocalDateTime.now().minusHours(rand.nextInt(240));
            hist.setLoginTime(loginTime);
            if ("SUCCESS".equals(hist.getStatus()) && rand.nextBoolean()) {
                hist.setLogoutTime(loginTime.plusMinutes(rand.nextInt(120) + 5));
            }
            loginHistoryRepository.save(hist);
        }

        // Seed Account Requests
        for (int i = 0; i < 15; i++) {
            AccountRequest req = new AccountRequest();
            req.setSchool(mainSchool);
            req.setRequestType("ACCOUNT_CREATION");
            req.setRequesterName("Pending User " + i);
            req.setRequesterEmail("pending" + i + "@example.com");
            req.setRequesterPhone("555-010" + i);
            req.setRequestedRole(new String[]{"TEACHER", "PARENT", "STAFF"}[rand.nextInt(3)]);
            req.setDescription("I would like to join the school as a " + req.getRequestedRole().toLowerCase());
            
            int statusRand = rand.nextInt(3);
            if (statusRand == 0) {
                req.setStatus("PENDING");
            } else if (statusRand == 1) {
                req.setStatus("APPROVED");
                req.setResolvedAt(LocalDateTime.now().minusDays(rand.nextInt(10)));
                req.setUser(users.get(rand.nextInt(users.size())));
            } else {
                req.setStatus("REJECTED");
                req.setRejectReason("Duplicate account suspected");
                req.setResolvedAt(LocalDateTime.now().minusDays(rand.nextInt(10)));
            }
            accountRequestRepository.save(req);
        }
    }
}
