package capsules.the.domain;

import capsules.the.repository.EventRepository;

import javax.json.Json;
import javax.json.JsonObject;
import javax.websocket.EncodeException;
import javax.websocket.Session;
import java.io.IOException;

public class ReminderTask implements Runnable {

    private Session session;
    private Event event;
    private EventRepository repository;

    public ReminderTask() { }

    public ReminderTask(Session session, Event event, EventRepository repository) {
        this.session = session;
        this.event = event;
        this.repository = repository;
    }

    @Override
    public void run() {
        try {
            JsonObject object = Json.createObjectBuilder()
                .add("op", 1)
                .add("reminder", String.format("This is a reminder about %s.", event.title()))
                .build();
            session.getBasicRemote().sendObject(object);
            repository.remove(event.id());
        } catch (IOException | EncodeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
