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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static util.TaskStatus.*;

class FileBackedTaskManagerTest {
    private FileBackedTaskManager manager;
    private Task task;
    private Epic epic;
    private Subtask subtask;
    private int epicId;
    private File tempFile;


    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("tasks_empty", ".csv");
        tempFile.deleteOnExit();
        manager = new FileBackedTaskManager(Managers.getDefaultHistory(), tempFile);
    }

    @Test
    void saveLoadEmptyFile() throws IOException {
        manager.save();

        FileBackedTaskManager loaded = Managers.loadFromFile(tempFile);

        // Проверяем, что ни одна из коллекций не содержит элементов
        assertTrue(loaded.getTasks().isEmpty(), "Tasks должен быть пуст");
        assertTrue(loaded.getEpics().isEmpty(), "Epics должен быть пуст");
        assertTrue(loaded.getSubtasks().isEmpty(), "Subtasks должен быть пуст");
    }

    @Test
    void saveMultipleTasks() throws IOException {
        Task t1 = new Task("T1", "DescT1", TaskStatus.NEW);
        int idT = manager.addTask(t1);

        Epic e1 = new Epic("E1", "DescE1");
        int idE = manager.addEpic(e1);

        Subtask s1 = new Subtask("S1", "DescS1", TaskStatus.IN_PROGRESS, idE);
        int idS = manager.addSubtask(s1);

        manager.save();

        List<String> lines = Files.readAllLines(tempFile.toPath());

        assertEquals(1 + 3, lines.size(), "Количество строк в файле должно быть header + 3");

        assertEquals("id,type,name,status,description,epicID", lines.get(0).trim());

        // Проверяем, что в файле сохранились задачи по строкам
        String csv1 = t1.toCsv().trim();  //сохраняем такс в строку.
        String csv2 = e1.toCsv().trim();  //... эпик в строку
        String csv3 = s1.toCsv().trim();  //... сабтаск в строку

        assertEquals(csv1, lines.get(1).trim());
        assertEquals(csv2, lines.get(2).trim());
        assertEquals(csv3, lines.get(3).trim());
    }

    @Test
    void loadMultipleTasks() throws IOException {
        Task t1 = new Task("T1", "DescT1", TaskStatus.NEW);
        int idT = manager.addTask(t1);

        Epic e1 = new Epic("E1", "DescE1");
        int idE = manager.addEpic(e1);

        Subtask s1 = new Subtask("S1", "DescS1", TaskStatus.IN_PROGRESS, idE);
        int idS = manager.addSubtask(s1);

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

        assertEquals(t1.getName(), lt.getName());
        assertEquals(t1.getDescription(), lt.getDescription());
        assertEquals(t1.getStatus(), lt.getStatus());
        assertEquals(t1.getTaskID(), lt.getTaskID());

        assertEquals(e1.getName(), le.getName());
        assertEquals(e1.getDescription(), le.getDescription());
        assertEquals(e1.getTaskID(), le.getTaskID());
        assertEquals(1, le.getSubtaskIdList().size(), "эпик не содержит подзадачу");

        assertEquals(s1.getName(), ls.getName());
        assertEquals(s1.getDescription(), ls.getDescription());
        assertEquals(s1.getStatus(), ls.getStatus());
        assertEquals(s1.getTaskID(), ls.getTaskID());

        //связь сабтаска и эпика
        assertEquals(idE, ls.getEpicID());
        assertTrue(loaded.getEpicById(idE).getSubtaskIdList().contains(idS));
    }

    @Test
    void removeAllTasks() {
    }

    @Test
    void getAllTypesById() {
        Task task = new Task("T1", "DescT1", TaskStatus.NEW);
        int idT = manager.addTask(task);

        Epic epic = new Epic("E1", "DescE1");
        int idE = manager.addEpic(epic);

        Subtask subtask = new Subtask("S1", "DescS1", TaskStatus.IN_PROGRESS, idE);
        int idS = manager.addSubtask(subtask);


        Task savedTask = manager.getTaskById(idT);
        Epic savedEpic = manager.getEpicById(idE);
        Subtask savedSubtask = manager.getSubtaskById(idS);

        assertNotNull(savedTask, "Задача не найдена");
        assertNotNull(savedEpic, "Эпик не найден");
        assertNotNull(savedSubtask, "Подзадача не найдена");

        assertEquals(task, savedTask, "Задачи не равны");
        assertEquals(epic, savedEpic, "Эпики не равны");
        assertEquals(subtask, savedSubtask, "Подзадачи не равны");

    }


    @Test
    void historyShouldKeepPreviousTaskVersionAfterUpdate() throws IOException {
        Task task = new Task("T1", "DescT1", TaskStatus.NEW);
        int idT = manager.addTask(task);
        manager.getTaskById(idT); // добавляем в историю task {0}

        Epic epic = new Epic("E1", "DescE1");
        int idE = manager.addEpic(epic);
        TaskStatus epicStatus = epic.getStatus();  // NEW
        manager.getEpicById(idE); // добавляем в историю epic {1}

        Subtask subtask = new Subtask("S1", "DescS1", TaskStatus.IN_PROGRESS, idE);
        int idS = manager.addSubtask(subtask);
        manager.getSubtaskById(idS);  // добавляем в историю subtask {2}


        // обновляем задачу
        Task updatedTask = new Task("updTask", "updTask_Desc", IN_PROGRESS);
        updatedTask.setTaskID(idT);
        manager.updateTask(updatedTask);

        // обновляем эпик
        Epic epicUpdated = new Epic("updEpic", "updEpic_Desc");
        epicUpdated.setTaskID(idE);
        epicUpdated.setStatus(DONE);
        manager.updateEpic(epicUpdated);

        // обновляем подзадачу
        Subtask subtaskUpdated = new Subtask("updSubtask", "updSubtask_Desc", NEW, idE);
        subtaskUpdated.setTaskID(idS);
        manager.updateSubtask(subtaskUpdated);

        List<Task> history = manager.getHistory(); //получаем историю обращений к задачам.

        // проверка истории
        assertFalse(history.isEmpty(), "Ошибка! История пуста");
        assertNotNull(history, "Ошибка! history = null.");
        assertEquals(3, manager.getHistory().size(), "Ошибка! История должна хранить 3 элемента");

        // проверка хранения Тасков.
        assertEquals("T1", history.get(0).getName(), "name != не хранит исходную версию");
        assertEquals("DescT1", history.get(0).getDescription(),
                "Description != не хранит исходную версию");
        assertEquals(NEW, history.get(0).getStatus(), "Status != не хранит исходную версию");
        assertEquals(idT, history.get(0).getTaskID(), "taskId != не хранит исходную версию");

        // проверка хранения Эпиков.
        assertEquals("E1", history.get(1).getName(), "name != не хранит исходную версию");
        assertEquals("DescE1", history.get(1).getDescription(),
                "Description != не хранит исходную версию");
        assertEquals(epicStatus, history.get(1).getStatus(), "Status != не хранит исходную версию");


        // проверка хранения Сабтасков.
        assertEquals("S1", history.get(2).getName(), "name != не хранит исходную версию");
        assertEquals("DescS1", history.get(2).getDescription(),
                "Description != не хранит исходную версию");
        assertEquals(IN_PROGRESS, history.get(2).getStatus(), "Status != не хранит исходную версию");

        // проверка обновленных задач после загрузки из файла
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
        assertEquals(NEW, loadedEpic.getStatus(), "Ошибка! Epic status - не совпадают");

        assertEquals("updSubtask", loadedSubtask.getName(), "Ошибка! Subtask name - не совпадают");
        assertEquals("updSubtask_Desc", loadedSubtask.getDescription(),
                "Ошибка! Subtask description - не совпадают");
        assertEquals(NEW, loadedSubtask.getStatus(), "Ошибка! Subtask status - не совпадают");

    }

    @Test
    void removeTaskByIdAndRemoveAllTasksByType() throws IOException {
        Task task = new Task("T1", "DescT1", TaskStatus.NEW);
        int idT = manager.addTask(task);

        Epic epic = new Epic("E1", "DescE1");
        int idE = manager.addEpic(epic);

        Subtask subtask = new Subtask("S1", "DescS1", TaskStatus.IN_PROGRESS, idE);
        int idS = manager.addSubtask(subtask);


        manager.removeTaskById(idT);
        manager.removeAllTasks(TaskType.EPIC);


        //загружаем из файла
        FileBackedTaskManager loaded = Managers.loadFromFile(tempFile);


        assertNull(loaded.getTaskById(idT), "Task не должен существовать после удаления");
        assertTrue(loaded.getEpics().isEmpty(), "Epics должны быть пусты после removeAll(EPIC)");
        assertTrue(loaded.getSubtasks().isEmpty(), "Subtasks должны быть пусты после removeAll(EPIC)");

    }


}