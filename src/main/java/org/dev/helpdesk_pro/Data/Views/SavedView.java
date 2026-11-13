package org.dev.helpdesk_pro.Data.Views;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** A ticket list view saved by one user: filters, visible columns and sort order. */
@Entity
@Table(name = "saved_views")
@Getter
@Setter
public class SavedView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "name", nullable = false, length = 60)
    private String name;

    /** Canonical query string produced by TicketFilter.toQuery(). */
    @Column(name = "filters", length = 1000)
    private String filters;

    @Column(name = "visibleColumns")
    private String visibleColumns;

    @Column(name = "sortOrder", length = 30)
    private String sortOrder;

    @Column(name = "displayOrder", nullable = false)
    private int displayOrder;

    @Column(name = "isDefault", nullable = false)
    private boolean defaultView;
}
