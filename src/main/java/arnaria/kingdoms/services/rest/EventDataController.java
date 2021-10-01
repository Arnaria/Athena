package arnaria.kingdoms.services.rest;

import arnaria.kingdoms.services.events.Event;
import arnaria.kingdoms.services.events.EventManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EventDataController {

    private static final String url = "/api/events";
    private static final ObjectMapper mapper = new ObjectMapper();

     @GetMapping(url)
    ObjectNode events() {
        ObjectNode mapping = mapper.createObjectNode();

        for (Event event : EventManager.getActiveEvents()) {
            mapping.put("eventName", event.getName());
            mapping.putPOJO("members", event.getMembers());
        }
        return mapping;
    }
}
