package org.example.warehouse

import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/auth")
class AuthController(
    private val userService: UserService
) {
    @PostMapping("/register")
    fun create(@RequestBody request: UserCreateRequest) = userService.create(request)


    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): JwtResponse {
        return userService.loginIn(req)
    }
}

@RestController
@RequestMapping("/warehouse")
class WareHouseController(
    private val service: WareHouseService
) {

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun create(@RequestBody request: WareHouseRequest) =
        service.create(request)

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: WareHouseUpdateRequest
    ) = service.update(id, request)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) =
        service.delete(id)

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long): WareHouseResponse =
        service.getOne(id)

    @GetMapping
    fun getAll(): List<WareHouseListResponse> =
        service.getAll()
}



@RestController
@RequestMapping("/category")
class CategoryController(
    private val service: CategoryService
) {
    @PostMapping
    fun create(@RequestBody request: CategoryRequest) = service.create(request)

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: CategoryUpdateRequest) =
        service.update(id, request)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = service.delete(id)

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long): CategoryResponse = service.getOne(id)

    @GetMapping
    fun getAll(): List<CategoryListResponse> = service.getAll()
}


@RestController
@RequestMapping("/measurementUnit")
class MeasurementUnitController(
    private val measurementUnitService: MeasurementUnitService
){
    @PostMapping()
    fun create(@RequestBody measurementUnitRequest: MeasurementUnitRequest) =  measurementUnitService.create(measurementUnitRequest)

    @GetMapping("/{id}")
    fun getOne(@PathVariable id:Long): MeasurementUnitResponse = measurementUnitService.getOne(id)
}


@RestController
@RequestMapping("/supplier")
class SupplierController(
    private val supplierService: SupplierService
){
    @PostMapping()
    fun create(@RequestBody supplierRequest: SupplierRequest) =  supplierService.create(supplierRequest)

    @GetMapping("/{id}")
    fun getOne(@PathVariable id:Long): SupplierResponse = supplierService.getOne(id)
}

@RestController
@RequestMapping("/products")
class ProductController(
    private val productService: ProductService
){
    @PostMapping
    fun create(@RequestBody request: ProductRequest) = productService.create(request)

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: ProductUpdateRequest) =
        productService.update(id, request)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = productService.delete(id)

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long): ProductResponse = productService.getOne(id)

    @GetMapping
    fun getAll(): List<ProductListResponse> = productService.getAll()
}



@RestController
@RequestMapping("/transactions")
class TransactionController(
    private val transactionService: TransactionService
) {

    @PostMapping("/income")
    fun createIncome(
        @RequestBody request: TransactionCreateRequestDto
    ) = transactionService.createIncome(request)

    @PostMapping("/sale")
    fun createSale(
        @Valid @RequestBody request: TransactionSaleCreateRequestDto
    ) = transactionService.createSale(request)


    @PostMapping("/cancel")
    fun cancelTransaction(
        @Valid @RequestBody request: TransactionCancelRequestDto
    ) = transactionService.cancelTransaction(request)
}


@RestController
@RequestMapping("/statistics")
class StatisticsController(
    private val statisticsService: StatisticsService
) {

    @PostMapping("/daily-income")
    fun getDailyIncome(
        @RequestBody request: DailyIncomeRequestDto
    ): List<DailyIncomeDto> = statisticsService.getDailyIncome(request)

    @PostMapping("/daily-top-sale")
    fun getDailyTopSale(
        @RequestBody request: DailyTopSaleRequestDto
    ): List<DailyTopSaleDto> = statisticsService.getDailyTopSale(request)

    @PostMapping("/expired-products")
    fun getExpiredProducts(
        @RequestBody request: ExpiredProductRequestDto
    ): List<ExpiredProductDto> = statisticsService.getExpiredProducts(request)
}










