package controllers;

import exceptions.IntersectTaskException;
import org.junit.jupiter.api.Test;
import model.*;

import static util.TaskStatus.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.TaskStatus;
import util.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected T taskManager;
    protected Task task;
    protected Epic epic;
    protected Subtask subtask;
    protected int epicId;
    LocalDateTime start;
    Duration duration;

    protected abstract T createTaskManager();

    @BeforeEach
    void setUp() {
        taskManager = createTaskManager();
        start = LocalDateTime.of(2030, 1, 1, 10, 0);
        duration = Duration.ofMinutes(10);


        task = new Task("Test Task", "Test description", NEW);
        task.setStartTime(start);
        task.setDuration(duration);
        taskManager.addTask(task);

        epic = new Epic("Test Epic", "Test Epic description");
        epicId = taskManager.addEpic(epic);

        subtask = new Subtask("Test Subtask", "Test Subtask description", NEW, epicId);
        subtask.setStartTime(start.plusDays(1));
        subtask.setDuration(duration);
        taskManager.addSubtask(subtask);
    }


    @Test
    public void testEpicStatusUpdate() {
        // a. Все подзадачи NEW
        Subtask subtask2 = new Subtask("Subtask2", "Subtask2 description", NEW, epicId);

        subtask2.setStartTime(start.plusDays(2));
        subtask2.setDuration(duration);
        taskManager.addSubtask(subtask2);
        Epic updatedEpic = taskManager.getEpicById(epicId);
        assertEquals(TaskStatus.NEW, updatedEpic.getStatus(), "Epic должен иметь статус NEW, если все подзадачи NEW");

        // b. Все подзадачи DONE
        subtask.setStatus(DONE);
        subtask2.setStatus(DONE);
        taskManager.updateSubtask(subtask);
        taskManager.updateSubtask(subtask2);
        updatedEpic = taskManager.getEpicById(epicId);
        assertEquals(TaskStatus.DONE, updatedEpic.getStatus(), "Epic должен иметь статус DONE, если все подзадачи DONE");

        // c. NEW и DONE
        subtask2.setStatus(NEW);
        taskManager.updateSubtask(subtask2);
        updatedEpic = taskManager.getEpicById(epicId);
        assertEquals(TaskStatus.IN_PROGRESS, updatedEpic.getStatus(), "Epic должен иметь статус IN_PROGRESS, " +
                "если Подзадачи со статусами NEW и DONE ");

        // d. Подзадачи IN_PROGRESS
        subtask.setStatus(IN_PROGRESS);
        subtask2.setStatus(IN_PROGRESS);
        taskManager.updateSubtask(subtask);
        taskManager.updateSubtask(subtask2);
        updatedEpic = taskManager.getEpicById(epicId);
        assertEquals(TaskStatus.IN_PROGRESS, updatedEpic.getStatus(), "Epic должен иметь статус IN_PROGRESS, " +
                "если Подзадачи со статусами IN_PROGRESS");
    }


    // Для подзадач необходимо дополнительно убедиться в наличии связанного эпика.
    @Test
    public void shouldLinkSubtaskToEpic() {
        int stId = subtask.getTaskID();
        Subtask retrieved = taskManager.getSubtaskById(stId);
        assertNotNull(retrieved, "Подзадача должна быть создана");
        assertEquals(epicId, retrieved.getEpicID(), "Подзадача должна ссылаться на правильный Epic");

        Epic updatedEpic = taskManager.getEpicById(epicId);
        assertTrue(updatedEpic.getSubtaskIdList().contains(stId), "Epic должен содержать ID новой подзадачи");
    }

    @Test
    public void shouldUpdateEpicWhenSubtaskIsRemoved() {
        // Добавим вторую подзадачу
        Subtask subtask2 = new Subtask("Subtask2", "Subtask2 description", NEW, epicId);

        subtask2.setStartTime(start.plusDays(4));
        subtask2.setDuration(duration);
        int subtaskId2 = taskManager.addSubtask(subtask2);

        // Удалим первую подзадачу
        taskManager.removeTaskById(subtask.getTaskID());
        Epic updatedEpic = taskManager.getEpicById(epicId);
        assertFalse(updatedEpic.getSubtaskIdList().contains(subtask.getTaskID()), "Epic не должен содержать ID удалённой подзадачи");
        assertTrue(updatedEpic.getSubtaskIdList().contains(subtaskId2), "Epic должен по-прежнему содержать другую подзадачу");
    }

    @Test
    public void shouldRemoveSubtasksWhenEpicIsRemoved() {
        // Добавим ещё одну подзадачу
        Subtask subtask2 = new Subtask("Subtask2", "Subtask2 description", NEW, epicId);
        subtask2.setStartTime(start.plusDays(5));
        subtask2.setDuration(duration);
        int subtaskId2 = taskManager.addSubtask(subtask2);

        taskManager.removeTaskById(epicId); // удалим эпик

        assertNull(taskManager.getEpicById(epicId), "Epic должен быть удалён");
        assertNull(taskManager.getSubtaskById(subtask.getTaskID()), "Первая подзадача должна быть удалена вместе с Epic");
        assertNull(taskManager.getSubtaskById(subtaskId2), "Вторая подзадача тоже должна быть удалена вместе с Epic");
    }

    ///
    @Test
    public void testTimeIsIntersect() {

        Task task2 = new Task("Task2", "Task2 description", NEW);
        LocalDateTime startTask2 = task.getStartTime().plusMinutes(5);
        Duration durationTask2 = Duration.ofMinutes(15);
        task2.setStartTime(startTask2);
        task2.setDuration(durationTask2);

        assertThrows(IntersectTaskException.class, () -> taskManager.addTask(task2)); // Исключение, если есть пересечение
    }
    ///


    @Test
    void removeAllTasks() {
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
    void manualAndGeneratedIdsShouldNotConflict() {
        Task manualTask = new Task("manual Task", "manual Task description", NEW);
        manualTask.setTaskID(1);

        manualTask.setStartTime(start.plusDays(8));
        manualTask.setDuration(duration);
        int manualTaskId = taskManager.addTask(manualTask);

        Task autoTask = new Task("autoTask Task", "autoTask Task description", NEW);

        autoTask.setStartTime(start.plusDays(5));
        autoTask.setDuration(duration);

        int autoTaskId = taskManager.addTask(autoTask);
        assertNotEquals(manualTaskId, autoTaskId, "ID задач не должны совпадать");
        assertNotEquals(manualTask, autoTask, "задач не должны совпадать");
    }

    @Test
    void removeTaskById() {
        Task newTask = new Task("Test Task", "Test description", NEW);
        newTask.setStartTime(start.plusDays(10));
        newTask.setDuration(duration);

        Subtask newSubtask = new Subtask("Test Subtask_2", "Subtask_2 description", NEW, epicId);
        newSubtask.setStartTime(start.plusDays(11));
        newSubtask.setDuration(duration);

        Epic newEpic = new Epic("Test Epic", "Test Epic description");

        int taskIdToDel = taskManager.addTask(newTask);
        int subTaskIdToDel = taskManager.addSubtask(newSubtask);
        int epicIdToDel = taskManager.addEpic(newEpic);


        taskManager.removeTaskById(taskIdToDel);
        Task removedTask = taskManager.getTaskById(taskIdToDel);

        taskManager.removeTaskById(subTaskIdToDel);
        Subtask removedSubTask = taskManager.getSubtaskById(subTaskIdToDel);

        taskManager.removeTaskById(epicIdToDel);
        Epic removedEpic = taskManager.getEpicById(epicIdToDel);

        assertNotEquals(newTask, removedTask, "задачи не должны совпадать");
        assertNotEquals(newSubtask, removedSubTask, "задачи не должны совпадать");
        assertNotEquals(newEpic, removedEpic, "задачи не должны совпадать");
    }

    @Test
    void epicSubtaskIDsListDoNotContainSubtaskIDAfterRemove() {
        int subTaskId = subtask.getTaskID();
        int epicID = subtask.getEpicID();
        taskManager.removeTaskById(subTaskId);
        Epic epic = taskManager.getEpicById(epicID);
        assertFalse(epic.getSubtaskIdList().contains(subTaskId), "Ошибка! ID не должно быть в списке");

    }


    @Test
    void updateAndGetHistory() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        historyManager.add(task);
        final List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История  пустая.");
        assertEquals(1, history.size(), "История пустая.");
    }

    @Test
    void historyShouldKeepPreviousTaskVersionAfterUpdate() {
        int taskId = task.getTaskID();
        taskManager.getTaskById(taskId);

        Task updatedTask = new Task("Обновленная задача", "Новое описание", IN_PROGRESS);
        updatedTask.setStartTime(start.plusDays(20));
        updatedTask.setDuration(duration);

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

        subtaskUpdated.setStartTime(start.plusDays(13));
        subtaskUpdated.setDuration(duration);


        subtaskUpdated.setTaskID(subtask.getTaskID());
        taskManager.updateSubtask(subtaskUpdated);

        List<Task> history = taskManager.getHistory();
        assertNotNull(history, "history = null.");

        assertEquals("Test Subtask", history.get(0).getName(), "name != не хранит исходную версию");
        assertEquals("Test Subtask description", history.get(0).getDescription(),
                "Description != не хранит исходную версию");
    }


    @Test
    void updateTaskDoNotInfluenceToHistory() {
        Task taskToUpd = taskManager.getTaskById(task.getTaskID());
        Epic epicToUpd = taskManager.getEpicById(epic.getTaskID());
        Subtask subtaskToUpd = taskManager.getSubtaskById(subtask.getTaskID());


        taskToUpd.setName("TaskAfterUpd");
        taskToUpd.setDescription("Task description After Upd");
        taskToUpd.setStatus(DONE);

        subtaskToUpd.setName("SubtaskAfterUpd");
        subtaskToUpd.setDescription("Subtask description After Upd");
        subtaskToUpd.setStatus(IN_PROGRESS);

        epicToUpd.setName("EpicAfterUpd");
        epicToUpd.setDescription("Epic description After Upd");

        List<Task> history = taskManager.getHistory();

        assertNotEquals(taskToUpd, history.get(0), "Задачи не должны совпадать");
        assertNotEquals(epicToUpd, history.get(1), "Эпики не должны совпадать");
        assertNotEquals(subtaskToUpd, history.get(2), "Подзадачи не должны совпадать");

    }

    @Test
    void linkedListHistoryAddTests() {
        int taskId = task.getTaskID();
        int subTaskId = subtask.getTaskID();
        int epicID = epicId;
        List<Task> historyTest;

        taskManager.getTaskById(taskId);
        historyTest = taskManager.getHistory();


        assertFalse(historyTest.isEmpty(), "История пуста");
        assertNotNull(historyTest, "history = null.");

        assertEquals(task, historyTest.get(0), "Задачи не совпадают");
        assertEquals(1, historyTest.size(), "размер списка не равен 1");


        taskManager.getSubtaskById(subTaskId);
        historyTest = taskManager.getHistory();

        assertEquals(subtask, historyTest.get(1), "Подзадачи не совпадают");
        assertEquals(2, historyTest.size(), "размер списка не равен 2");


        taskManager.getEpicById(epicID);
        historyTest = taskManager.getHistory();

        assertEquals(epic, historyTest.get(2), "Эпики не совпадают");
        assertEquals(3, historyTest.size(), "размер списка не равен 3");


        taskManager.getTaskById(taskId);
        historyTest = taskManager.getHistory();

        assertNotEquals(task, historyTest.get(0), "Ошибка! Задача не должна быть в начале списка");
        assertEquals(task, historyTest.get(2), "Ошибка! Задача должна быть в конце списка");

    }

    @Test
    void linkedListHistoryRemoveTests() {
        int taskId = task.getTaskID();
        List<Task> historyTest;

        taskManager.getTaskById(taskId);
        historyTest = taskManager.getHistory();

        assertFalse(historyTest.isEmpty(), "Ошибка! История пуста");
        assertNotNull(historyTest, "history = null.");


        taskManager.removeTaskById(taskId);
        historyTest = taskManager.getHistory();
        assertTrue(historyTest.isEmpty(), "Ошибка! История НЕ пуста");

    }


}

