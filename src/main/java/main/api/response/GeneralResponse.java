package main.api.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class GeneralResponse {
    private int count;
    private List<Map<String, Object>> posts;
}
