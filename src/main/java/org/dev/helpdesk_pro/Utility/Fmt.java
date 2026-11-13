package org.dev.helpdesk_pro.Utility;

import org.dev.helpdesk_pro.Enum.TicketDepartment;
import org.dev.helpdesk_pro.Enum.TicketImportance;
import org.dev.helpdesk_pro.Enum.TicketStatus;
import org.dev.helpdesk_pro.Enum.UserRole;
import org.springframework.stereotype.Component;

import java.util.Locale;

/** Small formatting helpers for the templates, used as ${@fmt.label(...)}. */
@Component("fmt")
public class Fmt {

    private static final java.util.Set<String> NAME_SUFFIXES = java.util.Set.of("jr", "sr", "ii", "iii", "iv", "v");

    /** IN_PROGRESS -> "In progress". Anything that is not an enum-style constant is returned unchanged. */
    public String label(Object value) {
        if (value == null) {
            return "";
        }
        String raw = value.toString();
        if (!raw.matches("[A-Z][A-Z0-9_]*")) {
            return raw;
        }
        String text = raw.replace('_', ' ').toLowerCase(Locale.ROOT);
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    /**
     * Up to two initials: the first letter of the first and last name parts. Works on code points, so
     * accented, CJK and emoji-prefixed names are safe, and suffixes such as "III" are ignored.
     */
    public String initials(String name) {
        java.util.List<String> parts = new java.util.ArrayList<>();
        if (name != null) {
            // Real names split on spaces ("Wiśniewska-Kowalczyk" is one part); usernames split on dots and dashes.
            String trimmed = name.trim();
            for (String part : trimmed.split(trimmed.matches(".*\\s.*") ? "\\s+" : "[._-]+")) {
                if (part.isEmpty() || !Character.isLetterOrDigit(part.codePointAt(0))) {
                    continue;
                }
                if (!parts.isEmpty() && NAME_SUFFIXES.contains(part.toLowerCase(Locale.ROOT).replace(".", ""))) {
                    continue;
                }
                parts.add(part);
            }
        }
        if (parts.isEmpty()) {
            return "?";
        }
        String first = new String(Character.toChars(parts.get(0).codePointAt(0)));
        String last = parts.size() > 1 ? new String(Character.toChars(parts.get(parts.size() - 1).codePointAt(0))) : "";
        return (first + last).toUpperCase(Locale.ROOT);
    }

    /** UTC timestamp for a &lt;time datetime&gt; attribute, or null when the stored values are not parseable. */
    public String iso(String date, String time) {
        try {
            java.time.LocalDate day = java.time.LocalDate.parse(date);
            java.time.LocalTime clock = time == null ? java.time.LocalTime.MIDNIGHT : java.time.LocalTime.parse(time);
            return day + "T" + clock.withNano(0).format(java.time.format.DateTimeFormatter.ISO_LOCAL_TIME) + "Z";
        } catch (RuntimeException unparseable) {
            return null;
        }
    }

    /** 1284 -> "1,284 tickets"; 1 -> "1 ticket". */
    public String count(long number, String singular, String plural) {
        return String.format(Locale.US, "%,d", number) + " " + (number == 1 ? singular : plural);
    }

    public String number(Number number) {
        return number == null ? "0" : String.format(Locale.US, "%,d", number.longValue());
    }

    /** "12:34:56.789" -> "12:34". */
    public String time(String time) {
        return time == null ? "" : time.length() > 5 ? time.substring(0, 5) : time;
    }

    public TicketStatus[] statuses() {
        return TicketStatus.values();
    }

    public TicketImportance[] importances() {
        return TicketImportance.values();
    }

    public TicketDepartment[] departments() {
        return TicketDepartment.values();
    }

    public java.util.List<String> columns() {
        return org.dev.helpdesk_pro.Logic.TicketFilter.COLUMNS;
    }

    public java.util.List<String> sorts() {
        return org.dev.helpdesk_pro.Logic.TicketFilter.SORTS;
    }

    public UserRole[] roles() {
        return UserRole.values();
    }
}
