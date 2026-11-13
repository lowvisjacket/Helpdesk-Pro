package org.dev.helpdesk_pro.Controller;

import org.dev.helpdesk_pro.Data.Users.User;
import org.dev.helpdesk_pro.Data.Users.UserRepository;
import org.dev.helpdesk_pro.Data.Views.SavedView;
import org.dev.helpdesk_pro.Data.Views.SavedViewRepository;
import org.dev.helpdesk_pro.Exceptions.UserNotFoundException;
import org.dev.helpdesk_pro.Logic.TicketFilter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

/** Per-user preferences (theme, notification sound) and saved ticket views. */
@Controller
@PreAuthorize("isAuthenticated()")
public class SettingsEndpoint {

    private static final String STAFF = "hasAnyRole('TECHNICIAN','ADMIN','SYSADMIN')";
    private static final int MAX_VIEWS = 20;

    private final UserRepository userRepository;
    private final SavedViewRepository savedViewRepository;

    public SettingsEndpoint(UserRepository userRepository, SavedViewRepository savedViewRepository) {
        this.userRepository = userRepository;
        this.savedViewRepository = savedViewRepository;
    }

    @GetMapping("/settings")
    public String getSettings() {
        return "settings";
    }

    @PostMapping("/settings/preferences")
    public ResponseEntity<Void> updatePreferences(
            @RequestParam(required = false) String theme,
            @RequestParam(required = false) Boolean soundMuted,
            Principal principal
    ) {
        User user = userRepository.findByUsername(principal.getName());
        if (user == null) {
            throw new UserNotFoundException("User does not exist.");
        }
        if (theme != null) {
            user.setTheme("light".equals(theme) || "dark".equals(theme) ? theme : null);
        }
        if (soundMuted != null) {
            user.setSoundMuted(soundMuted);
        }
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(STAFF)
    @PostMapping("/views")
    public String createView(
            @RequestParam String name,
            @RequestParam(defaultValue = "") String filters,
            @RequestParam(defaultValue = "") String columns,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "false") boolean makeDefault,
            Principal principal
    ) {
        List<SavedView> views = views(principal);
        if (views.size() >= MAX_VIEWS) {
            throw new IllegalArgumentException("You can save up to " + MAX_VIEWS + " views. Delete one first.");
        }
        SavedView view = new SavedView();
        view.setUsername(principal.getName());
        view.setName(cleanName(name));
        view.setDisplayOrder(views.isEmpty() ? 0 : views.get(views.size() - 1).getDisplayOrder() + 1);
        apply(view, filters, columns, sort);
        savedViewRepository.save(view);
        if (makeDefault) {
            setDefault(views, view);
        }
        return "redirect:/dashboard/?view=" + view.getId();
    }

    /** Overwrites an existing view with the filters currently on screen. */
    @PreAuthorize(STAFF)
    @PostMapping("/views/{id}/save")
    public String saveView(
            @PathVariable Long id,
            @RequestParam(defaultValue = "") String filters,
            @RequestParam(defaultValue = "") String columns,
            @RequestParam(defaultValue = "newest") String sort,
            Principal principal
    ) {
        SavedView view = owned(views(principal), id);
        apply(view, filters, columns, sort);
        savedViewRepository.save(view);
        return "redirect:/dashboard/?view=" + id;
    }

    @PreAuthorize(STAFF)
    @PostMapping("/views/{id}/rename")
    public String renameView(@PathVariable Long id, @RequestParam String name, Principal principal) {
        SavedView view = owned(views(principal), id);
        view.setName(cleanName(name));
        savedViewRepository.save(view);
        return "redirect:/settings#views";
    }

    @PreAuthorize(STAFF)
    @PostMapping("/views/{id}/move")
    public String moveView(@PathVariable Long id, @RequestParam String dir, Principal principal) {
        List<SavedView> views = new ArrayList<>(views(principal));
        int index = views.indexOf(owned(views, id));
        int target = "up".equals(dir) ? index - 1 : index + 1;
        if (target >= 0 && target < views.size()) {
            views.add(target, views.remove(index));
            for (int i = 0; i < views.size(); i++) {
                views.get(i).setDisplayOrder(i);
            }
            savedViewRepository.saveAll(views);
        }
        return "redirect:/settings#views";
    }

    /** Makes this the view the ticket list opens with. Choosing the current default clears it. */
    @PreAuthorize(STAFF)
    @PostMapping("/views/{id}/default")
    public String defaultView(@PathVariable Long id, Principal principal) {
        List<SavedView> views = views(principal);
        SavedView view = owned(views, id);
        setDefault(views, view.isDefaultView() ? null : view);
        return "redirect:/settings#views";
    }

    @PreAuthorize(STAFF)
    @PostMapping("/views/{id}/delete")
    public String deleteView(@PathVariable Long id, Principal principal) {
        savedViewRepository.delete(owned(views(principal), id));
        return "redirect:/settings#views";
    }

    private List<SavedView> views(Principal principal) {
        return savedViewRepository.findByUsernameOrderByDisplayOrderAscIdAsc(principal.getName());
    }

    /** Views are looked up within the caller's own list, so nobody can touch another user's view. */
    private static SavedView owned(List<SavedView> views, Long id) {
        return views.stream().filter(view -> view.getId().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("That view no longer exists."));
    }

    private void setDefault(List<SavedView> views, SavedView chosen) {
        List<SavedView> all = new ArrayList<>(views);
        if (chosen != null && all.stream().noneMatch(view -> view.getId().equals(chosen.getId()))) {
            all.add(chosen);
        }
        all.forEach(view -> view.setDefaultView(chosen != null && view.getId().equals(chosen.getId())));
        savedViewRepository.saveAll(all);
    }

    private static void apply(SavedView view, String filters, String columns, String sort) {
        TicketFilter filter = TicketFilter.fromQuery(filters, sort);
        view.setFilters(filter.toQuery());
        view.setSortOrder(filter.sort());
        view.setVisibleColumns(String.join(",", TicketFilter.columns(columns)));
    }

    private static String cleanName(String name) {
        String cleaned = name == null ? "" : name.trim();
        if (cleaned.isEmpty() || cleaned.length() > 60) {
            throw new IllegalArgumentException("Give the view a name of up to 60 characters.");
        }
        return cleaned;
    }
}
