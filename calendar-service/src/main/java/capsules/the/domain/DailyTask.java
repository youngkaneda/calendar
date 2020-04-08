package capsules.the.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class DailyTask extends Thread {

    private Runnable command;
    private ZoneId timeZone;

    public DailyTask(Runnable command, ZoneId timeZone) {
        this.command = command;
        this.timeZone = timeZone;
    }

    @Override
    public void run() {
        while (true) {
            ZonedDateTime now = ZonedDateTime.now(this.timeZone);
            ZonedDateTime tomorrow = ZonedDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIDNIGHT, timeZone);
            try {
//                System.out.println("duration " + TimeUnit.DAYS.toHours(Duration.between(now, tomorrow).toMillis()));
                Thread.sleep(Duration.between(now, tomorrow).toMillis());
                this.command.run();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
