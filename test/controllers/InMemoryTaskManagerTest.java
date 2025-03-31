package controllers;

import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.TaskStatus;
import util.TaskType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static util.TaskStatus.IN_PROGRESS;
import static util.TaskStatus.NEW;

class InMemoryTaskManagerTest {

    private TaskManager taskManager;
    private Task task;
    private Epic epic;
    private Subtask subtask;
    private int epicId;

    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefault();

        task = new Task("Test Task", "Test description", NEW);
        taskManager.addTask(task);

        epic = new Epic("Test Epic", "Test Epic description");
        epicId = taskManager.addEpic(epic);

        subtask = new Subtask("Test Subtask", "Test Subtask description", NEW, epicId);
        taskManager.addSubtask(subtask);
    }

    @Test
    void updateAndGetHistory() {

        HistoryManager historyManager = Managers.getDefaultHistory();
        historyManager.updateHistory(task);
        final List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История  пустая.");
        assertEquals(1, history.size(), "История пустая.");
    }


    @Test
    void removeAllTasks() {
        taskManager.addSubtask(subtask);

        taskManager.removeAllTasks(TaskType.TASK);
        assertTrue(taskManager.getTasks().isEmpty(), "Ошибка! Список Task должен быть пуст");

        taskManager.removeAllTasks(TaskType.EPIC);
        assertTrue(taskManager.getEpics().isEmpty(), "Ошибка! Список Epics должен быть пуст");
        assertTrue(taskManager.getSubtasks().isEmpty(), "Ошибка! Список Subtasks должен быть пуст");
    }

    @Test
    void getTaskById() {
        Task savedTask = taskManager.getTaskById(task.getTaskID());
        assertEquals(task, savedTask, "Задача не найдена");
    }

    @Test
    void getEpicById() {
        Epic savedEpic = taskManager.getEpicById(epicId);
        assertEquals(epic, savedEpic, "Эпик не найден");
    }

    @Test
    void getSubtaskById() {
        Subtask savedSubtask = taskManager.getSubtaskById(subtask.getTaskID());
        assertEquals(subtask, savedSubtask, "Подзадача не найдена");
    }

    @Test
    void addNewTask() {
        final Task savedTask = taskManager.getTaskById(task.getTaskID());

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        final List<Task> tasks = taskManager.getTasks();
        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.get(0), "Задачи не совпадают.");
    }

    @Test
    void addNewEpic() {
        final Epic savedEpic = taskManager.getEpicById(epic.getTaskID());

        assertNotNull(savedEpic, "Эпик не найден.");
        assertEquals(epic, savedEpic, "Эпики не совпадают.");

        final List<Epic> epics = taskManager.getEpics();
        assertNotNull(epics, "Эпики не возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество Эпиков.");
        assertEquals(epic, epics.get(0), "Эпики не совпадают.");
    }

    @Test
    void addNewSubtask() {
        final Subtask savedSubtask = taskManager.getSubtaskById(subtask.getTaskID());

        assertNotNull(savedSubtask, "Подзадача не найдена.");
        assertEquals(subtask, savedSubtask, "Подзадачи не совпадают.");

        final List<Subtask> subtasks = taskManager.getSubtasks();
        assertNotNull(subtasks, "Подзадачи не возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество Подзадач.");
        assertEquals(subtask, subtasks.get(0), "Подзадачи не совпадают.");
    }

    @Test
    void EpicShouldNotAddToSubtask() {
        Epic epic = new Epic("Test Epic", "Test Epic description");
        epic.setTaskID(1);

        epic.setSubtaskIdList(epic.getTaskID());

        assertTrue(epic.getSubtaskIdList().isEmpty(), "Эпик не должен быть добавлен в самого себя");
    }

    @Test
    void SubtaskCantBecomeOwnEpic() {
        int subtaskId = taskManager.addSubtask(subtask);
        Subtask addedSubtask = taskManager.getSubtaskById(subtaskId);

        assertNotEquals(addedSubtask.getEpicID(), addedSubtask.getTaskID(), "Ошибка! EpicID = TaskID");
    }

    @Test
    void manualAndGeneratedIdsShouldNotConflict() {
        Task manualTask = new Task("manual Task", "manual Task description", NEW);
        manualTask.setTaskID(1);
        int manualTaskId = taskManager.addTask(manualTask);

        Task autoTask = new Task("autoTask Task", "autoTask Task description", NEW);
        int autoTaskId = taskManager.addTask(autoTask);
        assertNotEquals(manualTaskId, autoTaskId, "ID задач не должны совпадать");
        assertNotEquals(manualTask, autoTask, "задач не должны совпадать");
    }


    @Test
    void historyShouldKeepPreviousTaskVersionAfterUpdate() {
        int taskId = taskManager.addTask(task);
        taskManager.getTaskById(taskId);

        Task updatedTask = new Task("Обновленная задача", "Новое описание", IN_PROGRESS);
        updatedTask.setTaskID(taskId);
        taskManager.updateTask(updatedTask);  // обновляем Таск

        List<Task> history = taskManager.getHistory();

        assertEquals("Test Task", history.get(0).getName(), "name != не хранит исходную версию");
        assertEquals("Test description", history.get(0).getDescription(),
                "Description != не хранит исходную версию");
        assertEquals(NEW, history.get(0).getStatus(), "Status != не хранит исходную версию");
        assertEquals(taskId, history.get(0).getTaskID(), "taskId != не хранит исходную версию");
    }

    @Test
    void historyShouldKeepPreviousEpicVersionAfterUpdate() {

        TaskStatus epicStatus = epic.getStatus();
        taskManager.getEpicById(epicId); // получаем эпик по id и записываем его в историю


        Epic epicUpdated = new Epic("Новый Эпик", "Новое описание Эпика");
        epicUpdated.setTaskID(epicId);
        epicUpdated.setStatus(IN_PROGRESS);
        taskManager.updateEpic(epicUpdated);  // обновляем эпик

        List<Task> history = taskManager.getHistory();

        assertFalse(history.isEmpty(), "История пуста");

        assertNotNull(history, "history = null.");
        assertEquals("Test Epic", history.get(0).getName(), "name != не хранит исходную версию");
        assertEquals("Test Epic description", history.get(0).getDescription(),
                "Description != не хранит исходную версию");
        assertEquals(epicStatus, history.get(0).getStatus(), "Status != не хранит исходную версию");

    }

    @Test
    void historyShouldKeepPreviousSubtaskVersionAfterUpdate() {

        taskManager.getSubtaskById(subtask.getTaskID());
        Subtask subtaskUpdated = new Subtask("Обновленная Подзадача",
                "Новое описание Подзадачи", IN_PROGRESS, epicId);
        subtaskUpdated.setTaskID(subtask.getTaskID());
        taskManager.updateSubtask(subtaskUpdated);

        List<Task> history = taskManager.getHistory();
        assertNotNull(history, "history = null.");

        assertEquals("Test Subtask", history.get(0).getName(), "name != не хранит исходную версию");
        assertEquals("Test Subtask description", history.get(0).getDescription(),
                "Description != не хранит исходную версию");
    }

    @Test
    void removeTaskById() {
    }


}