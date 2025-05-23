package controllers;

import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.TaskStatus;
import util.TaskType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static util.TaskStatus.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    private File tempFile;

    @Override
    protected FileBackedTaskManager createTaskManager() {
        try {
            tempFile = File.createTempFile("tasks_empty", ".csv");
            tempFile.deleteOnExit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new FileBackedTaskManager(Managers.getDefaultHistory(), tempFile);
    }


    @Test
    void saveMultipleTasks() throws IOException {
        taskManager.save();

        List<String> lines = Files.readAllLines(tempFile.toPath());

        assertEquals(1 + 3, lines.size(), "Количество строк в файле должно быть header + 3");
        assertEquals("id,type,name,status,description,startTime,duration,endTime,epicID", lines.get(0).trim());

        // Проверяем, что в файле сохранились задачи по строкам
        String csv1 = task.toCsv().trim();  //сохраняем такс в строку.
        String csv2 = epic.toCsv().trim();  //... эпик в строку
        String csv3 = subtask.toCsv().trim();  //... сабтаск в строку

        assertEquals(csv1, lines.get(1).trim());
        assertEquals(csv2, lines.get(2).trim());
        assertEquals(csv3, lines.get(3).trim());
    }

    @Test
    void loadMultipleTasks() throws IOException {
        int idS = subtask.getTaskID();
        //проверка, что файл существует.
        assertTrue(Files.exists(tempFile.toPath()), "Файл не был создан на диске");

        //Загружаем новый менеджер из этого файла
        FileBackedTaskManager loaded = Managers.loadFromFile(tempFile);

        //Проверяем размеры коллекций
        assertEquals(1, loaded.getTasks().size(), "Неверное число Task после загрузки");
        assertEquals(1, loaded.getEpics().size(), "Неверное число Epic после загрузки");
        assertEquals(1, loaded.getSubtasks().size(), "Неверное число Subtask после загрузки");

        //Сравниваем задачи по полям
        Task lt = loaded.getTasks().get(0);
        Epic le = loaded.getEpics().get(0);
        Subtask ls = loaded.getSubtasks().get(0);

        assertEquals(task.getName(), lt.getName());
        assertEquals(task.getDescription(), lt.getDescription());
        assertEquals(task.getStatus(), lt.getStatus());
        assertEquals(task.getTaskID(), lt.getTaskID());

        assertEquals(epic.getName(), le.getName());
        assertEquals(epic.getDescription(), le.getDescription());
        assertEquals(epic.getTaskID(), le.getTaskID());
        assertEquals(1, le.getSubtaskIdList().size(), "эпик не содержит подзадачу");

        assertEquals(subtask.getName(), ls.getName());
        assertEquals(subtask.getDescription(), ls.getDescription());
        assertEquals(subtask.getStatus(), ls.getStatus());
        assertEquals(subtask.getTaskID(), ls.getTaskID());

        //связь сабтаска и эпика
        assertEquals(epicId, ls.getEpicID());
        assertTrue(loaded.getEpicById(epicId).getSubtaskIdList().contains(idS));
    }

    @Test
    void getAllTypesById() {
        int idT = task.getTaskID();
        int idE = epicId;
        int idS = subtask.getTaskID();

        Task savedTask = taskManager.getTaskById(idT);
        Epic savedEpic = taskManager.getEpicById(idE);
        Subtask savedSubtask = taskManager.getSubtaskById(idS);

        assertNotNull(savedTask, "Задача не найдена");
        assertNotNull(savedEpic, "Эпик не найден");
        assertNotNull(savedSubtask, "Подзадача не найдена");

        assertEquals(task, savedTask, "Задачи не равны");
        assertEquals(epic, savedEpic, "Эпики не равны");
        assertEquals(subtask, savedSubtask, "Подзадачи не равны");
    }


    @Test
    void historyShouldKeepPreviousTaskVersionAfterUpdate() {
        int idT = task.getTaskID();
        taskManager.getTaskById(idT); // добавляем в историю task {0}

        int idE = epicId;
        TaskStatus epicStatus = epic.getStatus();  // NEW
        taskManager.getEpicById(idE); // добавляем в историю epic {1}

        int idS = subtask.getTaskID();
        taskManager.getSubtaskById(idS);  // добавляем в историю subtask {2}

        // обновляем задачу
        Task updatedTask = new Task("updTask", "updTask_Desc", IN_PROGRESS);
        updatedTask.setStartTime(start.plusDays(8));
        updatedTask.setDuration(duration);
        updatedTask.setTaskID(idT);
        taskManager.updateTask(updatedTask);

        // обновляем эпик
        Epic epicUpdated = new Epic("updEpic", "updEpic_Desc");
        epicUpdated.setTaskID(idE);
        epicUpdated.setStatus(DONE);
        taskManager.updateEpic(epicUpdated);

        // обновляем подзадачу
        Subtask subtaskUpdated = new Subtask("updSubtask", "updSubtask_Desc", IN_PROGRESS, idE);
        subtaskUpdated.setStartTime(start.plusDays(5));
        subtaskUpdated.setDuration(duration);
        subtaskUpdated.setTaskID(idS);
        taskManager.updateSubtask(subtaskUpdated);

        List<Task> history = taskManager.getHistory(); //получаем историю обращений к задачам.

        // проверка истории
        assertFalse(history.isEmpty(), "Ошибка! История пуста");
        assertNotNull(history, "Ошибка! history = null.");
        assertEquals(3, taskManager.getHistory().size(), "Ошибка! История должна хранить 3 элемента");

        // проверка хранения Тасков.
        assertEquals("Test Task", history.get(0).getName(), "name != не хранит исходную версию");
        assertEquals("Test description", history.get(0).getDescription(),
                "Description != не хранит исходную версию");
        assertEquals(NEW, history.get(0).getStatus(), "Status != не хранит исходную версию");
        assertEquals(idT, history.get(0).getTaskID(), "taskId != не хранит исходную версию");

        // проверка хранения Эпиков.
        assertEquals("Test Epic", history.get(1).getName(), "name != не хранит исходную версию");
        assertEquals("Test Epic description", history.get(1).getDescription(),
                "Description != не хранит исходную версию");
        assertEquals(epicStatus, history.get(1).getStatus(), "Status != не хранит исходную версию");


        // проверка хранения Сабтасков.
        assertEquals("Test Subtask", history.get(2).getName(), "name != не хранит исходную версию");
        assertEquals("Test Subtask description", history.get(2).getDescription(),
                "Description != не хранит исходную версию");
        assertEquals(NEW, history.get(2).getStatus(), "Status != не хранит исходную версию");

        // проверка обновленных задач после загрузки из файла
        try {
            FileBackedTaskManager loaded = Managers.loadFromFile(tempFile);

            Task loadedTask = loaded.getTaskById(idT);
            Epic loadedEpic = loaded.getEpicById(idE);
            Subtask loadedSubtask = loaded.getSubtaskById(idS);

            assertEquals("updTask", loadedTask.getName(), "Ошибка! Task name - не совпадают");
            assertEquals("updTask_Desc", loadedTask.getDescription(),
                    "Ошибка! Task description - не совпадают");
            assertEquals(IN_PROGRESS, loadedTask.getStatus(), "Ошибка! Task status - не совпадают");

            assertEquals("updEpic", loadedEpic.getName(), "Ошибка! Epic name - не совпадают");
            assertEquals("updEpic_Desc", loadedEpic.getDescription(),
                    "Ошибка! Epic description - не совпадают");
            assertEquals(IN_PROGRESS, loadedEpic.getStatus(), "Ошибка! Epic status - не совпадают");

            assertEquals("updSubtask", loadedSubtask.getName(), "Ошибка! Subtask name - не совпадают");
            assertEquals("updSubtask_Desc", loadedSubtask.getDescription(),
                    "Ошибка! Subtask description - не совпадают");
            assertEquals(IN_PROGRESS, loadedSubtask.getStatus(), "Ошибка! Subtask status - не совпадают");
        } catch (IOException e) {
            fail("Ошибка при загрузке менеджера из файла: " + e.getMessage());
        }
    }

    @Test
    void removeTaskByIdAndRemoveAllTasksByType() throws IOException {
        int idT = task.getTaskID();
        taskManager.removeTaskById(idT);
        taskManager.removeAllTasks(TaskType.EPIC);

        //загружаем из файла
        FileBackedTaskManager loaded = Managers.loadFromFile(tempFile);

        assertNull(loaded.getTaskById(idT), "Task не должен существовать после удаления");
        assertTrue(loaded.getEpics().isEmpty(), "Epics должны быть пусты после removeAll(EPIC)");
        assertTrue(loaded.getSubtasks().isEmpty(), "Subtasks должны быть пусты после removeAll(EPIC)");
    }

}