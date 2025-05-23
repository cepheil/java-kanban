package controllers;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;
    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 10, 0);
        Duration duration = Duration.ofMinutes(10);

        task1 = new Task("Task1", "Description1", TaskStatus.NEW);
        task1.setTaskID(1);
        task1.setStartTime(start);
        task1.setDuration(duration);

        task2 = new Task("Task2", "Description2", TaskStatus.NEW);
        task2.setTaskID(2);
        task2.setStartTime(start.plusDays(1));
        task2.setDuration(duration);

        task3 = new Task("Task3", "Description3", TaskStatus.NEW);
        task3.setTaskID(3);
        task2.setStartTime(start.plusDays(2));
        task2.setDuration(duration);
    }

    @Test
    void testEmptyHistory() {
        List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не должна быть null");
        assertTrue(history.isEmpty(), "История должна быть пустой");
    }

    @Test
    void testAddSingleTask() {
        historyManager.add(task1);
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать одну задачу");
        assertEquals(task1.getTaskID(), history.get(0).getTaskID(), "ID задачи должен совпадать");
    }

    @Test
    void testAddMultipleTasks() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        List<Task> history = historyManager.getHistory();

        assertEquals(3, history.size(), "История должна содержать три задачи");
        assertEquals(task1.getTaskID(), history.get(0).getTaskID());
        assertEquals(task2.getTaskID(), history.get(1).getTaskID());
        assertEquals(task3.getTaskID(), history.get(2).getTaskID());
    }

    @Test
    void testAddDuplicateTaskMovesToEnd() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1); // дублируем

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), "История должна содержать две уникальные задачи");
        assertEquals(task2.getTaskID(), history.get(0).getTaskID(), "task2 должна быть первой");
        assertEquals(task1.getTaskID(), history.get(1).getTaskID(), "task1 должна быть перемещена в конец");
    }

    @Test
    void testRemoveTaskFromBeginning() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task1.getTaskID());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task2.getTaskID(), history.get(0).getTaskID());
    }

    @Test
    void testRemoveTaskFromMiddle() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task2.getTaskID());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1.getTaskID(), history.get(0).getTaskID());
        assertEquals(task3.getTaskID(), history.get(1).getTaskID());
    }

    @Test
    void testRemoveTaskFromEnd() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task3.getTaskID());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1.getTaskID(), history.get(0).getTaskID());
        assertEquals(task2.getTaskID(), history.get(1).getTaskID());
    }

    @Test
    void testRemoveNonexistentTask() {
        historyManager.add(task1);
        historyManager.remove(999); // несуществующий ID

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История не должна измениться");
    }

}