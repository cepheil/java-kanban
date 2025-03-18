import java.util.Scanner;

public class Main {


    public static void main(String[] args) {
        String name;
        String description;
        TaskStatus status;
        TaskType taskType;
        Scanner scanner = new Scanner(System.in);
        TaskManager manager = new TaskManager();


        while (true) {
            printMenu();
            String cmd = scanner.nextLine();

            switch (cmd) {
                case "1":
                    System.out.println("Печать списка всех задач.");
                    manager.printAllTasks();
                    break;

                case "2":
                    System.out.println("Печать списка всех задач выбранного типа.");
                    taskType = chooseTaskType(scanner);
                    switch (taskType) {
                        case TASK:
                            manager.getTasks();  // мапа тасков
                            break;
                        case EPIC:
                            manager.getEpics(); // мапа Эпиков
                            break;
                        case SUBTASK:
                            manager.getSubtasks(); //мапа Подзадач
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
                            manager.getTaskById(taskID);
                            break;
                        case EPIC:
                            manager.getEpicById(taskID);
                            break;
                        case SUBTASK:
                            manager.getSubtaskById(taskID);
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
                        case TASK:
                            Task taskForUpd = new Task("Ужин", "Приготовить еду самостоятельно", TaskStatus.NEW);
                            manager.updateTask(2, taskForUpd);
                            break;
                        case EPIC:
                            Epic epicForUpd = new Epic("Купить весло", "Купить весло, втрое найти", TaskStatus.IN_PROGRESS);
                            manager.updateEpic(6, epicForUpd);
                            break;
                        case SUBTASK:
                            Subtask subtaskForUpd = new Subtask("Словить иксы", "Купить DOGE на низах", TaskStatus.IN_PROGRESS, 6);
                            manager.updateSubtask(7, subtaskForUpd);
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
                    manager.getSubtasksList(taskID);
                    break;

                case "9":
                    addPreloadedTasks(manager);
                    System.out.println("Предустановленные задачи успешно добавлены.");
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
        System.out.println("0. ВЫХОД");
    }

    public static Task createTask(String name, String description, TaskStatus status) {
        Task task = new Task(name, description, status);
        return task;
    }

    public static Epic createEpic(String name, String description, TaskStatus status) {
        Epic epic = new Epic(name, description, status);
        System.out.println("Создана ЭПИК задача taskID: " + epic.getTaskID());
        return epic;
    }

    public static Subtask createSubtask(String name, String description, TaskStatus status, int epicID) {
        Subtask subtask = new Subtask(name, description, status, epicID);
        return subtask;
    }

    public static TaskType chooseTaskType(Scanner scanner) {
        System.out.println("Введите тип задачи Обычная [T]  Эпик [E]  Подзадача [S] ");
        String type = scanner.nextLine();
        TaskType taskType;
        switch (type.toUpperCase()) {
            case "T":
                taskType = TaskType.TASK;
                break;
            case "E":
                taskType = TaskType.EPIC;
                break;
            case "S":
                taskType = TaskType.SUBTASK;
                break;
            default:
                System.out.print("по умолчанию  ");
                taskType = TaskType.TASK;
                break;
        }
        System.out.println("выбрана задача " + taskType);
        return taskType;
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

    public static void addPreloadedTasks(TaskManager manager) {
        Task task1 = new Task("Сходить в магазин", "Купить продукты на неделю.", TaskStatus.NEW);
        manager.addTask(task1);
        Task task2 = new Task("Обед", "Заказать еду через приложение", TaskStatus.IN_PROGRESS);
        manager.addTask(task2);

        Epic epic1 = new Epic("Поездка в Сочи", "Поездка в Сочи на горнолыжку", TaskStatus.NEW);
        manager.addEpic(epic1);
        Subtask subtask1 = new Subtask("Билеты на самолет", "Купить билеты в Сочи",
                TaskStatus.NEW, epic1.getTaskID());
        manager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask("Отель в Сочи", "Выбрать подходящий отель и забронировать его",
                TaskStatus.IN_PROGRESS, epic1.getTaskID());
        manager.addSubtask(subtask2);

        Epic epic2 = new Epic("Обновление компьютера", "Заменить комплектующие пк", TaskStatus.NEW);
        manager.addEpic(epic2);
        Subtask subtask3 = new Subtask("купить видеокарту", "выбрать подходящую вк из серии 50..",
                TaskStatus.NEW, epic2.getTaskID());
        manager.addSubtask(subtask3);
        Subtask subtask4 = new Subtask("купить Блок Питания", "подобрать БП от 1200 Вт",
                TaskStatus.IN_PROGRESS, epic2.getTaskID());
        manager.addSubtask(subtask4);

    }


}
