package capsules.the.domain;

import javax.json.Json;
import javax.json.JsonObject;
import javax.persistence.*;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Entity
public class Event implements Serializable {

    @Id
    @SequenceGenerator(name = "event_gen", sequenceName = "event_seq")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "event_gen")
    private int id;
    private String title;
    private ZonedDateTime beginAt;
    private ZonedDateTime endAt;
    private ZoneId timeZone;

    public Event() { }

    public Event(String title, ZonedDateTime beginAt, ZonedDateTime endAt, ZoneId timeZone) {
        this.title = title;
        this.beginAt = beginAt;
        this.endAt = endAt;
        this.timeZone = timeZone;
    }

    public int id() {
        return id;
    }

    public String title() {
        return title;
    }

    public ZonedDateTime beginAt() {
        return beginAt.withZoneSameInstant(this.timeZone);
    }

    public ZonedDateTime endAt() {
        return endAt.withZoneSameInstant(this.timeZone);
    }

    public ZoneId timeZone() {
        return timeZone;
    }

    public boolean isToday() {
        ZonedDateTime now = ZonedDateTime.now(this.timeZone);
        return now.toLocalDate().isEqual((this.beginAt.toLocalDate()));
    }

    public void schedule(ScheduledExecutorService executor, Runnable task) {
        ZonedDateTime now = ZonedDateTime.now().withZoneSameInstant(this.timeZone);
        Duration delay = Duration.between(now.toInstant(), this.beginAt);
        executor.schedule(task, delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    public static Event fromJson(JsonObject object) {
        String PATTERN = "dd/MM/yyyy HH:mm:ss";
        //
        ZonedDateTime beginAt = ZonedDateTime.of(LocalDateTime.parse(
            object.getString("beginAt"),
            DateTimeFormatter.ofPattern(PATTERN)),
            ZoneId.of(object.getString("timeZone"))
        );
        ZonedDateTime endAt = ZonedDateTime.of(LocalDateTime.parse(
            object.getString("endAt"),
            DateTimeFormatter.ofPattern(PATTERN)),
            ZoneId.of(object.getString("timeZone"))
        );
        return new Event(
            object.getString("title"),
            beginAt.withZoneSameInstant(ZoneId.of("UTC")),
            endAt.withZoneSameInstant(ZoneId.of("UTC")),
            ZoneId.of(object.getString("timeZone"))
        );
    }

    public JsonObject toJson() {
        return Json.createObjectBuilder()
            .add("title", this.title)
            .add("beginAt", beginAt.withZoneSameInstant(timeZone)
                .toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
            )
            .add("endAt", endAt.withZoneSameInstant(timeZone)
                .toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
            )
            .add("timeZone", this.timeZone.toString())
            .build();
    }
}
