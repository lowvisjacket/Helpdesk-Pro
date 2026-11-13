package org.dev.helpdesk_pro.TestData;

import org.dev.helpdesk_pro.Data.Tickets.Ticket;
import org.dev.helpdesk_pro.Data.Tickets.TicketNote;
import org.dev.helpdesk_pro.Data.Tickets.TicketRepository;
import org.dev.helpdesk_pro.Data.Users.User;
import org.dev.helpdesk_pro.Data.Users.UserRepository;
import org.dev.helpdesk_pro.Enum.TicketDepartment;
import org.dev.helpdesk_pro.Enum.TicketImportance;
import org.dev.helpdesk_pro.Enum.TicketStatus;
import org.dev.helpdesk_pro.Enum.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Development-only fixture: realistic worst-case data for stress-testing the UI (very long and very
 * short names, unbreakable strings, right-to-left and CJK text, markup-looking text, every status,
 * a long conversation and more than a thousand tickets).
 *
 * Enable it together with the demo data: --spring.profiles.active=demo,worstcase
 * It never runs in production because it needs the "worstcase" profile.
 */
@Component
@Profile("worstcase")
@Order(2)
public class WorstCaseSeeder implements CommandLineRunner {

    private static final String LONG_URL =
            "https://example.com/workspaces/northwind/projects/q3-launch/docs/9f8e7d6c5b4a?tab=comments&filter=unresolved";

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final PasswordEncoder passwordEncoder;
    private final String seedPassword;

    public WorstCaseSeeder(UserRepository userRepository, TicketRepository ticketRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${demo.seed.password:test123}") String seedPassword) {
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
        this.passwordEncoder = passwordEncoder;
        this.seedPassword = seedPassword;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByUsername("wc.user")) {
            return;
        }
        String hash = passwordEncoder.encode(seedPassword);
        user(hash, "wc.user", "Aleksandra", "Wiśniewska-Kowalczyk", "CLINICAL_INFORMATICS", UserRole.USER, true);
        user(hash, "wc.tech", "Christopher Alexander", "Montgomery III", "INFORMATION_TECHNOLOGY", UserRole.TECHNICIAN, true);
        user(hash, "wc.admin", "María José", "de la Cruz y Fernández", "INFORMATION_TECHNOLOGY", UserRole.ADMIN, true);
        user(hash, "wc.sys", "Jo", "Ng", "INFORMATION_TECHNOLOGY", UserRole.SYSADMIN, true);
        user(hash, "j", "J", "X", "FINANCE", UserRole.USER, true);
        user(hash, "thisisaverylongusername.allowed32", "Konstantin", "Oberhauser-Wettstein",
                "Paramètres de confidentialité et de sécurité", UserRole.USER, true);
        user(hash, "dang.han", "Đặng Thị Ngọc", "Hân", "HUMAN_RESOURCES", UserRole.USER, true);
        user(hash, "wang.xiuying", "王", "秀英", "ENGINEERING", UserRole.USER, false);
        user(hash, "nour", "نور الهدى", "عبد الرحمن", "MARKETING", UserRole.USER, true);
        user(hash, "fox", "🦊 Fox", "lee", "Benachrichtigungseinstellungen", UserRole.TECHNICIAN, true);
        user(hash, "dana", "dana", "o'brien-ó súilleabháin", "SECURITY", UserRole.USER, true);

        String requester = "Aleksandra Wiśniewska-Kowalczyk";
        List<Ticket> tickets = new ArrayList<>();

        Ticket longTitle = ticket(requester, "wc.user",
                "Shared mailbox for the Clinical Informatics quarterly governance review stops syncing on every laptop "
                        + "after the certificate rollover, and calendar invitations sent from it arrive without attachments",
                ("Since the certificate rollover on Monday the shared mailbox no longer syncs. Steps we have tried:\n\n"
                        + "1. Removed and re-added the account on three laptops.\n"
                        + "2. Cleared the offline cache.\n"
                        + "3. Signed in through the web client, which works.\n\n"
                        + "The full export of the sync log is at " + LONG_URL + "\n\n").repeat(4).trim(),
                TicketStatus.IN_PROGRESS, TicketImportance.CRITICAL, TicketDepartment.CLINICAL_INFORMATICS,
                "Christopher Alexander Montgomery III", "wc.tech", "2026-11-12", "09:12:44.120");
        for (int i = 1; i <= 34; i++) {
            boolean staff = i % 3 != 0;
            note(longTitle, staff ? "wc.tech" : "wc.user", i % 7 == 0,
                    i == 5 ? "IMG_20250914_183022_HDR_portrait_edited_edited_final_v12_approved_by_legal_do_not_rename.HEIC"
                            : i == 9 ? "<script>alert(1)</script> &amp; **bold** <b>not bold</b>"
                            : i == 12 ? "ok"
                            : i == 14 ? "Checked the gateway. " + "The rollover replaced the intermediate certificate, so clients pinned to the old chain reject the connection. ".repeat(12).trim()
                            : (staff ? "Update " : "Reply ") + i + ": still reproducing on the second laptop after the cache reset.",
                    "2026-11-12", String.format("%02d:%02d:00", 9 + i / 6, (i * 7) % 60));
        }
        tickets.add(longTitle);

