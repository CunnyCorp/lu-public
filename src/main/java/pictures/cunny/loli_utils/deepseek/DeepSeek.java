package pictures.cunny.loli_utils.deepseek;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeepSeek {
    private final Gson gson = new GsonBuilder().create();
    private final HttpClient client = HttpClient.newHttpClient();
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    public final String apiKey;

    private final Map<CompletionBody, CompletionResponse> responseCache = new ConcurrentHashMap<>();

    public DeepSeek(String apiKey) {
        this.apiKey = apiKey;
    }

    public void requestCompletion(CompletionBody body, ResponseFunction<CompletionResponse> function) {
        // Check if the response is already in the cache
        /*if (responseCache.containsKey(body)) {
            function.run(responseCache.get(body));

            return;
        }*/

        CompletableFuture.supplyAsync(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.deepseek.com/chat/completions"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body, CompletionBody.class)))
                    .build();
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println(response.body());
                return gson.fromJson(response.body(), CompletionResponse.class);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }, executorService).thenAccept(response -> {
            if (response != null) {
                responseCache.put(body, response);
                function.run(response);
            }
        });
    }

    @FunctionalInterface
    public interface ResponseFunction<T> {
        void run(T response);
    }
}
