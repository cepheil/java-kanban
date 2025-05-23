package controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FileBackedTaskManagerEmptyTest {

    private File tempFile;
    private FileBackedTaskManager taskManager;

    @BeforeEach
    void init() throws IOException {
        tempFile = File.createTempFile("tasks_empty", ".csv");
        tempFile.deleteOnExit();
        taskManager = new FileBackedTaskManager(Managers.getDefaultHistory(), tempFile);
    }

    @Test
    void saveLoadEmptyFile() throws IOException {
        taskManager.save();

        FileBackedTaskManager loaded = Managers.loadFromFile(tempFile);

        // Проверяем, что ни одна из коллекций не содержит элементов
        assertTrue(loaded.getTasks().isEmpty(), "Tasks должен быть пуст");
        assertTrue(loaded.getEpics().isEmpty(), "Epics должен быть пуст");
        assertTrue(loaded.getSubtasks().isEmpty(), "Subtasks должен быть пуст");
    }
}
