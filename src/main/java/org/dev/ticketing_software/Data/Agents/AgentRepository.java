package org.dev.ticketing_software.Data.Agents;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface AgentRepository extends CrudRepository<Agent, Long> {}
