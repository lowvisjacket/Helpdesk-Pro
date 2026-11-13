package org.dev.helpdesk_pro.Utility;

import org.dev.helpdesk_pro.Data.Users.User;
import org.dev.helpdesk_pro.Data.Users.UserRepository;
import org.dev.helpdesk_pro.Data.Views.SavedViewRepository;
import org.dev.helpdesk_pro.Enum.UserRole;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

/** Adds the signed-in user's display details and UI preferences to every page model. */
@ControllerAdvice
public class GlobalModel {

    private final UserRepository userRepository;
    private final SavedViewRepository savedViewRepository;

    public GlobalModel(UserRepository userRepository, SavedViewRepository savedViewRepository) {
        this.userRepository = userRepository;
        this.savedViewRepository = savedViewRepository;
    }

    /** Never exposes the user entity (and its password hash) to the templates. */
    @lombok.Getter
    @lombok.AllArgsConstructor
    public static class Me {
        private final String username;
        private final String fullName;
        private final String department;
        private final UserRole role;
        private final boolean staff;
        private final String theme;
        private final boolean soundMuted;
    }

    @ModelAttribute
    public void populate(Principal principal, Model model) {
        model.addAttribute("now", System.currentTimeMillis());
        if (principal == null) {
            return;
        }
        User user = userRepository.findByUsername(principal.getName());
        if (user == null) {
            return;
        }
        boolean staff = user.getRole() != UserRole.USER;
        String theme = "light".equals(user.getTheme()) || "dark".equals(user.getTheme()) ? user.getTheme() : "";
        model.addAttribute("me", new Me(user.getUsername(), user.getFirstName() + " " + user.getLastName(),
                user.getDepartment(), user.getRole(), staff, theme, Boolean.TRUE.equals(user.getSoundMuted())));
        model.addAttribute("staff", staff);
        if (staff) {
            model.addAttribute("navViews", savedViewRepository.findByUsernameOrderByDisplayOrderAscIdAsc(user.getUsername()));
        }
    }
}
