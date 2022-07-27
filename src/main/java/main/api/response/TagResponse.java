package main.api.response;

import lombok.Data;

@Data
public class TagResponse {
    private final String name;
    private final double weight;
}
