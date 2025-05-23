package model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static util.TaskStatus.NEW;

class TaskTest {

    @Test
    void tasksWithSameIdShouldBeEqual() {

        Task task1 = new Task("Test Task", "Test Task description", NEW);
        task1.setTaskID(1);

        Task task2 = new Task("Test Task", "Test Task description", NEW);
        task2.setTaskID(1);

        assertEquals(task1, task2, "Задачи с одинаковым id должны быть равны");
    }

    @Test
    void shouldSetAndGetStartTimeAndDuration() {
        Task task = new Task("Test", "Desc",  NEW);
        LocalDateTime start = LocalDateTime.of(2222, 1, 11, 22, 33);
        Duration duration = Duration.ofMinutes(90);

        task.setStartTime(start);
        task.setDuration(duration);

        assertEquals(start, task.getStartTime(),"startTime не совпадает" );
        assertEquals(duration, task.getDuration(), "duration не совпадает");
        assertEquals(start.plus(duration), task.getEndTime(), "EndTime не совпадает");
    }

    @Test
    void shouldCompareTasksWithTimeFieldsCorrectly() {
        Task task1 = new Task("A", "B",  NEW);
        Task task2 = new Task("A", "B",  NEW);

        LocalDateTime time = LocalDateTime.of(2222, 1, 11, 22, 33);
        Duration dur = Duration.ofMinutes(60);

        task1.setStartTime(time);
        task1.setDuration(dur);
        task2.setStartTime(time);
        task2.setDuration(dur);

        assertEquals(task1, task2,"Задачи не равны" );
        assertEquals(task1.hashCode(), task2.hashCode(), "hashCode не равен");
    }

}