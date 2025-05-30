package controllers;

import model.Task;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static util.TaskStatus.NEW;

class ManagersTest {

    @Test
    void getDefaultShouldReturnInitializedObject() {
        TaskManager manager = Managers.getDefault();
        assertNotNull(manager, "Менеджер задач не должен быть null");


        Task task = new Task("Test Task", "Test Task description", NEW);
        LocalDateTime start  = LocalDateTime.of(2030, 5, 4, 10, 0);
        Duration duration = Duration.ofMinutes(10);
        task.setStartTime(start);
        task.setDuration(duration);
        int taskId = manager.addTask(task);
        Task savedTask = manager.getTaskById(taskId);

        assertEquals(task, savedTask, "Задача должна быть добавлена и доступна");

    }

    @Test
    void getDefaultHistoryShouldReturnInitializedObject() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertNotNull(historyManager, "Менеджер истории не должен быть null");

        Task task = new Task("Test Task", "Test Task description", NEW);
        historyManager.add(task);
        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(), "Ошибка. история должна содержать одну задачу");
        assertEquals(task, history.get(0), "Ошибка. задачи не совпадают");

    }
}