package com.ironvault.payments.adapter.in.web;

import com.ironvault.payments.adapter.in.dto.PaymentRequest;
import com.ironvault.payments.adapter.in.dto.PaymentResponse;
import com.ironvault.payments.adapter.in.mapper.PaymentResponseMapper;
import com.ironvault.payments.adapter.in.dto.UpdatePaymentStatusRequest;
import com.ironvault.payments.domain.port.in.payment.UpdatePaymentStatusCommand;
import com.ironvault.payments.domain.port.in.payment.UpdatePaymentStatusUseCase;
import com.ironvault.payments.domain.enums.PaymentStatus;
import com.ironvault.payments.domain.model.Payment;
import com.ironvault.payments.domain.port.in.payment.CreatePaymentCommand;
import com.ironvault.payments.domain.port.in.payment.CreatePaymentUseCase;
import com.ironvault.payments.domain.port.in.payment.GetAllPaymentsUseCase;
import com.ironvault.payments.domain.port.in.payment.GetPaymentByIdUseCase;
import com.ironvault.payments.domain.query.PageQuery;
import com.ironvault.payments.domain.query.PageResult;
import com.ironvault.payments.domain.query.PaymentFilter;
import com.ironvault.payments.utils.IdempotencyKeyValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Operações de pagamento")
public class PaymentController {

    private static final int MAX_PAGE_SIZE = 100;

    private final CreatePaymentUseCase createPaymentUseCase;
    private final GetAllPaymentsUseCase getAllPaymentsUseCase;
    private final GetPaymentByIdUseCase getPaymentByIdUseCase;
    private final UpdatePaymentStatusUseCase updatePaymentStatusUseCase;

    private final PaymentResponseMapper mapper;

    public PaymentController(CreatePaymentUseCase createPaymentUseCase,
                             GetAllPaymentsUseCase getAllPaymentsUseCase,
                             GetPaymentByIdUseCase getPaymentByIdUseCase,
                             UpdatePaymentStatusUseCase updatePaymentStatusUseCase,
                             PaymentResponseMapper mapper) {
        this.createPaymentUseCase = createPaymentUseCase;
        this.getAllPaymentsUseCase = getAllPaymentsUseCase;
        this.getPaymentByIdUseCase = getPaymentByIdUseCase;
        this.updatePaymentStatusUseCase = updatePaymentStatusUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Criar pagamento",
            description = "Cria um novo pagamento PIX via Mercado Pago"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pagamento criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "409", description = "Idempotency key já utilizada")
    })
    public ResponseEntity<PaymentResponse> create(
            @RequestHeader(value = "Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody PaymentRequest request) {

        // Validate idempotencyKey
        IdempotencyKeyValidator.validateIdempotencyKey(idempotencyKey);

        Payment payment = createPaymentUseCase.create(
                new CreatePaymentCommand(request.getAmount(), request.getCurrency(), request.getPaymentMethod(),
                        request.getDescription(), request.getPayerEmail()),
                idempotencyKey
        );

        return ResponseEntity.status(201).body(mapper.toResponse(payment));
    }

    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Atualizar status do pagamento",
            description = "Atualiza o status de um pagamento existente"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Status inválido"),
            @ApiResponse(responseCode = "404", description = "Pagamento não encontrado")
    })
    public ResponseEntity<PaymentResponse> updateStatus(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdatePaymentStatusRequest request) {

        PaymentStatus targetStatus;
        try {
            targetStatus = PaymentStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid status: " + request.getStatus());
        }

        Payment updated = updatePaymentStatusUseCase.updateStatus(
                new UpdatePaymentStatusCommand(
                        id,
                        targetStatus,
                        request.getFailureReason()
                )
        );

        return ResponseEntity.ok(mapper.toResponse(updated));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pagamento por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pagamento encontrado"),
            @ApiResponse(responseCode = "404", description = "Pagamento não encontrado")
    })
    public ResponseEntity<PaymentResponse> getById(@PathVariable("id")UUID id) {

        Payment payment = getPaymentByIdUseCase.getById(id);
        return ResponseEntity.ok(mapper.toResponse(payment));
    }

    @GetMapping
    @Operation(
            summary = "Listar pagamentos",
            description = "Retorna pagamentos com filtros opcionais e paginação"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos")
    })
    public ResponseEntity<PageResult<PaymentResponse>> getAll(
            @Parameter(description = "Filtrar por status (ex: PROCESSING, APPROVED, FAILED)")
            @RequestParam(required = false) String status,

            @Parameter(description = "Filtrar por moeda (ex: BRL, USD)")
            @RequestParam(required = false) String currency,

            @Parameter(description = "Valor mínimo")
            @RequestParam(required = false) BigDecimal minAmount,

            @Parameter(description = "Valor máximo")
            @RequestParam(required = false) BigDecimal maxAmount,

            @Parameter(description = "Número da página (começa em 0)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Tamanho da página (máximo " + MAX_PAGE_SIZE + ")")
            @RequestParam(defaultValue = "10") int size) {

        if (size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size must not exceed " + MAX_PAGE_SIZE);
        }

        PaymentStatus paymentStatus = null;

        if (status != null) {
            try {
                paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Invalid status: " + status);
            }
        }

        if (currency != null && currency.length() != 3) {
            throw new IllegalArgumentException(
                    "Currency must be a 3-letter code (e.g. BRL, USD)"
            );
        }

        if (minAmount != null && maxAmount != null && minAmount.compareTo(maxAmount) > 0) {
            throw new IllegalArgumentException(
                    "minAmount cannot be greater than maxAmount"
            );
        }

        PaymentFilter filter = new PaymentFilter(
                paymentStatus,
                currency,
                minAmount,
                maxAmount
        );

        PageQuery pageQuery = new PageQuery(page, size);

        PageResult<PaymentResponse> response =
                getAllPaymentsUseCase
                        .getAllWithFilters(filter, pageQuery)
                        .map(mapper::toResponse);

        return ResponseEntity.ok(response);
    }

}
