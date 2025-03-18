import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, Epic> epics = new HashMap<>();
    private HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private HashMap<Integer, Task> deleted = new HashMap<>();


    // a. Получение списка всех задач. Task
    public HashMap<Integer, Task> getTasks() {
        if (tasks.isEmpty()) {
            System.out.println("Ошибка! Нет задач типа Task");
        }
        for (Task task : tasks.values()) {
            System.out.println(task);
        }
        return tasks;
    }

    // a. Получение списка всех задач. Epic
    public HashMap<Integer, Epic> getEpics() {
        if (epics.isEmpty()) {
            System.out.println("Ошибка! Нет задач типа ЭПИК");
        }
        for (Task task : epics.values()) {
            System.out.println(task);
        }
        return epics;
    }

    // a. Получение списка всех задач. Subtask
    public HashMap<Integer, Subtask> getSubtasks() {
        if (subtasks.isEmpty()) {
            System.out.println("Ошибка! Нет задач типа ПОДЗАДАЧА");
        }
        for (Task task : subtasks.values()) {
            System.out.println(task);
        }
        return subtasks;
    }

    //c. Получение по идентификатору.
    public Task getTaskById(int taskId) {
        if (!tasks.containsKey(taskId)) {
            System.out.println("Ошибка! Задачи с таким ID не существует.");
        }
        return tasks.get(taskId);
    }

    //c. Получение по идентификатору.
    public Epic getEpicById(int taskId) {
        if (!epics.containsKey(taskId)) {
            System.out.println("Ошибка! ЭПИК  с таким ID не существует.");
        }
        return epics.get(taskId);
    }

    //c. Получение по идентификатору.
    public Subtask getSubtaskById(int taskId) {
        if (!subtasks.containsKey(taskId)) {
            System.out.println("Ошибка! Подзадачи  с таким ID не существует.");
        }
        return subtasks.get(taskId);
    }

    //d. Создание. Сам объект должен передаваться в качестве параметра. Task
    public void addTask(Task newTask) {
        if (tasks.isEmpty()) {
            tasks.put(newTask.getTaskID(), newTask); // если МАП пустой - добавляем таск
            System.out.println("Добавлена задача: " + newTask);
        } else {
            for (Task task : tasks.values()) { // иначе, сравниваем все элементы
                if (task.equals(newTask)) {  // если имя и описание одинаковые, сравниваем статус задачи
                    if (task.getStatus() != newTask.getStatus()) {
                        System.out.println("Задача " + newTask.getName() + "уже есть в списке с другим статусом" + task.getStatus());
                        task.setStatus(newTask.getStatus()); //  если статусы не равны - обновляем статус на новый.
                        System.out.println("обновляем статус до " + task.getStatus());
                        return;
                    } else {
                        System.out.println("Задача с таким именем, описанием и статусом уже есть в списке");
                        return;
                    }
                }
            }  // если повторов нет, значит добавляем в мапу новую Таску.
            tasks.put(newTask.getTaskID(), newTask);
            System.out.println("Добавлена задача: " + newTask);
        }
    }

    //d. Создание. Сам объект должен передаваться в качестве параметра. Epic
    public void addEpic(Epic newEpic) {
        if (tasks.isEmpty()) {
            epics.put(newEpic.getTaskID(), newEpic);
            System.out.println("Добавлен ЭПИК: " + newEpic);
        } else {
            for (Epic epic : epics.values()) {
                if (epic.equals(newEpic)) {
                    System.out.println("Задача ЭПИК с таким именем, описанием  уже есть в списке");
                    return;
                }
            }
            epics.put(newEpic.getTaskID(), newEpic);
            System.out.println("Добавлен ЭПИК: " + newEpic);
        }
    }

    //d. Создание. Сам объект должен передаваться в качестве параметра. Subtask
    public void addSubtask(Subtask newSubtask) {
        if (!epics.containsKey(newSubtask.getEpicID())) {
            System.out.println("ЭПИК задачи с epicID" + newSubtask.getEpicID() + "не найдено!");
            return;
        }

        if (subtasks.isEmpty()) {
            subtasks.put(newSubtask.getTaskID(), newSubtask);  // создаем подзадачу для ЭПИКА epic.getTaskID = EpicID
            System.out.println("Добавлен Подзадача: " + newSubtask);
            Epic tempEpic = epics.get(newSubtask.getEpicID());
            tempEpic.setSubtaskIdList(newSubtask.getTaskID()); //  в этом эпике добавляем в лист ID подзадачи
            updEpicStatus(tempEpic); // обновляем статус в эпике через метод
        } else {
            for (Subtask subtask : subtasks.values()) {
                if (subtask.equals(newSubtask)) {
                    if (subtask.getStatus() != newSubtask.getStatus()) {
                        System.out.println("Задача " + newSubtask.getName() + "уже есть в списке с другим статусом" + subtask.getStatus());
                        subtask.setStatus(newSubtask.getStatus()); //  обновляем сатус подзадачи
                        System.out.println("обновляем статус до " + subtask.getStatus());
                        updEpicStatus(epics.get(subtask.getEpicID())); // обновляем стутс эпика
                        return;
                    } else {
                        System.out.println("Подзадача с таким именем, описанием и статусом уже есть в списке");
                    }
                }
            }
            subtasks.put(newSubtask.getTaskID(), newSubtask);
            epics.get(newSubtask.getEpicID()).setSubtaskIdList(newSubtask.getTaskID());
            System.out.println("Добавлен Подзадача: " + newSubtask);
            updEpicStatus(epics.get(newSubtask.getEpicID()));
        }
    }

    //b. Для эпиков: Управление статусами
    public void updEpicStatus(Epic epic) {
        ArrayList<Integer> subtaskIdList = epic.getSubtaskIdList();
        if (subtaskIdList == null || subtaskIdList.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            System.out.println("Эпик обновлён до: " + epic.getStatus());
            return;
        }
        int newCounter = 0;
        int doneCounter = 0;
        for (Integer subtaskId : subtaskIdList) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask == null) {
                continue;
            }
            TaskStatus subTaskStatus = subtask.getStatus();
            if (subTaskStatus.equals(TaskStatus.NEW)) {
                newCounter++;
            } else if (subTaskStatus.equals(TaskStatus.DONE)) {
                doneCounter++;
            }
        }
        TaskStatus result;
        if (newCounter == subtaskIdList.size()) {
            result = TaskStatus.NEW;
        } else if (doneCounter == subtaskIdList.size()) {
            result = TaskStatus.DONE;
        } else {
            result = TaskStatus.IN_PROGRESS;
        }
        epic.setStatus(result);
        System.out.println("ЭПИК статус после обновления: " + result);
    }

    // b. Удаление всех задач.
    public void removeAllTasks(TaskType type) {
        switch (type) {
            case TASK:
                if (tasks.isEmpty()) {
                    System.out.println("список задач пуст");
                } else {
                    for (Task task : tasks.values()) {
                        task.setStatus(TaskStatus.DELETED);
                        deleted.put(task.getTaskID(), task);
                        System.out.println("Задача удалена" + task);
                    }
                    tasks.clear();
                    System.out.println("Задачи " + type + " удалены");
                }
                break;
            case EPIC:
                if (tasks.isEmpty()) {
                    System.out.println("список ЭПИК пуст");
                } else {
                    for (Epic epic : epics.values()) {
                        epic.setStatus(TaskStatus.DELETED);
                        deleted.put(epic.getTaskID(), epic);
                        System.out.println("эпик удален" + epic);
                        for (Integer subtaskId : epic.getSubtaskIdList()) {
                            subtasks.get(subtaskId).setStatus(TaskStatus.DELETED);
                            deleted.put(subtaskId, subtasks.get(subtaskId));
                            System.out.println("подзадача удалена" + subtasks.get(subtaskId));
                            subtasks.remove(subtaskId);
                        }
                    }
                    epics.clear();
                }
                break;
            case SUBTASK:
                if (subtasks.isEmpty()) {
                    System.out.println("список подзадач пуст");
                } else {
                    for (Subtask subtask : subtasks.values()) {
                        subtask.setStatus(TaskStatus.DELETED);
                        deleted.put(subtask.getTaskID(), subtask);
                        System.out.println("подзадача удалена" + subtask);
                        epics.get(subtask.getEpicID()).setStatus(TaskStatus.NEW);
                        System.out.println("У эпика обновлен статус" + epics.get(subtask.getEpicID()));
                    }
                    subtasks.clear();
                    System.out.println("подзадачи " + type + " удалены");
                }
                break;
            default:
                System.out.println("Ошибка! " + type + " - такого типа нет ");
                break;
        }
    }

    //a. Получение списка всех подзадач определённого эпика.
    public ArrayList<Integer> getSubtasksList(int taskID) {
        ArrayList<Integer> subtasksList = new ArrayList<>();
        if (!epics.containsKey(taskID)) {
            System.out.println("ЭПИК задачи с taskID: " + taskID + " не найдено!");
            System.out.println("subtasksList: = null ");

        } else {
            Epic epic = epics.get(taskID);
            System.out.println("тест epic " + epic.toString());
            subtasksList = epic.getSubtaskIdList();
            System.out.println("тест subtasksList " + subtasksList);
            if (subtasksList.isEmpty()) {
                System.out.println("у ЭПИКА " + taskID + " нет подзадач!");
            } else {
                for (Integer subtasksID : subtasksList) {
                    System.out.println(subtasks.get(subtasksID));
                }
            }
        }
        return subtasksList;
    }

    //e. Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
    public void updateTask(int taskId, Task task) {
        if (tasks.containsKey(taskId)) {
            tasks.get(taskId).setName(task.getName());
            tasks.get(taskId).setDescription(task.getDescription());
            tasks.get(taskId).setStatus(task.getStatus());
        } else {
            System.out.println(" Задачи с таким id " + taskId + "  нет в таблице");
            return;
        }
    }

    //e. Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
    public void updateSubtask(int taskId, Subtask subtask) {
        if (subtasks.containsKey(taskId)) {
            subtasks.get(taskId).setName(subtask.getName());
            subtasks.get(taskId).setDescription(subtask.getDescription());
            subtasks.get(taskId).setStatus(subtask.getStatus());
            subtasks.get(taskId).getEpicID();
            updEpicStatus(epics.get(subtasks.get(taskId).getEpicID()));
        } else {
            System.out.println(" Подзадачи с таким id " + taskId + "  нет в таблице");
            return;
        }
    }

    //e. Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
    public void updateEpic(int taskId, Epic epic) {
        if (epics.containsKey(taskId)) {
            epics.get(taskId).setName(epic.getName());
            epics.get(taskId).setDescription(epic.getDescription());
        } else {
            System.out.println(" ЭПИК с таким id " + taskId + "  нет в таблице");
            return;
        }
    }

    // f. Удаление по идентификатору.
    public void removeTaskById(int taskId) {
        if (tasks.containsKey(taskId)) {
            Task task = tasks.get(taskId);
            task.setStatus(TaskStatus.DELETED);
            deleted.put(task.getTaskID(), task);
            tasks.remove(taskId);
            System.out.println("Задача удалена: " + task);
            return;
        }

        if (subtasks.containsKey(taskId)) {
            Subtask subtaskToDel = subtasks.get(taskId);
            int epicId = subtasks.get(taskId).getEpicID();
            subtaskToDel.setStatus(TaskStatus.DELETED);
            deleted.put(subtaskToDel.getTaskID(), subtaskToDel);

            subtasks.remove(taskId);
            System.out.println("Подзадача удалена: " + subtaskToDel);
            Epic epic = epics.get(epicId);
            if (epic != null) {
                updEpicStatus(epic);
            }
            return;
        }

        if (epics.containsKey(taskId)) {
            Epic epic = epics.get(taskId);
            epic.setStatus(TaskStatus.DELETED);
            deleted.put(epic.getTaskID(), epic);
            System.out.println("Лист подзадач эпика " + epic + "для удаления :" + epic.getSubtaskIdList());
            for (Integer subtaskId : epic.getSubtaskIdList()) {
                removeTaskById(subtaskId);
            }
            epics.remove(taskId);
            System.out.println("Эпик удалён: " + epic);
            return;
        }
    }

    // метод для отладки - вывод всех задач.
    public void printAllTasks() {
        if (tasks.isEmpty() && epics.isEmpty() && subtasks.isEmpty()) {
            System.out.println("Список задач пуст.");
            return;
        }
        System.out.println("\n===== СПИСОК ВСЕХ ЗАДАЧ =====");
        if (!tasks.isEmpty()) {
            System.out.println("\n--- Обычные задачи ---");
            for (Task task : tasks.values()) {
                System.out.println(task);
            }
        }
        if (!epics.isEmpty()) {
            System.out.println("\n--- Эпики и их подзадачи ---");
            for (Epic epic : epics.values()) {
                System.out.println(epic); // Вывод самого эпика
                ArrayList<Integer> subtaskIds = epic.getSubtaskIdList();
                System.out.println("Подзадачи эпика [" + epic.getTaskID() + "]: " + subtaskIds);
                if (!subtaskIds.isEmpty()) {
                    System.out.println("   Подзадачи:");
                    for (Integer subtaskId : subtaskIds) {
                        Subtask subtask = subtasks.get(subtaskId);
                        if (subtask != null) {
                            System.out.println("   ↳ " + subtask);
                        } else {
                            System.out.println("   ⚠ Ошибка: Подзадача с ID " + subtaskId + " не найдена!");
                        }
                    }
                } else {
                    System.out.println("   (Эпик пока не содержит подзадач)");
                }
            }
        }
        System.out.println("\n============================");
    }

}
