package model;

import org.junit.jupiter.api.Test;

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
}