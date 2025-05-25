package controllers;

import model.Epic;
import model.Task;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static util.TaskStatus.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {


    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager(Managers.getDefaultHistory());
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
    void testGetPrioritizedTasks() {
        Task t1 = new Task("t1", "d1", NEW);
        Task t2 = new Task("t2", "d2", NEW);
        Task t3 = new Task("t3", "d3", NEW);
        Task t4 = new Task("t4", "d4", NEW);
        Task t5 = new Task("t5", "d5", NEW);

        t1.setStartTime(start.plusDays(10));
        t1.setDuration(duration);

        t2.setStartTime(start.plusDays(8));
        t2.setDuration(duration);

        t3.setStartTime(start.plusDays(7));
        t3.setDuration(duration);

        t4.setStartTime(start.plusDays(6));
        t4.setDuration(duration);

        t5.setStartTime(start.plusDays(5));
        t5.setDuration(duration);

        taskManager.addTask(t1);
        taskManager.addTask(t2);
        taskManager.addTask(t3);
        taskManager.addTask(t4);
        taskManager.addTask(t5);

        List<Task> prioritized = taskManager.getPrioritizedTasks();
        List<Task> expectedOrder = List.of(task, subtask, t5, t4, t3, t2, t1);
        assertEquals(expectedOrder, new ArrayList<>(prioritized), "Задачи должны быть отсортированы по startTime в порядке возрастания");

    }

}
