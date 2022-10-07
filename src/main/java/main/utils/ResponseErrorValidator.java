package main.utils;

import main.api.response.ResultResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;

@Component
public class ResponseErrorValidator {

    public ResponseEntity<Object> mapValidationService(BindingResult result) {
        if(result.hasErrors()) {
            Map<String, String> errorMap = new HashMap<>();

            for(FieldError error : result.getFieldErrors()) {
                errorMap.put(error.getField(), error.getDefaultMessage());
            }
            ResultResponse resultResponse = new ResultResponse();
            resultResponse.setResult(false);
            resultResponse.setErrors(errorMap);
            return ResponseEntity.badRequest().body(resultResponse);
        }
        return null;
    }
}
