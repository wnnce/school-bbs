import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @Author: lisang
 * @DateTime: 2023-10-25 14:28:26
 * @Description:
 */
public class JsonNodeTest {
    @Test
    public void testJsonNodeGet() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        System.out.println(System.currentTimeMillis());
        Future<String> text = executorService.submit(() -> {
            Thread.sleep(2);
            return "hello world";
        });
        System.out.println(System.currentTimeMillis());
        String s = text.get();
        System.out.println(System.currentTimeMillis());
        System.out.println(s);
    }
}
