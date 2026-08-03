package com.ironvault.payments.adapter.in.web;

import com.ironvault.payments.adapter.in.dto.TransactionResponse;
import com.ironvault.payments.domain.enums.TransactionStatus;
import com.ironvault.payments.domain.enums.TransactionType;
import com.ironvault.payments.domain.model.Transaction;
import com.ironvault.payments.domain.port.in.payment.GetAllTransactionsUseCase;
import com.ironvault.payments.domain.port.in.payment.GetTransactionsByPaymentUseCase;
import com.ironvault.payments.domain.query.PageQuery;
import com.ironvault.payments.domain.query.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@Tag(name = "Transactions", description = "Operações de transações")
public class TransactionController {

    private final GetTransactionsByPaymentUseCase getTransactionsByPaymentUseCase;
    private final GetAllTransactionsUseCase getAllTransactionsUseCase;

    public TransactionController(GetTransactionsByPaymentUseCase getTransactionsByPaymentUseCase,
                                 GetAllTransactionsUseCase getAllTransactionsUseCase) {
        this.getTransactionsByPaymentUseCase = getTransactionsByPaymentUseCase;
        this.getAllTransactionsUseCase = getAllTransactionsUseCase;
    }

    @GetMapping("/payments/{id}/transactions")
    @Operation(summary = "Listar transações de um pagamento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transações retornadas com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pagamento não encontrado")
    })
    public ResponseEntity<List<TransactionResponse>> getByPaymentId(
            @Parameter(description = "ID do pagamento", required = true)
            @PathVariable("id") UUID id,
            HttpServletRequest httpRequest) {

        String merchantIdAttr = (String) httpRequest.getAttribute("merchantId");
        UUID requesterMerchantId = merchantIdAttr != null ? UUID.fromString(merchantIdAttr) : null;
        boolean isAdmin = httpRequest.isUserInRole("ADMIN");

        List<TransactionResponse> response = getTransactionsByPaymentUseCase
                .getByPaymentId(id, requesterMerchantId, isAdmin)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/transactions")
    @Operation(summary = "Histórico de transações", description = "Retorna todas as transações com filtros opcionais")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico retornado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos")
    })
    public ResponseEntity<PageResult<TransactionResponse>> getAll(
            @Parameter(description = "Filtrar por tipo (CHARGE, REFUND, CHARGEBACK)")
            @RequestParam(required = false) String type,

            @Parameter(description = "Filtrar por status (PENDING, CAPTURED, FAILED...)")
            @RequestParam(required = false) String status,

            @Parameter(description = "Número da página (começa em 0)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Tamanho da página")
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {

        TransactionType transactionType = type != null
                ? TransactionType.valueOf(type.toUpperCase()) : null;

        TransactionStatus transactionStatus = status != null
                ? TransactionStatus.valueOf(status.toUpperCase()) : null;

        String merchantIdAttr = (String) httpRequest.getAttribute("merchantId");
        UUID requesterMerchantId = merchantIdAttr != null ? UUID.fromString(merchantIdAttr) : null;
        boolean isAdmin = httpRequest.isUserInRole("ADMIN");
        UUID merchantIdFilter = isAdmin ? null : requesterMerchantId;

        PageResult<TransactionResponse> response = getAllTransactionsUseCase
                .getAll(merchantIdFilter, transactionType, transactionStatus, new PageQuery(page, size))
                .map(this::toResponse);

        return ResponseEntity.ok(response);
    }

    private TransactionResponse toResponse(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getPaymentId(),
                t.getExternalId(),
                t.getType() != null ? t.getType().name() : null,
                t.getStatus() != null ? t.getStatus().name() : null,
                t.getAmount(),
                t.getCurrency(),
                t.getGatewayCode(),
                t.getGatewayMessage(),
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }
}
