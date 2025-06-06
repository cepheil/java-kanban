package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import controllers.Managers;
import model.Epic;
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

class EpicsHttpHandlerTest {

    private TaskManager manager;
    private HttpTaskServer taskServer;
    private final Gson gson = HttpTaskServer.getGson();
    private final HttpClient client = HttpClient.newHttpClient();
    private static final String EPICS_URL = "http://localhost:8080/epics";
//    LocalDateTime start;
//    Duration duration;

    @BeforeEach
    void setUp() throws IOException {
        manager = Managers.getDefault();
        taskServer = new HttpTaskServer(manager);
        taskServer.start();
//        start = LocalDateTime.of(2030, 1, 1, 10, 0);
//        duration = Duration.ofMinutes(10);
    }

    @AfterEach
    void tearDown() {
        taskServer.stop();
    }



    @Test
    void shouldAddEpicSuccessfully() throws IOException, InterruptedException {

        Epic epic = new Epic("E1", "E1_D");
        String json = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(EPICS_URL))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();
        //HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Некорректный код ответа");

        List<Epic> epics = manager.getEpics();
        assertEquals(1, epics.size(), "Некорректное количество задач");
        assertEquals("E1", epics.get(0).getName(), "Некорректное имя задачи");
        assertEquals("E1_D", epics.get(0).getDescription(), "Некорректное описание задачи");
    }


//////

    @Test
    public void testGetEpics() throws IOException, InterruptedException {
        Epic epic = new Epic("E1", "E1_D");
        manager.addEpic(epic);


        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(EPICS_URL);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Некорректный код ответа при GET /epics");


        class UserListTypeToken extends TypeToken<List<Epic>> {
        }
        List<Epic> epics = gson.fromJson(response.body(), new UserListTypeToken().getType());

        assertNotNull(epics, "Задачи не возвращаются");
        assertEquals(1, epics.size(), "Неверное количество задач");
        assertEquals("E1", epics.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void testGetAllEpics() throws IOException, InterruptedException {
        Epic e1 = new Epic("E1", "E1_D");
        Epic e2 = new Epic("E2", "E2_D");
        Epic e3 = new Epic("E3", "E3_D");
        manager.addEpic(e1);
        manager.addEpic(e2);
        manager.addEpic(e3);



        // Отправляем GET-запрос
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(EPICS_URL);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Некорректный код ответа при GET /epics");

        class UserListTypeToken extends TypeToken<List<Epic>> {
        }
        List<Epic> epics = gson.fromJson(response.body(), new UserListTypeToken().getType());

        assertNotNull(epics, "Задачи не возвращаются");
        assertEquals(3, epics.size(), "Неверное количество задач");
        assertEquals("E1", epics.get(0).getName(), "Некорректное имя задачи");
        assertEquals("E2", epics.get(1).getName(), "Некорректное имя задачи");
        assertEquals("E3", epics.get(2).getName(), "Некорректное имя задачи");
        assertEquals("E2_D", epics.get(1).getDescription(), "Некорректное описание задачи");

    }



    @Test
    public void testGetEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("E1", "E1_D");
        int taskId = manager.addEpic(epic);

        // Отправляем GET-запрос
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(EPICS_URL + "/" + taskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Некорректный код ответа");

        Epic epicFromResponse = gson.fromJson(response.body(), Epic.class); // получаем задачу из тела ответа.
        // Проверяем, что задача вернулась и её поля совпадают
        assertNotNull(epicFromResponse, "Задача не найдена в ответе");
        assertEquals(epic.getName(), epicFromResponse.getName(), "Имя задачи не совпадает");
        assertEquals(epic.getDescription(), epicFromResponse.getDescription(), "Описание задачи не совпадает");

    }

    @Test
    public void testGetEpicById_NotFound() throws IOException, InterruptedException {
        int nonExistentId = 9999;

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(EPICS_URL + "/" + nonExistentId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Ожидался код 404 при запросе несуществующей задачи");
    }



    @Test
    public void testUpdateEpic() throws IOException, InterruptedException {
        Epic e1 = new Epic("E1", "E1_D");
        int id = manager.addEpic(e1);

        Epic e2 = new Epic("updated E1", "updated E1_D");
        e2.setTaskID(id);
        String jsonEpic = gson.toJson(e2);


        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(EPICS_URL))
                .POST(HttpRequest.BodyPublishers.ofString(jsonEpic))
                .header("Content-Type", "application/json")
                .build();
        //HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Некорректный код ответа");


        Epic updatedEpic = manager.getEpicById(id);

        assertEquals(e2.getName(), updatedEpic.getName(), "Имя задачи не совпадает");
        assertEquals(e2.getDescription(), updatedEpic.getDescription(), "Описание задачи не совпадает");


    }



    @Test
    public void testDeleteTaskById() throws IOException, InterruptedException {
        Epic e1 = new Epic("E1", "E1_D");
        int id = manager.addEpic(e1);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(EPICS_URL + "/" + id))
                .DELETE()
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Некорректный код ответа");

        Epic deletedEpic = manager.getEpicById(id);

        assertNull(deletedEpic, "Задача не удалена");
    }




    @Test
    public void testDeleteAllEpics() throws IOException, InterruptedException {
        Epic e1 = new Epic("E1", "E1_D");
        Epic e2 = new Epic("E2", "E2_D");


        int id1 = manager.addEpic(e1);
        int id2 = manager.addEpic(e2);



        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(EPICS_URL))
                .DELETE()
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Некорректный код ответа");

        List<Epic> epicss = manager.getEpics();
        assertTrue(epicss.isEmpty(), "Список задач должен быть пустым");
    }



    @Test
    public void testAddEpic_InvalidJson() throws IOException, InterruptedException {
        String invalidJson = "{ \"name\": \"Broken Task\", \"description\": \"Missing other fields\""; // нет закрывающей }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(EPICS_URL))
                .POST(HttpRequest.BodyPublishers.ofString(invalidJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем, что сервер возвращает 400 (Bad Request)
        assertEquals(400, response.statusCode(), "Ожидался код 400 при ошибке разбора JSON");

        // Убеждаемся, что задача не добавлена
        List<Epic> epicss = manager.getEpics();
        assertTrue(epicss.isEmpty(), "Некорректный JSON не должен создавать задачу");
    }


    @Test
    public void testTaskSerializationAndDeserialization() throws IOException, InterruptedException {
        Epic e1 = new Epic("E1", "E1_D");
        e1.setTaskID(1); // Устанавливаем ID вручную (для сравнения)

        // Сериализация
        String json = gson.toJson(e1);

        // Десериализация
        Epic deserializedTask = gson.fromJson(json, Epic.class);

        // Сравниваем поля
        assertEquals(e1.getName(), deserializedTask.getName(), "Имя не совпадает");
        assertEquals(e1.getDescription(), deserializedTask.getDescription(), "Описание не совпадает");
        assertEquals(e1.getStatus(), deserializedTask.getStatus(), "Статус не совпадает");

    }




}