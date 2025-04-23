package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static util.TaskStatus.NEW;

class EpicTest {


    @Test
    void tasksWithSameIdShouldBeEqual() {

        Epic epic1 = new Epic("Test Epic", "Test Epic description");
        epic1.setTaskID(1);

        Epic epic2 = new Epic("Test Epic", "Test Epic description");
        epic2.setTaskID(1);

        assertEquals(epic1, epic2, "Эпики с одинаковым id должны быть равны");
    }


}