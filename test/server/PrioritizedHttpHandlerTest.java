package server;

import com.google.gson.Gson;

import com.google.gson.reflect.TypeToken;
import controllers.InMemoryTaskManager;
import controllers.Managers;
import controllers.TaskManager;
import model.Task;
import util.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class PrioritizedHttpHandlerTest {

    private TaskManager manager;
    private HttpTaskServer server;
    private final Gson gson = HttpTaskServer.getGson();
    private final HttpClient client = HttpClient.newHttpClient();
    private static final String BASE_URL = "http://localhost:8080/prioritized";
    LocalDateTime start;
    Duration duration;


    @BeforeEach
    void setUp() throws IOException {
        manager = Managers.getDefault();
        server = new HttpTaskServer(manager);
        server.start();
        start = LocalDateTime.of(2030, 1, 1, 10, 0);
        duration = Duration.ofMinutes(10);
    }


    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void testGetPrioritizedTasks_WhenEmpty() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Ожидался код 200 для пустого списка");

        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertNotNull(tasks, "Ответ не должен быть null");
        assertEquals(0, tasks.length, "Ожидался пустой список задач");
    }

    @Test
    void testGetPrioritizedTasks_WhenMultipleTasks() throws IOException, InterruptedException {
        Task t1 = new Task("T1", "D1", TaskStatus.NEW,
                start, duration);
        Task t2 = new Task("T2", "D2", TaskStatus.NEW,
                start.plusMinutes(60), duration);

        manager.addTask(t1);
        manager.addTask(t2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        class UserListTypeToken extends TypeToken<List<Task>> {
        }
        List<Task> history = gson.fromJson(response.body(), new UserListTypeToken().getType());


        assertNotNull(history);
        assertEquals(2, history.size(), "Ожидались 2 задачи");
    }


    @Test
    void testPrioritizedTasksOrder() throws IOException, InterruptedException {
        Task t1 = new Task("T1", "D1", TaskStatus.NEW,
                start.plusMinutes(30), duration);     // 2
        Task t2 = new Task("T2", "D2", TaskStatus.NEW,
                start.plusMinutes(10), duration);    // 1
        Task t3 = new Task("T3", "D3", TaskStatus.NEW,
                start.plusMinutes(60), duration);     // 3

        manager.addTask(t1);
        manager.addTask(t2);
        manager.addTask(t3);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        class UserListTypeToken extends TypeToken<List<Task>> {
        }
        List<Task> tasks = gson.fromJson(response.body(), new UserListTypeToken().getType());

        assertEquals(3, tasks.size());
        assertEquals("T2", tasks.get(0).getName(), "Первая задача должна быть T2");
        assertEquals("T1", tasks.get(1).getName(), "Вторая задача должна быть T1");
        assertEquals("T3", tasks.get(2).getName(), "Третья задача должна быть T3");
    }


}