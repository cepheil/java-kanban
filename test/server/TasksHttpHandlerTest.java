package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import controllers.Managers;
import model.Task;
import util.DurationAdapter;
import util.LocalDateTimeAdapter;
import util.TaskStatus;
import org.junit.jupiter.api.*;
import server.HttpTaskServer;
import controllers.InMemoryTaskManager;
import controllers.TaskManager;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TasksHttpHandlerTest {

    private TaskManager manager;
    private HttpTaskServer taskServer;
    private final Gson gson = HttpTaskServer.getGson();
    private final HttpClient client = HttpClient.newHttpClient();
    private static final String TASKS_URL = "http://localhost:8080/tasks";
    LocalDateTime start;
    Duration duration;

    @BeforeEach
    void setUp() throws IOException {
        manager = Managers.getDefault();
        taskServer = new HttpTaskServer(manager);
        taskServer.start();
        start = LocalDateTime.of(2030, 1, 1, 10, 0);
        duration = Duration.ofMinutes(10);
    }

    @AfterEach
    void tearDown() {
        taskServer.stop();
    }

    @Test
    void shouldAddTaskSuccessfully() throws IOException, InterruptedException {

        Task task = new Task("Test Task", "Task description",
                TaskStatus.NEW, start, duration);
        String json = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TASKS_URL))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();
        //HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Некорректный код ответа");

        List<Task> tasks = manager.getTasks();
        assertEquals(1, tasks.size(), "Некорректное количество задач");
        assertEquals("Test Task", tasks.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void testGetTasks() throws IOException, InterruptedException {
        Task task = new Task("Test Get", "Check GET method", TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(10));
        manager.addTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(TASKS_URL);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Некорректный код ответа при GET /tasks");


        class UserListTypeToken extends TypeToken<List<Task>> {
        }
        List<Task> tasks = gson.fromJson(response.body(), new UserListTypeToken().getType());

        assertNotNull(tasks, "Задачи не возвращаются");
        assertEquals(1, tasks.size(), "Неверное количество задач");
        assertEquals("Test Get", tasks.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void testGetAllTasks() throws IOException, InterruptedException {
        Task t1 = new Task("task1", "d1", TaskStatus.NEW, start, duration);
        Task t2 = new Task("task2", "d2", TaskStatus.IN_PROGRESS, start.plusDays(1), duration);
        Task t3 = new Task("task3", "d3", TaskStatus.DONE, start.plusDays(2), duration);
        manager.addTask(t1);
        manager.addTask(t2);
        manager.addTask(t3);

        // Отправляем GET-запрос
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(TASKS_URL);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Некорректный код ответа при GET /tasks");

        class UserListTypeToken extends TypeToken<List<Task>> {
        }
        List<Task> tasks = gson.fromJson(response.body(), new UserListTypeToken().getType());

        assertNotNull(tasks, "Задачи не возвращаются");
        assertEquals(3, tasks.size(), "Неверное количество задач");
        assertEquals("task1", tasks.get(0).getName(), "Некорректное имя задачи");
        assertEquals("task2", tasks.get(1).getName(), "Некорректное имя задачи");
        assertEquals("task3", tasks.get(2).getName(), "Некорректное имя задачи");
        assertEquals("d2", tasks.get(1).getDescription(), "Некорректное описание задачи");
        assertEquals(TaskStatus.DONE, tasks.get(2).getStatus(), "Некорректный статус задачи");
    }


    @Test
    public void testGetTaskById() throws IOException, InterruptedException {
        Task t1 = new Task("task1", "d1", TaskStatus.NEW, start, duration);
        int taskId = manager.addTask(t1);

        // Отправляем GET-запрос
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(TASKS_URL + "/" + taskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Некорректный код ответа");

        Task taskFromResponse = gson.fromJson(response.body(), Task.class); // получаем задачу из тела ответа.
        // Проверяем, что задача вернулась и её поля совпадают
        assertNotNull(taskFromResponse, "Задача не найдена в ответе");
        assertEquals(t1.getName(), taskFromResponse.getName(), "Имя задачи не совпадает");
        assertEquals(t1.getDescription(), taskFromResponse.getDescription(), "Описание задачи не совпадает");
        assertEquals(t1.getStatus(), taskFromResponse.getStatus(), "Статус задачи не совпадает");
        assertEquals(t1.getStartTime(), taskFromResponse.getStartTime(), "Время начала не совпадает");
        assertEquals(t1.getDuration(), taskFromResponse.getDuration(), "Длительность не совпадает");

    }


    @Test
    public void testGetTaskById_NotFound() throws IOException, InterruptedException {
        int nonExistentId = 9999;

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(TASKS_URL + "/" + nonExistentId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Ожидался код 404 при запросе несуществующей задачи");
    }

    @Test
    public void testUpdateTask() throws IOException, InterruptedException {
        Task t1 = new Task("task1", "d1", TaskStatus.NEW, start, duration);
        int taskId = manager.addTask(t1);

        Task t2 = new Task("updated T1", "updated D1", TaskStatus.DONE, start, duration);
        t2.setTaskID(taskId);
        String jsonTask = gson.toJson(t2);


        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TASKS_URL))
                .POST(HttpRequest.BodyPublishers.ofString(jsonTask))
                .header("Content-Type", "application/json")
                .build();
        //HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Некорректный код ответа");


        Task updatedTask = manager.getTaskById(taskId);

        assertEquals(t2.getName(), updatedTask.getName(), "Имя задачи не совпадает");
        assertEquals(t2.getDescription(), updatedTask.getDescription(), "Описание задачи не совпадает");
        assertEquals(t2.getStatus(), updatedTask.getStatus(), "Статус задачи не совпадает");
        assertEquals(t2.getStartTime(), updatedTask.getStartTime(), "Время начала не совпадает");
        assertEquals(t2.getDuration(), updatedTask.getDuration(), "Длительность не совпадает");

    }


    @Test
    public void testDeleteTaskById() throws IOException, InterruptedException {
        Task t1 = new Task("task1", "d1", TaskStatus.NEW, start, duration);
        int taskId = manager.addTask(t1);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TASKS_URL + "/" + taskId))
                .DELETE()
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Некорректный код ответа");

        Task deletedTask = manager.getTaskById(taskId);

        assertNull(deletedTask, "Задача не удалена");
    }


    @Test
    public void testDeleteAllTasks() throws IOException, InterruptedException {
        Task t1 = new Task("task1", "d1", TaskStatus.NEW, start, duration);
        Task t2 = new Task("task2", "d2", TaskStatus.NEW, start.plusMinutes(30), duration);
        manager.addTask(t1);
        manager.addTask(t2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TASKS_URL))
                .DELETE()
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Некорректный код ответа");

        List<Task> tasks = manager.getTasks();
        assertTrue(tasks.isEmpty(), "Список задач должен быть пустым");
    }



    @Test
    public void testAddTask_InvalidJson() throws IOException, InterruptedException {
        String invalidJson = "{ \"name\": \"Broken Task\", \"description\": \"Missing other fields\""; // нет закрывающей }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TASKS_URL))
                .POST(HttpRequest.BodyPublishers.ofString(invalidJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем, что сервер возвращает 400 (Bad Request)
        assertEquals(400, response.statusCode(), "Ожидался код 400 при ошибке разбора JSON");

        // Убеждаемся, что задача не добавлена
        List<Task> tasks = manager.getTasks();
        assertTrue(tasks.isEmpty(), "Некорректный JSON не должен создавать задачу");
    }


    @Test
    public void testAddTask_MissingFields() throws IOException, InterruptedException {
        String incompleteJson = """
        {
            "name": "Task without status",
            "description": "Missing important fields"
        }
        """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TASKS_URL))
                .POST(HttpRequest.BodyPublishers.ofString(incompleteJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем код ответа — 400 Bad Request (или другой, если у тебя иная логика)
        assertEquals(400, response.statusCode(), "Ожидался код 400 из-за отсутствующих полей");

        // Убедимся, что задача не была добавлена
        List<Task> tasks = manager.getTasks();
        assertTrue(tasks.isEmpty(), "Задача с неполными данными не должна добавляться");
    }


    @Test
    public void testTaskSerializationAndDeserialization() throws IOException, InterruptedException {
        Task originalTask = new Task("Serialize Me", "Testing Gson", TaskStatus.NEW, start, duration);
        originalTask.setTaskID(1); // Устанавливаем ID вручную (для сравнения)

        // Сериализация
        String json = gson.toJson(originalTask);

        // Десериализация
        Task deserializedTask = gson.fromJson(json, Task.class);

        // Сравниваем поля
        assertEquals(originalTask.getName(), deserializedTask.getName(), "Имя не совпадает");
        assertEquals(originalTask.getDescription(), deserializedTask.getDescription(), "Описание не совпадает");
        assertEquals(originalTask.getStatus(), deserializedTask.getStatus(), "Статус не совпадает");
        assertEquals(originalTask.getStartTime(), deserializedTask.getStartTime(), "StartTime не совпадает");
        assertEquals(originalTask.getDuration(), deserializedTask.getDuration(), "Duration не совпадает");
        assertEquals(originalTask.getTaskID() , deserializedTask.getTaskID(), "ID не совпадает");
    }



}