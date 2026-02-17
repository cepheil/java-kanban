package server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import controllers.Managers;
import controllers.TaskManager;
import model.Task;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.HttpTaskServer;
import util.TaskStatus;
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

class HistoryHttpHandlerTest {
    private static final String HISTORY_URL = "http://localhost:8080/history";
    private HttpClient client;
    private TaskManager manager;
    private HttpTaskServer server;
    private final LocalDateTime start = LocalDateTime.now();
    private final Duration duration = Duration.ofMinutes(30);
    private final Gson gson = BaseHttpHandler.gson;

    @BeforeEach
    void setUp() throws IOException {
        manager = Managers.getDefault();
        server  = new HttpTaskServer(manager);
        client = HttpClient.newHttpClient();
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }


    @Test
    void testGetHistory_WhenEmpty() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HISTORY_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа должен быть 200");

        Type taskListType = new TypeToken<List<Task>>() {}.getType();
        //List<Task> history = server.getGson().fromJson(response.body(), taskListType);
        List<Task> history = gson.fromJson(response.body(), taskListType);

        assertNotNull(history, "История не должна быть null");
        assertTrue(history.isEmpty(), "История должна быть пустой");
    }

    @Test
    void testGetHistory_WhenTasksWereAccessed() throws IOException, InterruptedException {
        Task t1 = new Task("Task1", "desc1", TaskStatus.NEW, start, duration);
        Task t2 = new Task("Task2", "desc2", TaskStatus.NEW, start.plusHours(1), duration);
        int id1 = manager.addTask(t1);
        int id2 = manager.addTask(t2);

        // Имитируем доступ к задачам
        manager.getTaskById(id1);
        manager.getTaskById(id2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HISTORY_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа должен быть 200");

        Type taskListType = new TypeToken<List<Task>>() {}.getType();
        List<Task> history = gson.fromJson(response.body(), taskListType);

        assertNotNull(history, "История не должна быть null");
        assertEquals(2, history.size(), "В истории должно быть 2 задачи");
        assertEquals(id1, history.get(0).getTaskID(), "Первая задача должна быть Task1");
        assertEquals(id2, history.get(1).getTaskID(), "Вторая задача должна быть Task2");
    }
}