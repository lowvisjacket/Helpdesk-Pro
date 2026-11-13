package org.dev.helpdesk_pro.Data.Views;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavedViewRepository extends JpaRepository<SavedView, Long> {

    List<SavedView> findByUsernameOrderByDisplayOrderAscIdAsc(String username);
}
