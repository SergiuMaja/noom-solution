package com.noom.interview.fullstack.sleep.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class GlobalExceptionHandler {

    data class ErrorResponse(
        val error: String,
        val details: List<String>? = null
    )

    @ExceptionHandler(SleepLogNotFoundException::class)
    fun handleNotFound(ex: SleepLogNotFoundException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(ex.message ?: "Not found"))

    @ExceptionHandler(DuplicateSleepLogException::class)
    fun handleDuplicate(ex: DuplicateSleepLogException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse(ex.message ?: "Duplicate entry"))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val details = ex.bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.badRequest()
            .body(ErrorResponse("Validation failed", details))
    }

    @ExceptionHandler(MissingRequestHeaderException::class)
    fun handleMissingHeader(ex: MissingRequestHeaderException): ResponseEntity<ErrorResponse> =
        ResponseEntity.badRequest()
            .body(ErrorResponse("Missing required header: ${ex.headerName}"))

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleBadRequest(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> =
        ResponseEntity.badRequest()
            .body(ErrorResponse("Invalid request body"))

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> =
        ResponseEntity.badRequest()
            .body(ErrorResponse("Invalid parameter: ${ex.name}"))
}
