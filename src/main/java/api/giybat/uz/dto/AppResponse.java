package api.giybat.uz.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppResponse<T> {
    private T data;
    private String message;

    public AppResponse(String message) {
        this.message = message;
    }

    public AppResponse(T data, String message) {
        this.data = data;
        this.message = message;
    }
}