        tickets.add(ticket("Jo", "wc.user", "?", "x", TicketStatus.OPEN, TicketImportance.LOW,
                TicketDepartment.FINANCE, null, null, "2026-11-12", "23:59:59"));
        tickets.add(ticket("Christopher Alexander Montgomery III", "wc.user",
                "IMG_20250914_183022_HDR_portrait_edited_edited.HEIC_upload_fails_with_error_0x80070057_on_SharePoint",
                LONG_URL, TicketStatus.PENDING, TicketImportance.HIGH, TicketDepartment.INFORMATION_TECHNOLOGY,
                "Jo Ng", "wc.sys", "2025-12-31", "23:30:00"));
        tickets.add(ticket("  Sam   Lee ", "wc.user", "<script>alert(1)</script> &amp; **bold** <b>title</b>",
                "<img src=x onerror=alert(1)> must be shown as text.\nLine two.", TicketStatus.PAUSED,
                TicketImportance.MEDIUM, TicketDepartment.SECURITY, "🦊 Fox lee", "fox", "1970-01-01", "00:00:00"));
        tickets.add(ticket("نور الهدى عبد الرحمن", "nour", "الطابعة في الطابق الثالث لا تعمل منذ الصباح (HP-LaserJet-4200)",
                "لا يمكن الطباعة من أي جهاز. رسالة الخطأ: PCL XL error.", TicketStatus.RESOLVED,
                TicketImportance.LOW, TicketDepartment.MAINTENANCE, "María José de la Cruz y Fernández", "wc.admin",
                "2027-03-01", "08:00:00"));
        tickets.add(ticket("王秀英", "wang.xiuying", "新入职员工的笔记本电脑无法连接公司无线网络并且无法安装打印机驱动程序请尽快处理谢谢",
                "Đặng Thị Ngọc Hân cũng gặp lỗi tương tự.", TicketStatus.CLOSED, TicketImportance.HIGH,
                TicketDepartment.ENGINEERING, null, null, "2023-02-28", "12:00:00"));
        tickets.add(ticket("Đặng Thị Ngọc Hân", "dang.han", "Benachrichtigungseinstellungen 👩🏽‍💻 zurücksetzen",
                "Paramètres de confidentialité et de sécurité", TicketStatus.OPEN, TicketImportance.CRITICAL,
                TicketDepartment.HUMAN_RESOURCES, "Christopher Alexander Montgomery III", "wc.tech", "2026-11-11", "07:05:09"));
        ticketRepository.saveAll(tickets);

        // More than a thousand ordinary tickets: the list must stay fast and paginated.
        TicketStatus[] statuses = TicketStatus.values();
        TicketImportance[] importances = TicketImportance.values();
        TicketDepartment[] departments = TicketDepartment.values();
        List<Ticket> bulk = new ArrayList<>();
        for (int i = 1; i <= 1284; i++) {
            bulk.add(ticket(requester, "wc.user", "Bulk ticket " + i + ": password reset for the HR portal",
                    "Generated worst-case row " + i + ".", statuses[i % statuses.length], importances[i % importances.length],
                    departments[i % departments.length], i % 4 == 0 ? null : "Jo Ng", i % 4 == 0 ? null : "wc.sys",
                    String.format("2026-%02d-%02d", 1 + i % 11, 1 + i % 28), String.format("%02d:%02d:00", i % 24, i % 60)));
        }
        ticketRepository.saveAll(bulk);
    }

    private void user(String hash, String username, String first, String last, String department, UserRole role, boolean enabled) {
        User user = new User();
        user.setUsername(username);
        user.setFirstName(first);
        user.setLastName(last);
        user.setDepartment(department);
        user.setRole(role);
        user.setPassword(hash);
        user.setAccountStatus(enabled);
        user.setLoginAttempts(enabled ? 0L : 4L);
        userRepository.save(user);
    }

    private static Ticket ticket(String requestor, String requestorUsername, String title, String description,
                                 TicketStatus status, TicketImportance importance, TicketDepartment department,
                                 String agent, String agentUsername, String date, String time) {
        Ticket ticket = new Ticket(title, description, department, requestor, requestorUsername);
        ticket.setTicketStatus(status);
        ticket.setImportance(importance);
        ticket.setAgent(agent);
        ticket.setAgentUsername(agentUsername);
        ticket.setTicketDate(date);
        ticket.setTicketTime(time);
        ticket.touch(requestorUsername, false);
        return ticket;
    }

    private static void note(Ticket ticket, String username, boolean internal, String text, String date, String time) {
        TicketNote note = new TicketNote(ticket, text, username);
        note.setInternalNote(internal);
        note.setTicketNoteDate(date);
        note.setTicketNoteTime(time);
        ticket.getNotes().add(note);
    }
}
