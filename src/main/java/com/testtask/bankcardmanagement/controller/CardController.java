package com.testtask.bankcardmanagement.controller;

import com.testtask.bankcardmanagement.model.dto.CardRequest;
import com.testtask.bankcardmanagement.model.dto.CardResponse;
import com.testtask.bankcardmanagement.model.enums.CardStatus;
import com.testtask.bankcardmanagement.service.card.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/")
public class CardController {
    private final CardService cardService;

    @PostMapping("/admin/cards")
    public ResponseEntity<CardResponse> createCard(@RequestBody @Valid CardRequest cardRequest) {
        CardResponse response = cardService.createCard(cardRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/admin/cards")
    public ResponseEntity<Page<CardResponse>> getAllCards(
            @RequestParam(defaultValue = "") CardStatus cardStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") List<String> sortList,
            @RequestParam(defaultValue = "ASC") String sortOrder
            )
    {
        return ResponseEntity.ok(
                cardService.getAllCards(cardStatus, page, size, sortList, sortOrder)
        );
    }

//    @PutMapping("/admin/cards/{cardId}/block")
//    public ResponseEntity<CardResponse> blockingCard(@PathVariable("cardId") String uuidCard) {
//
//    }

//    @PutMapping("/admin/cards/{cardId}/activate")
//    public ResponseEntity<CardResponse> activatingCard(@PathVariable("cardId") String uuidCard) {
//
//    }

//    @DeleteMapping("/admin/cards/{cardId}")
//    public ResponseEntity<String> deleteCard(@PathVariable("cardID") String uuidCard) {
//
//    }

//    @PostMapping("admin/cards/{cardId}/limits")
//    public ResponseEntity<CardResponse> setCardLimit(@PathVariable("cardId") String uuid, @RequestBody LimitType) {
//
//    }

//    @GetMapping("user/cards")
//    public ResponseEntity<CardResponse> getAllCards() {
//
//    }

}
