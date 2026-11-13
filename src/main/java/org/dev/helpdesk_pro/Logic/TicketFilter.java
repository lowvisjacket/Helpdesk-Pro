package org.dev.helpdesk_pro.Logic;

import org.dev.helpdesk_pro.Data.Tickets.Ticket;
import org.dev.helpdesk_pro.Enum.TicketDepartment;
import org.dev.helpdesk_pro.Enum.TicketImportance;
import org.dev.helpdesk_pro.Enum.TicketStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Filter and sort settings for the staff ticket list. Every value is validated, so it is safe to store and replay. */
public record TicketFilter(
        Set<TicketStatus> statuses,
        Set<TicketImportance> importances,
        TicketDepartment department,
        String assignee,
        String q,
        String sort
) {
    public static final List<String> COLUMNS = List.of("requester", "status", "priority", "department", "assignee", "created");
    public static final List<String> SORTS = List.of("newest", "oldest", "priority", "updated");
    private static final List<String> ASSIGNEES = List.of("any", "me", "unassigned");

    /** The built-in view: every ticket that is not resolved or closed. */
    public static TicketFilter open() {
        return new TicketFilter(
                EnumSet.of(TicketStatus.OPEN, TicketStatus.PENDING, TicketStatus.PAUSED, TicketStatus.IN_PROGRESS),
                EnumSet.noneOf(TicketImportance.class), null, "any", "", "newest");
    }

    public static TicketFilter of(Collection<String> status, Collection<String> importance, String department,
                                  String assignee, String q, String sort) {
        Set<TicketStatus> statuses = EnumSet.noneOf(TicketStatus.class);
        if (status != null) {
            status.forEach(value -> { TicketStatus s = parse(TicketStatus.class, value); if (s != null) statuses.add(s); });
        }
        Set<TicketImportance> importances = EnumSet.noneOf(TicketImportance.class);
        if (importance != null) {
            importance.forEach(value -> { TicketImportance i = parse(TicketImportance.class, value); if (i != null) importances.add(i); });
        }
        String text = q == null ? "" : q.trim();
        if (text.length() > 100) {
            text = text.substring(0, 100);
        }
        return new TicketFilter(statuses, importances, parse(TicketDepartment.class, department),
                assignee != null && ASSIGNEES.contains(assignee) ? assignee : "any", text,
                sort != null && SORTS.contains(sort) ? sort : "newest");
    }

    /** Rebuilds a filter from the string written by {@link #toQuery()}. */
    public static TicketFilter fromQuery(String filters, String sort) {
        MultiValueMap<String, String> params = UriComponentsBuilder.newInstance()
                .query(filters == null || filters.isBlank() ? null : filters).build().getQueryParams();
        String q = params.getFirst("q");
        return of(params.get("status"), params.get("importance"), params.getFirst("department"),
                params.getFirst("assignee"), q == null ? null : URLDecoder.decode(q, StandardCharsets.UTF_8), sort);
    }

    public String toQuery() {
        StringBuilder query = new StringBuilder();
        statuses.forEach(s -> query.append("&status=").append(s.name()));
        importances.forEach(i -> query.append("&importance=").append(i.name()));
        if (department != null) {
            query.append("&department=").append(department.name());
        }
        if (!"any".equals(assignee)) {
            query.append("&assignee=").append(assignee);
        }
        if (!q.isEmpty()) {
            query.append("&q=").append(URLEncoder.encode(q, StandardCharsets.UTF_8));
        }
        return query.isEmpty() ? "" : query.substring(1);
    }

    /** Parses a comma separated column list. Null means "all columns". */
    public static Set<String> columns(String csv) {
        if (csv == null) {
            return new LinkedHashSet<>(COLUMNS);
        }
        Set<String> wanted = Set.of(csv.split(","));
        Set<String> columns = new LinkedHashSet<>();
        COLUMNS.forEach(column -> { if (wanted.contains(column)) columns.add(column); });
        return columns;
    }

    public boolean matches(Ticket ticket, String username) {
        if (!statuses.isEmpty() && !statuses.contains(ticket.getTicketStatus())) {
            return false;
        }
        if (!importances.isEmpty() && !importances.contains(ticket.getImportance())) {
            return false;
        }
        if (department != null && department != ticket.getDepartment()) {
            return false;
        }
        if ("me".equals(assignee) && !username.equals(ticket.getAgentUsername())) {
            return false;
        }
        if ("unassigned".equals(assignee) && ticket.getAgentUsername() != null) {
            return false;
        }
        if (!q.isEmpty()) {
            String needle = q.toLowerCase(Locale.ROOT);
            boolean inTitle = ticket.getTitle() != null && ticket.getTitle().toLowerCase(Locale.ROOT).contains(needle);
            boolean inRequester = ticket.getRequestor() != null && ticket.getRequestor().toLowerCase(Locale.ROOT).contains(needle);
            return inTitle || inRequester || needle.equals(String.valueOf(ticket.getId()));
        }
        return true;
    }

    public Comparator<Ticket> comparator() {
        Comparator<Ticket> created = Comparator.comparing(
                ticket -> String.valueOf(ticket.getTicketDate()) + ' ' + ticket.getTicketTime());
        Comparator<Ticket> newest = created.reversed().thenComparing(Ticket::getId, Comparator.reverseOrder());
        return switch (sort) {
            case "oldest" -> created.thenComparing(Ticket::getId);
            case "priority" -> Comparator.<Ticket, Integer>comparing(
                    ticket -> ticket.getImportance() == null ? -1 : ticket.getImportance().ordinal()).reversed().thenComparing(newest);
            case "updated" -> Comparator.<Ticket, Long>comparing(
                    ticket -> ticket.getStaffUpdatedAt() == null ? 0L : ticket.getStaffUpdatedAt()).reversed().thenComparing(newest);
            default -> newest;
        };
    }

    private static <E extends Enum<E>> E parse(Class<E> type, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(type, value.trim());
        } catch (IllegalArgumentException invalid) {
            return null;
        }
    }
}
