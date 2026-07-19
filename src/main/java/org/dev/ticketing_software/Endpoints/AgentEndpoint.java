package org.dev.ticketing_software.Endpoints;

import org.dev.ticketing_software.Data.Agents.Agent;
import org.dev.ticketing_software.Data.Agents.AgentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RequestMapping("/agent")
@RestController
public class AgentEndpoint {

    @Autowired
    AgentRepository agentRepository;

    @GetMapping("/{id}") //returns agent's info
    public Optional<Agent> getAgentByUUID(@PathVariable long id) {
        return agentRepository.findById(id);
    }

    @GetMapping("/allagents") //only for agents to see
    public Iterable<Agent> getAllAgents() {
        return agentRepository.findAll();
    }
}
