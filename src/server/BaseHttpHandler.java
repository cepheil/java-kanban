package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import controllers.TaskManager;
import util.DurationAdapter;
import util.LocalDateTimeAdapter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

public abstract class BaseHttpHandler implements HttpHandler {
    protected final TaskManager taskManager;
    protected static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .create();


    public BaseHttpHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }


    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            switch (method) {
                case "GET":
                    processGet(exchange, path);
                    break;
                case "POST":
                    processPost(exchange, path);
                    break;
                case "DELETE":
                    processDelete(exchange, path);
                    break;
                default:
                    sendMethodNotAllowed(exchange, path);
                    break;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            sendResponse(exchange, 500, "Internal Server Error");
        } finally {
            exchange.close();
        }
    }

    protected void processGet(HttpExchange exchange, String path) throws IOException {
        sendMethodNotAllowed(exchange, "GET");
    }

    protected void processPost(HttpExchange exchange, String path) throws IOException {
        sendMethodNotAllowed(exchange, "POST");
    }

    protected void processDelete(HttpExchange exchange, String path) throws IOException {
        sendMethodNotAllowed(exchange, "DELETE");
    }

    protected void sendMethodNotAllowed(HttpExchange exchange, String method) throws IOException {
        sendResponse(exchange, 405, method + " method not allowed for this endpoint.");
    }

    protected String readRequestBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    protected void sendText(HttpExchange h, String text) throws IOException {
        sendResponse(h, 200, text);
    }

    protected void sendCreated(HttpExchange h, String text) throws IOException {
        sendResponse(h, 201, text);
    }

    protected void sendNotFound(HttpExchange h, String text) throws IOException {
        sendResponse(h, 404, text);
    }

    protected void sendHasInteractions(HttpExchange h, String text) throws IOException {
        sendResponse(h, 406, text);
    }

    protected void sendResponse(HttpExchange h, int statusCode, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(statusCode, resp.length);
        try (OutputStream os = h.getResponseBody()) {
            os.write(resp);
        }
    }

    protected int parsePathId(String path) {
        String[] splitStrings = path.split("/");
        if (splitStrings.length < 3) {
            return -1;
        }
        try {
            return Integer.parseInt(splitStrings[2]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
