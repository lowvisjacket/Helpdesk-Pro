-- Schema changes for the UI redesign (MySQL).
-- Only needed where JPA_DDL_AUTO is "validate" (the default). With JPA_DDL_AUTO=update
-- (the compose.yaml demo stack) Hibernate applies the same changes on startup.

ALTER TABLE users
    ADD COLUMN theme VARCHAR(255) NULL,
    ADD COLUMN soundMuted BIT NULL;

ALTER TABLE tickets
    ADD COLUMN createdAt BIGINT NULL,
    ADD COLUMN updatedAt BIGINT NULL,
    ADD COLUMN staffUpdatedAt BIGINT NULL,
    ADD COLUMN lastUpdatedBy VARCHAR(255) NULL,
    ADD COLUMN staffLastUpdatedBy VARCHAR(255) NULL;

ALTER TABLE ticketNotes
    ADD COLUMN internalNote BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE saved_views (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    name VARCHAR(60) NOT NULL,
    filters VARCHAR(1000) NULL,
    visibleColumns VARCHAR(255) NULL,
    sortOrder VARCHAR(30) NULL,
    displayOrder INT NOT NULL,
    isDefault BIT NOT NULL,
    INDEX idx_saved_views_username (username)
);

-- Ticket descriptions may now be up to 5,000 characters.
ALTER TABLE tickets MODIFY description TEXT NULL;
