package server.util;

import server.Processing;
import server.XLogger;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;

public class TaskScheduler {
    private static final LinkedList<ScheduledTask> scheduledTasks = new LinkedList<>();

    public static final ZoneId usEast = ZoneId.of("America/New_York");
    public static final ZoneId usWest = ZoneId.of("America/Los_Angeles");
    public static final ZoneId belgium = ZoneId.of("Europe/Brussels");

    public static void scheduleTask(ZonedDateTime baseDateTime, Duration runEvery, Runnable task) {
        ScheduledTask newRecord = new ScheduledTask(runEvery, task, baseDateTime);
        while (newRecord.scheduledFor().isBefore(Processing.utcDateTime)){
            newRecord = newRecord.reschedule();
        }
        scheduledTasks.add(newRecord);
    }

    public static void scheduleTask(String timeString, ZoneId timezone, Duration runEvery, Runnable task) {
        LocalTime time = parseTimeString(timeString);
        ZonedDateTime baseDateTime = ZonedDateTime.now(timezone)
            .with(time)
            .truncatedTo(ChronoUnit.DAYS);
        scheduleTask(baseDateTime, runEvery, task);
    }

    private static int dailyQuietCount = 0;
    public static void scheduleDailyQuietHours(Runnable task){
        ZonedDateTime midnight = Processing.utcDateTime
            .withZoneSameLocal(usWest)
            .truncatedTo(ChronoUnit.DAYS)
            .plus(Duration.ofMinutes(dailyQuietCount++));

        scheduleTask(midnight, Duration.ofDays(1), task);
    }


    private static int weeklyQuietCount = 0;
    // Midnight on Tuesday (Tuesday 12:00 AM)
    public static void scheduleWeeklyQuietHours(Runnable task){
        ZonedDateTime nextMondayMidnight = Processing.utcDateTime
            .withZoneSameInstant(usWest)
            .with(TemporalAdjusters.next(DayOfWeek.TUESDAY))
            .truncatedTo(ChronoUnit.DAYS)
            .with(LocalTime.MIDNIGHT)
            .plus(Duration.ofMinutes(weeklyQuietCount++));

        scheduleTask(nextMondayMidnight, Duration.ofDays(7), task);
    }

    public static void process() {
        LinkedList<ScheduledTask> newTasks = new LinkedList<>();

        Iterator<ScheduledTask> iterator = scheduledTasks.iterator();
        while (iterator.hasNext()) {
            ScheduledTask task = iterator.next();
            if (task.scheduledFor().isBefore(Processing.utcDateTime)) {
                iterator.remove();
                newTasks.add(task.reschedule());
                try {
                    task.action().run();
                } catch (Exception e) {
                    XLogger.getInstance().log(Level.SEVERE, "Error processing scheduled task", e);
                }
            }
        }

        scheduledTasks.addAll(newTasks);
    }

    private static LocalTime parseTimeString(String timeString) throws DateTimeParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
        return LocalTime.parse(timeString, formatter);
    }

    public record ScheduledTask(Duration runEvery, Runnable action, ZonedDateTime scheduledFor) {
        public ScheduledTask reschedule(){
            return new ScheduledTask(runEvery, action, scheduledFor.plus(runEvery));
        }
    }

    /*
     * Example usage
     */
    public static void main(String[] args) {
        TaskScheduler.scheduleDailyQuietHours(() -> {
            System.out.println("Task executed daily during quiet hours.");
        });

        TaskScheduler.scheduleWeeklyQuietHours(() -> {
            System.out.println("Task executed weekly during quiet hours.");
        });

        TaskScheduler.scheduleTask("12:00 PM", TaskScheduler.belgium, Duration.ofDays(1), () -> {
            System.out.println("Task executed daily at noon in Belgium.");
        });

        TaskScheduler.scheduleTask("12:00 PM", TaskScheduler.belgium, Duration.ofHours(6), () -> {
            System.out.println("Task executed every 6 hours, starting at noon in Belgium.");
        });

        TaskScheduler.scheduleTask("8:00 AM", TaskScheduler.usEast, Duration.ofSeconds(10), () -> {
            System.out.println("Task executed every 10 seconds, starting at 8 AM in NYC.");
        });

        TaskScheduler.scheduleTask(ZonedDateTime.now().plusMinutes(1), Duration.ofSeconds(10), () -> {
            System.out.println("Task executed every 10 seconds, starting at now + 1 minute in local time.");
        });

        while (true){
            Processing.utcDateTime = ZonedDateTime.now();
            process();
        }
    }
}
