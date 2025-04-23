package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static util.TaskStatus.NEW;

class SubtaskTest {

    @Test
    void getEpicID() {
    }

    @Test
    void tasksWithSameIdShouldBeEqual() {
        Subtask subtask1 = new Subtask("Test Subtask", "Test Subtask description", NEW, 1);
        subtask1.setTaskID(1);

        Subtask subtask2 = new Subtask("Test Subtask", "Test Subtask description", NEW, 1);
        subtask2.setTaskID(1);

        assertEquals(subtask1, subtask2, "Subtask с одинаковым id должны быть равны");
    }
}