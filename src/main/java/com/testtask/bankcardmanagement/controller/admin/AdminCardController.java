package com.testtask.bankcardmanagement.controller.admin;

import com.testtask.bankcardmanagement.controller.SortableFieldService;
import com.testtask.bankcardmanagement.model.dto.card.CardParamFilter;
import com.testtask.bankcardmanagement.model.dto.card.CardResponse;
import com.testtask.bankcardmanagement.model.dto.card.CreateCardRequest;
import com.testtask.bankcardmanagement.service.user.AdminCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/card")
@RequiredArgsConstructor
public class AdminCardController {
    private final AdminCardService adminCardService;
    private final SortableFieldService sortableFieldService;

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

    @GetMapping("/all")
    public ResponseEntity<PagedModel<EntityModel<CardResponse>>> getAllCards(
            @Valid CardParamFilter filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") List<String> sortList,
            @RequestParam(defaultValue = "ASC") String sortOrder,
            PagedResourcesAssembler<CardResponse> assembler
    )
    {
        sortableFieldService.validAdminCardFields(sortList);
        List<Sort.Order> sorted = sortableFieldService.createSortOrder(sortList, sortOrder);

        Pageable pageable = PageRequest.of(page, size, Sort.by(sorted));
        Page<CardResponse> cardPage = adminCardService.getAllCards(filter, pageable);

        return ResponseEntity.ok(assembler.toModel(cardPage));
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
