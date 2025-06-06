import controllers.FileBackedTaskManager;
import controllers.Managers;
import controllers.TaskManager;
import model.Epic;
import model.Subtask;
import model.Task;
import util.TaskStatus;
import util.TaskType;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Main {


    public static void main(String[] args) {
        String name;
        String description;
        TaskStatus status;
        TaskType taskType;
        Scanner scanner = new Scanner(System.in);
        TaskManager manager = Managers.getDefault();
        File file = new File("resources/tasks.csv");


        while (true) {
            printMenu();
            String cmd = scanner.nextLine();

            switch (cmd) {
                case "1":
                    System.out.println("Печать списка всех задач.");
                    printAllTasks(manager);
                    break;

                case "2":
                    System.out.println("Печать списка всех задач выбранного типа.");
                    taskType = chooseTaskType(scanner);

                    switch (taskType) {

                        case TASK:
                            System.out.println(manager.getTasks()); // лист Тасков
                            break;
                        case EPIC:
                            System.out.println(manager.getEpics()); // лист Эпиков
                            break;
                        case SUBTASK:
                            System.out.println(manager.getSubtasks()); //лист Подзадач
                            break;
                    }
                    break;

                case "3":
                    System.out.println("Удаление всех задач");
                    manager.removeAllTasks(chooseTaskType(scanner));
                    break;

                case "4":
                    System.out.println("Получение по ID");
                    taskType = chooseTaskType(scanner);
                    System.out.println(" введите taskID: ");
                    int taskID = scanner.nextInt();
                    scanner.nextLine();
                    switch (taskType) {
                        case TASK:
                            System.out.println(manager.getTaskById(taskID));
                            break;
                        case EPIC:
                            System.out.println(manager.getEpicById(taskID));
                            break;
                        case SUBTASK:
                            System.out.println(manager.getSubtaskById(taskID));
                            break;
                    }
                    break;

                case "5":
                    System.out.println("создание задачи");
                    taskType = chooseTaskType(scanner);
                    System.out.println("введите название задачи ");
                    name = scanner.nextLine();
                    System.out.println("введите описание задачи ");
                    description = scanner.nextLine();

                    switch (taskType) {
                        case TASK:
                            System.out.println("создание задачи " + TaskType.TASK);
                            System.out.println("выберите статус задачи ");
                            status = chooseStatus(scanner);
                            manager.addTask(createTask(name, description, status));
                            break;
                        case EPIC:
                            System.out.println("создание задачи " + TaskType.EPIC);
                            status = TaskStatus.NEW;
                            manager.addEpic(createEpic(name, description, status));
                            break;
                        case SUBTASK:
                            System.out.println("создание задачи " + TaskType.SUBTASK);
                            System.out.println("введите epicID ");
                            int epicID = scanner.nextInt();
                            scanner.nextLine();
                            System.out.println("выберите статус задачи ");
                            status = chooseStatus(scanner);
                            manager.addSubtask(createSubtask(name, description, status, epicID));
                            break;
                    }
                    break;

                case "6":
                    System.out.println("обновление задачи");
                    taskType = chooseTaskType(scanner);
                    switch (taskType) {
                        case TASK:// Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
                            updateTaskLogic(scanner, manager);
                            break;
                        case EPIC: // Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
                            updateEpicLogic(scanner, manager);
                            break;
                        case SUBTASK: // Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
                            updateSubtaskLogic(scanner, manager);
                            break;
                    }
                    break;

                case "7":
                    System.out.println("Введите taskID для удаления:");
                    int removeID = scanner.nextInt();
                    scanner.nextLine();
                    manager.removeTaskById(removeID);
                    break;

                case "8":
                    System.out.println("Введите epic taskID  для получения списка подзадач :");
                    taskID = scanner.nextInt();
                    scanner.nextLine();
                    System.out.println(manager.getSubtasksList(taskID));
                    break;

                case "9":
                    addPreloadedTasks(manager);
                    System.out.println("Предустановленные задачи успешно добавлены.");
                    break;


                case "10":
                    printHistory(manager);
                    break;

                case "11":
                    System.out.println("Запуск сценария ТЗ_6");
                    addPreloadedTasks(manager); // загружены предустановленные задачи
                    /////////////////////////////////////////////
                    System.out.println("Запрашиваем задачу №2 :" + manager.getTaskById(2));
                    System.out.println("Запрашиваем задачу №1 :" + manager.getTaskById(1));
                    System.out.println("Запрашиваем подзадачу №7 :" + manager.getSubtaskById(7));
                    System.out.println("Запрашиваем подзадачу №4 :" + manager.getSubtaskById(4));
                    System.out.println("Запрашиваем эпик №10 :" + manager.getEpicById(10));
                    printHistory(manager);

                    System.out.println("Запрашиваем задачу №2 :" + manager.getTaskById(2));
                    printHistory(manager);

                    System.out.println("Запрашиваем задачу №1 :" + manager.getTaskById(1));
                    System.out.println("Запрашиваем эпик №10 :" + manager.getEpicById(10));
                    System.out.println("Запрашиваем эпик №3 :" + manager.getEpicById(3));
                    System.out.println("Запрашиваем подзадачу №7 :" + manager.getSubtaskById(7));
                    printHistory(manager);


                    System.out.println("Удаляем задачу №1 :");
                    manager.removeTaskById(1);
                    printHistory(manager);

                    System.out.println("Запрашиваем подзадачу №5 :" + manager.getSubtaskById(5));
                    System.out.println("Запрашиваем эпик №6 :" + manager.getEpicById(6));
                    System.out.println("Запрашиваем подзадачу №7 :" + manager.getSubtaskById(7));
                    System.out.println("Запрашиваем подзадачу №9 :" + manager.getSubtaskById(9));
                    System.out.println("Запрашиваем подзадачу №8 :" + manager.getSubtaskById(8));
                    printHistory(manager);

                    System.out.println("Запрашиваем задачу №2 :" + manager.getTaskById(2));
                    System.out.println("Удаляем эпик №6 и его подзадачи:");
                    manager.removeTaskById(6);
                    printHistory(manager);

                    break;

                case "12":
                    System.out.println("Запуск сценария ТЗ_7");
                    FileBackedTaskManager backedManager = (FileBackedTaskManager) Managers.getBackedTaskManager(file);
                    addPreloadedTasks(backedManager); // загружены предустановленные задачи

                    System.out.println("задачи сохранены в файл: " + file.getAbsolutePath());

                    FileBackedTaskManager loadedTaskManager;
                    try {
                        loadedTaskManager = Managers.loadFromFile(file);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    System.out.println(" Печать списка всех задач для backedManager");
                    printAllTasks(backedManager);
                    System.out.println();
                    System.out.println(" Печать списка всех задач для loadedTaskManager");
                    printAllTasks(loadedTaskManager);
                    break;

                case "13":
                    System.out.println("Выводим список задач в порядке приоритета");
                    addPreloadedTasks(manager);
                    System.out.println("Предустановленные задачи успешно добавлены.");
                    List<Task> prioritizedTasks = manager.getPrioritizedTasks();
                    for (Task t : prioritizedTasks) {
                        System.out.println(t);
                    }
                    break;


                case "0":
                    System.out.println("выход из программы");
                    return;


                default:
                    System.out.println("такого пункта не существует:" + cmd);
                    System.out.println("повторите выбор.");
                    break;
            }
        }
    }

    public static void printMenu() {
        System.out.println("Введите одну из команд: ");
        System.out.println("1. печать списка всех задач  ");
        System.out.println("2. печать списка всех задач выбранного типа  ");
        System.out.println("3. удаление всех задач ");
        System.out.println("4. получение по ID ");
        System.out.println("5. создание задачи ");
        System.out.println("6. обновление задачи по ID"); ///Новая версия объекта с верным идентификатором передаётся в виде параметра.
        System.out.println("7. удаление по ID ");
        System.out.println("8. Получение списка всех подзадач определённого эпика. по epicID");
        System.out.println("9. Загрузить предустановленные задачи");
        System.out.println("10. Печать истории");
        System.out.println("11. Запустить сценарий ТЗ_6");
        System.out.println("12. Запустить сценарий ТЗ_7");
        System.out.println("13. Вывести список задач в порядке приоритета ТЗ_8");
        System.out.println("0. ВЫХОД");
    }

    public static Task createTask(String name, String description, TaskStatus status) {
        Task task = new Task(name, description, status);
        return task;
    }

    public static Epic createEpic(String name, String description, TaskStatus status) {
        Epic epic = new Epic(name, description);
        System.out.println("Создана ЭПИК задача taskID: " + epic.getTaskID());
        return epic;
    }

    public static Subtask createSubtask(String name, String description, TaskStatus status, int epicID) {
        Subtask subtask = new Subtask(name, description, status, epicID);
        return subtask;
    }

    public static TaskType chooseTaskType(Scanner scanner) {
        while (true) {
            System.out.println("Введите тип задачи Обычная [T]  Эпик [E]  Подзадача [S] ");
            String type = scanner.nextLine();
            switch (type.toUpperCase()) {
                case "T":
                    return TaskType.TASK;
                case "E":
                    return TaskType.EPIC;
                case "S":
                    return TaskType.SUBTASK;
                default:
                    System.out.print("Ошибка!  Введите тип задачи Обычная [T], Эпик [E], Подзадача [S] ");
            }
        }
    }

    public static TaskStatus chooseStatus(Scanner scanner) {
        while (true) {
            System.out.println("Введите статус: N (NEW), P (IN_PROGRESS), D (DONE)");
            String status = scanner.nextLine();

            switch (status.toUpperCase()) {
                case "N":
                    return TaskStatus.NEW;

                case "P":
                    return TaskStatus.IN_PROGRESS;

                case "D":
                    return TaskStatus.DONE;

                default:
                    System.out.println("Ошибка! Введите N, P или D.");
            }
        }
    }

    // метод для отладки - вывод всех задач.
    public static void printAllTasks(TaskManager manager) {
        ArrayList<Task> tasks = manager.getTasks();
        ArrayList<Subtask> subtasks = manager.getSubtasks();
        ArrayList<Epic> epics = manager.getEpics();


        if (tasks.isEmpty() && epics.isEmpty() && subtasks.isEmpty()) {
            System.out.println("Список задач пуст.");
            return;
        }
        System.out.println("\n===== СПИСОК ВСЕХ ЗАДАЧ =====");
        if (!tasks.isEmpty()) {
            System.out.println("\n--- Обычные задачи ---");
            for (Task task : tasks) {
                System.out.println(task);
            }
        }
        if (!epics.isEmpty()) {
            System.out.println("\n--- Эпики и их подзадачи ---");
            for (Epic epic : epics) {
                System.out.println(epic); // Вывод самого эпика

                ArrayList<Integer> subtaskIds = epic.getSubtaskIdList();   // получаем список ID подзадач эпика
                System.out.println("Подзадачи эпика [" + epic.getTaskID() + "]: " + subtaskIds);
                if (!subtaskIds.isEmpty()) {
                    System.out.println("   Подзадачи:");

                    for (Integer subtaskId : subtaskIds) {
                        //Subtask subtask = manager.getSubtaskById(subtaskId); - наполняет историю подзадачаими.
                        Subtask subtask = manager.getSubtasksMap().get(subtaskId); //изменен вывод через новый геттер.
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


    public static void addPreloadedTasks(TaskManager manager) {
        Task task1 = new Task("Задача_1", "Описание Задачи_1", TaskStatus.NEW);
        task1.setStartTime(LocalDateTime.now());
        task1.setDuration(Duration.ofMinutes(1));
        manager.addTask(task1);

        Task task2 = new Task("Задача_2", "Описание Задачи_2", TaskStatus.IN_PROGRESS);
        task2.setStartTime(LocalDateTime.now().plus(Duration.ofMinutes(60)));
        task2.setDuration(Duration.ofMinutes(1));
        manager.addTask(task2);

        Epic epic1 = new Epic("Эпик_1", "Описание Эпика_1");
        manager.addEpic(epic1);
        Subtask subtask1 = new Subtask("Подзадача_1", "Описание Подзадачи_1",
                TaskStatus.NEW, epic1.getTaskID());
        subtask1.setStartTime(LocalDateTime.now().plus(Duration.ofMinutes(120)));
        subtask1.setDuration(Duration.ofMinutes(1));
        manager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask("Подзадача_2", "Описание Подзадачи_2",
                TaskStatus.IN_PROGRESS, epic1.getTaskID());
        subtask2.setStartTime(LocalDateTime.now().plus(Duration.ofMinutes(180)));
        subtask2.setDuration(Duration.ofMinutes(1));
        manager.addSubtask(subtask2);


        Epic epic2 = new Epic("Эпик_2", "Описание Эпика_2");
        manager.addEpic(epic2);
        Subtask subtask3 = new Subtask("Подзадача_3", "Описание Подзадачи_3",
                TaskStatus.NEW, epic2.getTaskID());
        subtask3.setStartTime(LocalDateTime.now().plus(Duration.ofMinutes(240)));
        subtask3.setDuration(Duration.ofMinutes(1));
        manager.addSubtask(subtask3);
        Subtask subtask4 = new Subtask("Подзадача_4", "Описание Подзадачи_4",
                TaskStatus.IN_PROGRESS, epic2.getTaskID());
        subtask4.setStartTime(LocalDateTime.now().plus(Duration.ofMinutes(300)));
        subtask4.setDuration(Duration.ofMinutes(1));
        manager.addSubtask(subtask4);
        Subtask subtask5 = new Subtask("Подзадача_5", "Описание Подзадачи_5",
                TaskStatus.DONE, epic2.getTaskID());
        subtask5.setStartTime(LocalDateTime.now().plus(Duration.ofMinutes(360)));
        subtask5.setDuration(Duration.ofMinutes(1));
        manager.addSubtask(subtask5);

        Epic epic3 = new Epic("Эпик_3", "Описание Эпика_3");
        manager.addEpic(epic3);


    }

    public static void printHistory(TaskManager manager) {
        System.out.println("Печать истории:");

        if (manager.getHistory().isEmpty()) {
            System.out.println("История пустая.");
        }

        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }

    }

    public static void updateTaskLogic(Scanner scanner, TaskManager manager) {
        System.out.println("Введите ID задачи для обновления:");
        int taskId = scanner.nextInt();
        scanner.nextLine();

        Task existingTask = manager.getTaskById(taskId);
        if (existingTask == null) {
            System.out.println("Задача с ID " + taskId + " не найдена!");
            return;
        }

        System.out.println("Введите новое название задачи (текущее: '" + existingTask.getName() + "'):");
        String newName = scanner.nextLine();

        System.out.println("Введите новое описание задачи (текущее: '" + existingTask.getDescription() + "'):");
        String newDescription = scanner.nextLine();

        System.out.println("Выберите новый статус задачи:");
        TaskStatus newStatus = chooseStatus(scanner);

        Task updatedTask = new Task(newName, newDescription, newStatus);
        updatedTask.setTaskID(taskId);
        manager.updateTask(updatedTask);
        System.out.println("Задача обновлена!");
    }

    public static void updateEpicLogic(Scanner scanner, TaskManager manager) {
        System.out.println("Введите ID эпика для обновления:");
        int epicId = scanner.nextInt();
        scanner.nextLine();

        Epic existingEpic = manager.getEpicById(epicId);
        if (existingEpic == null) {
            System.out.println("Эпик с ID " + epicId + " не найден!");
            return;
        }

        System.out.println("Введите новое название эпика (текущее: '" + existingEpic.getName() + "'):");
        String newName = scanner.nextLine();

        System.out.println("Введите новое описание эпика (текущее: '" + existingEpic.getDescription() + "'):");
        String newDescription = scanner.nextLine();

        Epic updatedEpic = new Epic(newName, newDescription);
        updatedEpic.setTaskID(epicId);
        manager.updateEpic(updatedEpic);
        System.out.println("Эпик обновлен!");
    }

    public static void updateSubtaskLogic(Scanner scanner, TaskManager manager) {
        System.out.println("Введите ID подзадачи для обновления:");
        int subtaskId = scanner.nextInt();
        scanner.nextLine();

        Subtask existingSubtask = manager.getSubtaskById(subtaskId);
        if (existingSubtask == null) {
            System.out.println("Подзадача с ID " + subtaskId + " не найдена!");
            return;
        }

        System.out.println("Введите новое название подзадачи (текущее: '" + existingSubtask.getName() + "'):");
        String newName = scanner.nextLine();

        System.out.println("Введите новое описание подзадачи (текущее: '" + existingSubtask.getDescription() + "'):");
        String newDescription = scanner.nextLine();

        System.out.println("Выберите новый статус подзадачи:");
        TaskStatus newStatus = chooseStatus(scanner);

        System.out.println("Введите новый Epic ID (текущий: " + existingSubtask.getEpicID() + "):");
        int newEpicId = scanner.nextInt();
        scanner.nextLine();

        Subtask updatedSubtask = new Subtask(newName, newDescription, newStatus, newEpicId);
        updatedSubtask.setTaskID(subtaskId);
        manager.updateSubtask(updatedSubtask);
        System.out.println("Подзадача обновлена!");
    }


}
