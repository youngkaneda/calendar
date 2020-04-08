package capsules.the.endpoint;

import capsules.the.domain.DailyTask;
import capsules.the.repository.EventRepository;
import capsules.the.domain.Event;
import capsules.the.domain.ReminderTask;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@ServerEndpoint("/event")
public class EventEndpoint /** extends Endpoint **/ {

    @Inject
    private EventRepository repository;
    private ScheduledExecutorService executor;

    public EventEndpoint() {
        this.executor = Executors.newScheduledThreadPool(5);
    }

    @OnOpen
    public void onOpen(Session session) throws IOException, EncodeException {
        //send to the client all events registered.
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        repository.findAll().stream().map(Event::toJson).forEach(arrayBuilder::add);
        JsonObject object = Json.createObjectBuilder()
            .add("op", 0)
            .add("events", arrayBuilder.build())
            .build();
        session.getBasicRemote().sendObject(object);
        String query = URLDecoder.decode(session.getQueryString(), StandardCharsets.UTF_8.name());
        Runnable command = () -> {
            //register a reminder for all the events today.
            repository.findAll()
                .stream()
                .filter(Event::isToday)
                .forEach(event -> event.schedule(executor, new ReminderTask(session, event, repository)));
        };
        command.run();
        //schedule a task to run every day at 00:00
        ZoneId clientTimeZone = ZoneId.of(query.split("=")[1]);
        new DailyTask(command, clientTimeZone).start();
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        JsonObject object = Json.createReader((new StringReader(message))).readObject();
        Event event = Event.fromJson(object);
        repository.save(event);
        //add the event to time task if it happens today yet.
        if (event.isToday()) {
            event.schedule(executor, new ReminderTask(session, event, repository));
        }
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        session.close();
        executor.shutdownNow();
    }
}
