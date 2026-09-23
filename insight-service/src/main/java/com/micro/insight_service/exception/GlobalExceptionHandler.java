package com.micro.insight_service.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.micro.insight_service.dto.InsightDto;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<InsightDto> handleAll(Exception e) {
        return ResponseEntity.ok(InsightDto.builder()
                .tips(" حدث خطأ غير متوقع — يرجى المحاولة لاحقًا. "
                        + "Fallback (ar/en): An unexpected error occurred. Please retry in a moment.")
                .energyConsuming(0.0)
                .build());
    }
}
