package server;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import controllers.Managers;
import model.Epic;
import model.Subtask;
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

class SubtasksHttpHandlerTest {


    private TaskManager manager;
    private HttpTaskServer taskServer;
    private final Gson gson = BaseHttpHandler.gson;
    private final HttpClient client = HttpClient.newHttpClient();
    private static final String SUBTASKS_URL = "http://localhost:8080/subtasks";
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
    void shouldAddSubtaskSuccessfully() throws IOException, InterruptedException {
        Epic e1 = new Epic("E1", "D_E1");
        int epicId = manager.addEpic(e1);

        Subtask subtask = new Subtask("Test Subtask", "Subtask description",
                TaskStatus.NEW, start, duration, epicId);
        String json = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SUBTASKS_URL))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();
        //HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Некорректный код ответа");

        List<Subtask> subtasks = manager.getSubtasks();
        assertEquals(1, subtasks.size(), "Некорректное количество задач");
        assertEquals("Test Subtask", subtasks.get(0).getName(), "Некорректное имя задачи");
        assertEquals("Subtask description", subtasks.get(0).getDescription(), "Некорректное описание задачи");
        assertEquals(epicId, subtasks.get(0).getEpicID(), "Некорректный ID эпика");

    }


    @Test
    public void testGetSubtask() throws IOException, InterruptedException {
        Epic e1 = new Epic("E1", "D_E1");
        int epicId = manager.addEpic(e1);

        Subtask s1 = new Subtask("S1", "S_D1", TaskStatus.NEW, start, duration, epicId);

        manager.addSubtask(s1);

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(SUBTASKS_URL);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Некорректный код ответа при GET /tasks");


        class UserListTypeToken extends TypeToken<List<Subtask>> {
        }
        List<Subtask> subtasks = gson.fromJson(response.body(), new UserListTypeToken().getType());

        assertNotNull(subtasks, "Задачи не возвращаются");
        assertEquals(1, subtasks.size(), "Неверное количество задач");
        assertEquals("S1", subtasks.get(0).getName(), "Некорректное имя задачи");
    }



    @Test
    public void testGetAllSubtasks() throws IOException, InterruptedException {
        Epic e1 = new Epic("E1", "D_E1");
        int epicId = manager.addEpic(e1);


        Subtask s1 = new Subtask("S1", "S_D1", TaskStatus.NEW, start, duration, epicId);
        Subtask s2 = new Subtask("S2", "S_D2", TaskStatus.IN_PROGRESS, start.plusDays(1), duration, epicId);
        Subtask s3 = new Subtask("S3", "S_D3", TaskStatus.DONE, start.plusDays(2), duration, epicId);

        manager.addSubtask(s1);
        manager.addSubtask(s2);
        manager.addSubtask(s3);

        // Отправляем GET-запрос
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(SUBTASKS_URL);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Некорректный код ответа при GET /tasks");

        class UserListTypeToken extends TypeToken<List<Subtask>> {
        }
        List<Subtask> subtasks  = gson.fromJson(response.body(), new UserListTypeToken().getType());

        assertNotNull(subtasks, "Задачи не возвращаются");
        assertEquals(3, subtasks.size(), "Неверное количество задач");
        assertEquals("S1", subtasks.get(0).getName(), "Некорректное имя задачи");
        assertEquals("S2", subtasks.get(1).getName(), "Некорректное имя задачи");
        assertEquals("S3", subtasks.get(2).getName(), "Некорректное имя задачи");
        assertEquals("S_D2", subtasks.get(1).getDescription(), "Некорректное описание задачи");
        assertEquals(TaskStatus.DONE, subtasks.get(2).getStatus(), "Некорректный статус задачи");
    }



    @Test
    public void testGetSubtaskById() throws IOException, InterruptedException {
        Epic e1 = new Epic("E1", "D_E1");
        int epicId = manager.addEpic(e1);

        Subtask s1 = new Subtask("S1", "S_D1", TaskStatus.NEW, start, duration, epicId);
        int taskId =  manager.addSubtask(s1);

        // Отправляем GET-запрос
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(SUBTASKS_URL + "/" + taskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Некорректный код ответа");

        Subtask subtaskFromResponse = gson.fromJson(response.body(), Subtask.class); // получаем задачу из тела ответа.
        // Проверяем, что задача вернулась и её поля совпадают
        assertNotNull(subtaskFromResponse, "Задача не найдена в ответе");
        assertEquals(s1.getName(), subtaskFromResponse.getName(), "Имя задачи не совпадает");
        assertEquals(s1.getDescription(), subtaskFromResponse.getDescription(), "Описание задачи не совпадает");
        assertEquals(s1.getStatus(), subtaskFromResponse.getStatus(), "Статус задачи не совпадает");
        assertEquals(s1.getStartTime(), subtaskFromResponse.getStartTime(), "Время начала не совпадает");
        assertEquals(s1.getDuration(), subtaskFromResponse.getDuration(), "Длительность не совпадает");

    }


    @Test
    public void testGetSubtaskById_NotFound() throws IOException, InterruptedException {
        int nonExistentId = 9999;

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(SUBTASKS_URL + "/" + nonExistentId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Ожидался код 404 при запросе несуществующей задачи");
    }

///

    @Test
    public void testUpdateSubtask() throws IOException, InterruptedException {
        Epic e1 = new Epic("E1", "D_E1");
        int epicId = manager.addEpic(e1);

        Subtask s1 = new Subtask("S1", "S_D1", TaskStatus.NEW, start, duration, epicId);
        int taskId =  manager.addSubtask(s1);

        Subtask s2 = new Subtask("updated S1", "updated S_D1", TaskStatus.DONE, start, duration, epicId);
        s2.setTaskID(taskId);

        String jsonS2 = gson.toJson(s2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SUBTASKS_URL))
                .POST(HttpRequest.BodyPublishers.ofString(jsonS2))
                .header("Content-Type", "application/json")
                .build();
        //HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Некорректный код ответа");


        Subtask updatedSubtask = manager.getSubtaskById(taskId);

        assertEquals(s2.getName(), updatedSubtask.getName(), "Имя задачи не совпадает");
        assertEquals(s2.getDescription(), updatedSubtask.getDescription(), "Описание задачи не совпадает");
        assertEquals(s2.getStatus(), updatedSubtask.getStatus(), "Статус задачи не совпадает");
        assertEquals(s2.getStartTime(), updatedSubtask.getStartTime(), "Время начала не совпадает");
        assertEquals(s2.getDuration(), updatedSubtask.getDuration(), "Длительность не совпадает");
        assertEquals(s2.getEpicID(), updatedSubtask.getEpicID(), "EpicId не совпадает");
    }


    @Test
    public void testDeleteSubtaskById() throws IOException, InterruptedException {
        Epic e1 = new Epic("E1", "D_E1");
        int epicId = manager.addEpic(e1);

        Subtask s1 = new Subtask("S1", "S_D1", TaskStatus.NEW, start, duration, epicId);
        int taskId =  manager.addSubtask(s1);


        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SUBTASKS_URL + "/" + taskId))
                .DELETE()
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Некорректный код ответа");

        Subtask deletedTask = manager.getSubtaskById(taskId);

        assertNull(deletedTask, "Задача не удалена");
    }


    @Test
    public void testDeleteAllSubtasks() throws IOException, InterruptedException {
        Epic e1 = new Epic("E1", "D_E1");
        int epicId = manager.addEpic(e1);


        Subtask s1 = new Subtask("S1", "S_D1", TaskStatus.NEW, start, duration, epicId);
        Subtask s2 = new Subtask("S2", "S_D2", TaskStatus.IN_PROGRESS, start.plusDays(1), duration, epicId);
        Subtask s3 = new Subtask("S3", "S_D3", TaskStatus.DONE, start.plusDays(2), duration, epicId);

        manager.addSubtask(s1);
        manager.addSubtask(s2);
        manager.addSubtask(s3);



        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SUBTASKS_URL))
                .DELETE()
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Некорректный код ответа");

        List<Subtask> subtasks  = manager.getSubtasks();
        assertTrue(subtasks.isEmpty(), "Список задач должен быть пустым");
    }



    ///
    @Test
    public void testAddSubtask_InvalidJson() throws IOException, InterruptedException {
        String invalidJson = "{ \"name\": \"Broken Task\", \"description\": \"Missing other fields\""; // нет закрывающей }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SUBTASKS_URL))
                .POST(HttpRequest.BodyPublishers.ofString(invalidJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем, что сервер возвращает 400 (Bad Request)
        assertEquals(400, response.statusCode(), "Ожидался код 400 при ошибке разбора JSON");

        // Убеждаемся, что задача не добавлена
        List<Subtask> subtasks  = manager.getSubtasks();
        assertTrue(subtasks.isEmpty(), "Некорректный JSON не должен создавать задачу");
    }



    @Test
    public void testAddSubtask_MissingFields() throws IOException, InterruptedException {
        String incompleteJson = """
        {
            "name": "Subtask without status",
            "description": "Missing important fields"
        }
        """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SUBTASKS_URL))
                .POST(HttpRequest.BodyPublishers.ofString(incompleteJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем код ответа — 400 Bad Request (или другой, если у тебя иная логика)
        assertEquals(400, response.statusCode(), "Ожидался код 400 из-за отсутствующих полей");

        // Убедимся, что задача не была добавлена
        List<Subtask> subtasks  = manager.getSubtasks();
        assertTrue(subtasks.isEmpty(), "Задача с неполными данными не должна добавляться");
    }


    @Test
    public void testSubtaskSerializationAndDeserialization() throws IOException, InterruptedException {

        Subtask originalSubtask = new Subtask("S1", "S_D1", TaskStatus.NEW, start, duration, 10);
        originalSubtask.setTaskID(1);


        // Сериализация
        String json = gson.toJson(originalSubtask);

        // Десериализация
        Subtask deserializedSubtask = gson.fromJson(json, Subtask.class);

        // Сравниваем поля
        assertEquals(originalSubtask.getName(), deserializedSubtask.getName(), "Имя не совпадает");
        assertEquals(originalSubtask.getDescription(), deserializedSubtask.getDescription(), "Описание не совпадает");
        assertEquals(originalSubtask.getStatus(), deserializedSubtask.getStatus(), "Статус не совпадает");
        assertEquals(originalSubtask.getStartTime(), deserializedSubtask.getStartTime(), "StartTime не совпадает");
        assertEquals(originalSubtask.getDuration(), deserializedSubtask.getDuration(), "Duration не совпадает");
        assertEquals(originalSubtask.getTaskID() , deserializedSubtask.getTaskID(), "ID не совпадает");
        assertEquals(originalSubtask.getEpicID() , deserializedSubtask.getEpicID(), "EpicID не совпадает");

    }

}