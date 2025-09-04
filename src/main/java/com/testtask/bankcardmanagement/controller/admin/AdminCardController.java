package com.testtask.bankcardmanagement.controller.admin;

import com.testtask.bankcardmanagement.model.dto.card.CardResponse;
import com.testtask.bankcardmanagement.model.dto.card.CreateCardRequest;
import com.testtask.bankcardmanagement.service.user.AdminCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/card")
@RequiredArgsConstructor
public class AdminCardController {
    private final AdminCardService adminCardService;

    @PostMapping("/new")
    public ResponseEntity<CardResponse> createCard(@RequestBody @Valid CreateCardRequest createCardRequest) {
        CardResponse response = adminCardService.createCard(createCardRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/remove/{cardId}")
    public ResponseEntity<String> deleteCard(@PathVariable("cardId") Long cardId) {
        adminCardService.deleteCard(cardId);
        return ResponseEntity.ok("The card was successfully deleted");
    }

    @PutMapping("/block/{cardId}")
    public ResponseEntity<CardResponse> blockingCard(@PathVariable("cardId") Long cardId) {
        return null;
    }

    @PutMapping("/activate/{cardId}")
    public ResponseEntity<CardResponse> activatingCard(@PathVariable("cardId") Long cardId) {
        return null;
    }
}
