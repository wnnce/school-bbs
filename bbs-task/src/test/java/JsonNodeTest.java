import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

/**
 * @Author: lisang
 * @DateTime: 2023-10-25 14:28:26
 * @Description:
 */
public class JsonNodeTest {
    @Test
    public void testJsonNodeGet() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "{\"log_id\": 149319909347709, \"error_cod\": 0, \"error_msg\":\"configId error\"}";
        JsonNode jsonNode = objectMapper.readValue(json, JsonNode.class);
        System.out.println(jsonNode);
        System.out.println(jsonNode.path("error_code").isMissingNode());
    }
}
