package org.example.warehouse

import org.example.warehouse.security.JwtService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

interface UserService {
    fun create(body:UserCreateRequest)
    fun loginIn(request: LoginRequest) : JwtResponse
    fun getAllUsers(): List<UserResponse>
    fun update(id:Long , userUpdateRequest: UserUpdateRequest)
    fun getOneUser(id:Long): UserFullResponse
    fun delete(id:Long)
}

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val mapper: UserMapper,
    private  val wareHouseRepository: WareHouseRepository,
    private val uniqueNumberGenerator: UniqueNumberGenerator,
    private val passwordEncoder: BCryptPasswordEncoder,
    private val jwtService: JwtService

): UserService{
    @Transactional
    override fun create(body: UserCreateRequest) {

    body.run {
        userRepository.findByPhone(phone)?.let {
            throw PhoneNumberAlreadyExistsException()
        }?:run {

            val wareHouse = wareHouseRepository.findByIdAndDeletedFalseAndStatus(warehouseId, Status.ACTIVE)
                ?:throw WareHouseNotFoundException()
            val savedUser = userRepository.save(mapper.toEntity(body,wareHouse,uniqueNumberGenerator.generate(8)))
            println("Sawed user => $savedUser")
        }
    }

    }

    override fun loginIn(request: LoginRequest): JwtResponse {
       val user =  userRepository.findByPhone(request.phone)
              ?: throw  UserNotFoundException()

     if(!passwordEncoder.matches(request.password, user.password)){
         throw PasswordIsIncorrect()
     }
        val token  = jwtService.generateToken(user.phone, user.role.name)
        return JwtResponse(token)
    }

    override fun getAllUsers(): List<UserResponse> {
        var findAllNotDeleted = userRepository.findAllNotDeleted()
        var usersResponse :List<UserResponse> = findAllNotDeleted.map { mapper.toDto(it) }
        return usersResponse
    }
    @Transactional
    override fun update(id:Long, userUpdateRequest: UserUpdateRequest) {
       var user =  userRepository.findByIdAndDeletedFalse(id)
            ?:throw UserNotFoundException()

        userUpdateRequest.run {
            phone?.let{newPhone->
                var exists = userRepository.findByPhone(newPhone)

                if (exists!=null && exists.id!=user.id){
                    throw PhoneNumberAlreadyExistsException()
                }
                  user.phone = newPhone

            }
            firstname?.let { user.firstName = it }
            lastname?.let { user.lastName = it }
            role?.let { user.role = it }
            status?.let { user.status = it }
            password?.let {
                user.password = passwordEncoder.encode(it)
            }

            if (wareHouseId != null && wareHouseId > 0) {
                val warehouse = wareHouseRepository.findByIdAndDeletedFalse(wareHouseId)
                    ?: throw WareHouseNotFoundException()
                user.wareHouse = warehouse
            }

        }
        userRepository.save(user)

    }

    override fun getOneUser(id: Long) : UserFullResponse{
       val user =  userRepository.findByIdAndDeletedFalse(id)
             ?:throw UserNotFoundException()
        return mapper.toDtoFull(user)
    }
    @Transactional
    override fun delete(id: Long) {
        userRepository.findByIdAndDeletedFalse(id)
            ?:throw UserNotFoundException()
        userRepository.trash(id)
    }
}


interface WareHouseService{
    fun create(request:WareHouseRequest)
    fun getOne(warehouseId:Long) : WareHouseResponse
    fun update(id: Long, request: WareHouseUpdateRequest)
    fun delete(id: Long)
    fun getAll(): List<WareHouseListResponse>

}

@Service
class WareHouseServiceImpl(
    private val repository: WareHouseRepository,
    private val mapper: WarehouseMapper
) : WareHouseService {
    @Transactional
    override fun create(request: WareHouseRequest) {
        repository.findByNameAndDeletedFalseAndStatus(request.name)?.let {
            throw WareHouseNameAlreadyExists()
        }
        repository.save(mapper.toEntity(request))
    }
    @Transactional
    override fun update(id: Long, request: WareHouseUpdateRequest) {
        val entity = repository.findByIdAndDeletedFalse(id)
            ?: throw WareHouseNotFoundException()

        repository.save(mapper.updateEntity(entity, request))
    }
    @Transactional
    override fun delete(id: Long) {
        repository.trash(id) ?: throw WareHouseNotFoundException()
    }

    override fun getOne(id: Long): WareHouseResponse {
        return repository.findByIdAndDeletedFalse(id)
            ?.let { mapper.toDto(it) }
            ?: throw WareHouseNotFoundException()
    }

    override fun getAll(): List<WareHouseListResponse> {
        return repository.findAllNotDeleted()
            .map { mapper.toListDto(it) }
    }
}


interface CategoryService {
    fun create(request: CategoryRequest)
    fun update(id: Long, request: CategoryUpdateRequest)
    fun delete(id: Long)
    fun getOne(id: Long): CategoryResponse
    fun getAll(): List<CategoryListResponse>
}

@Service
class CategoryServiceImpl(
    private val repository: CategoryRepository,
    private val mapper: CategoryMapper
) : CategoryService {
    @Transactional
    override fun create(request: CategoryRequest) {
        repository.findByNameAndDeletedFalseAndStatus(request.name)?.let { throw CategoryNameAlreadyExists() }
        val parent = request.parentId?.let { repository.findByIdAndDeletedFalse(it) }
        repository.save(mapper.toEntity(request, parent))
    }
    @Transactional
    override fun update(id: Long, request: CategoryUpdateRequest) {
        val entity = repository.findByIdAndDeletedFalse(id) ?: throw CategoryNotFound()
        val parent = request.parentId?.let { repository.findByIdAndDeletedFalse(it) }
        repository.save(mapper.updateEntity(entity, request, parent))
    }
    @Transactional
    override fun delete(id: Long) {
        repository.trash(id) ?: throw CategoryNotFound()
    }

    override fun getOne(id: Long): CategoryResponse =
        repository.findByIdAndDeletedFalse(id)?.let { mapper.toDto(it) } ?: throw CategoryNotFound()

    override fun getAll(): List<CategoryListResponse> =
        repository.findAllNotDeleted().map { mapper.toListDto(it) }
}



interface MeasurementUnitService{
    fun create(request:MeasurementUnitRequest)
    fun getOne(id:Long):MeasurementUnitResponse
    fun update(id:Long, measurementUnitUpdate: MeasurementUnitUpdate)
    fun getAll(): List<MeasurementUnitResponse>
    fun delete(id: Long)

}

@Service
class MeasurementUnitServiceImpl(
    private val measurementUnitRepository : MeasurementUnitRepository,
    private val mapper: MeasurementUnitMapper
): MeasurementUnitService {
    @Transactional
    override fun create(request: MeasurementUnitRequest) {
       measurementUnitRepository.findByNameAndDeletedFalseAndStatus(request.name)?.let {
           throw MeasurementUnitNameAlreadyExists()
       }
        measurementUnitRepository.save(mapper.toEntity(request))
    }

    override fun getOne(id: Long): MeasurementUnitResponse {
        measurementUnitRepository.findByIdAndDeletedFalse(id)?.let {
            return mapper.toDto(it)
         }?:throw MeasurementUnitNotFound()
    }

    override fun update(id: Long, measurementUnitUpdate: MeasurementUnitUpdate) {
       var measurementUnit =  measurementUnitRepository.findByIdAndDeletedFalse(id)
            ?:throw MeasurementUnitNotFound()

        measurementUnitUpdate.run {
            name?.let {
                measurementUnitRepository.findByNameAndDeletedFalse(it)?.let {
                    if (it.id!=measurementUnit.id){
                        throw MeasurementUnitNameAlreadyExists()
                    }
                }
                measurementUnit.name = it
            }
            status?.let { measurementUnit.status =it}
        }
        measurementUnitRepository.save(measurementUnit)
    }

    override fun getAll(): List<MeasurementUnitResponse> {
        return measurementUnitRepository.findAllNotDeleted().map { mapper.toDto(it) }

    }

    override fun delete(id: Long) {
        measurementUnitRepository.findByIdAndDeletedFalse(id)
            ?:throw MeasurementUnitNotFound()
        measurementUnitRepository.trash(id)
    }
}



interface SupplierService{
    fun create(request:SupplierRequest)
    fun getOne(id:Long):SupplierResponse
    fun update(id:Long, supplierUpdateRequest: SupplierUpdateRequest)
    fun getAll(): List<SupplierResponse>
    fun delete(id:Long)

}

@Service
class SupplierServiceImpl(
    private val supplierRepository: SupplierRepository ,
    private val mapper: SupplierMapper
): SupplierService {

    @Transactional
    override fun create(request: SupplierRequest) {
        supplierRepository.findByPhoneAndDeletedFalseAndStatus(request.phone)?.let {
            throw SupplierPhoneAlreadyExists()
        }
        supplierRepository.save(mapper.toEntity(request))
    }

    override fun getOne(id: Long): SupplierResponse {
        supplierRepository.findByIdAndDeletedFalse(id)?.let {
            return mapper.toDto(it)
        }?:throw SupplierNotFound()
    }

    @Transactional
    override fun update(id: Long, supplierUpdateRequest: SupplierUpdateRequest) {
        var supplier: Supplier = supplierRepository.findByIdAndDeletedFalse(id)
            ?:throw SupplierNotFound()

        supplierUpdateRequest.run {
            name?.let {supplier.name = it}
            phone?.let { supplier.phone = it }
            status?.let {supplier.status = it }
        }

        supplierRepository.save(supplier)
    }

    override fun getAll(): List<SupplierResponse> {
        var findAllNotDeleted = supplierRepository.findAllNotDeleted()
       return  findAllNotDeleted.map { mapper.toDto(it) }
    }

    @Transactional
    override fun delete(id: Long) {
        supplierRepository.findByIdAndDeletedFalse(id)
            ?:throw SupplierNotFound()
    }
}


interface ProductService{
    fun create(request: ProductRequest)
    fun update(id: Long, request: ProductUpdateRequest)
    fun delete(id: Long)
    fun getOne(id: Long): ProductResponse
    fun getAll(): List<ProductListResponse>
}

@Service
class ProductServiceImpl(
    private val productRepository: ProductRepository,
    private val mapper: ProductMapper,
    private val measurementUnitRepository: MeasurementUnitRepository,
    private val categoryRepository: CategoryRepository,
    private val uniqueNumberGenerator: UniqueNumberGenerator
) : ProductService {

    @Transactional
    override fun create(request: ProductRequest) {
        val category = categoryRepository.findByIdAndDeletedFalse(request.categoryId)
            ?: throw CategoryNotFound()

        val measurementUnit = measurementUnitRepository.findByIdAndDeletedFalse(request.measurementUnitId)
            ?: throw MeasurementUnitNotFound()

        productRepository.save(mapper.toEntity(
            request,
            category,
            measurementUnit,
            uniqueNumberGenerator.generate(9)
        ))
    }

    @Transactional
    override fun update(id: Long, request: ProductUpdateRequest) {
        val entity = productRepository.findByIdAndDeletedFalse(id)
            ?: throw ProductNotFound()

        request.name?.let { entity.name = it }

        request.categoryId?.let {
            if (it>0){
                entity.category = categoryRepository.findByIdAndDeletedFalse(it) ?: throw CategoryNotFound()
            }
        }

        request.measurementUnitId?.let {
            if(it>0){
                entity.measurementUnit = measurementUnitRepository.findByIdAndDeletedFalse(it)
                    ?: throw MeasurementUnitNotFound()
            }

        }

        productRepository.save(entity)
    }
    @Transactional
    override fun delete(id: Long) {
        productRepository.trash(id) ?: throw ProductNotFound()
    }

    override fun getOne(id: Long): ProductResponse =
        productRepository.findByIdAndDeletedFalse(id)?.let { mapper.toDto(it) } ?: throw ProductNotFound()

    override fun getAll(): List<ProductListResponse> =
        productRepository.findAllNotDeleted().map { mapper.toListDto(it) }

}


interface TransactionService {
    fun createIncome(request: TransactionCreateRequestDto)
    fun createSale(request: TransactionSaleCreateRequestDto)
    fun cancelTransaction(request: TransactionCancelRequestDto)

}

@Service
class TransactionServiceImpl(
    private val transactionRepository: TransactionRepository,
    private val transactionItemRepository: TransactionItemRepository,
    private val warehouseRepository: WareHouseRepository,
    private val supplierRepository: SupplierRepository,
    private val productRepository: ProductRepository,
    private val stockRepository: StockRepository,
    private val transactionMapper: TransactionMapper,
    private val transactionItemMapper: TransactionItemMapper,
    private val uniqueNumberGenerator: UniqueNumberGenerator,
    private val transactionSaleItemMapper: TransactionSaleItemMapper,
    private val transactionSaleMapper: TransactionSaleMapper
) : TransactionService {

    @Transactional
    override fun createIncome(request: TransactionCreateRequestDto) {

        val warehouse = warehouseRepository.findByIdAndDeletedFalseAndStatus(request.warehouseId)
            ?: throw WareHouseNotFoundException()


        val supplier = request.supplierId?.let {
            supplierRepository.findByIdAndDeletedFalseAndStatus(it)
                ?: throw SupplierNotFound()
        }

        val transaction = transactionMapper.toEntity(
            dto = request,
            warehouse = warehouse,
            supplier = supplier,
            uniqueNumber = uniqueNumberGenerator.generate()
        )

        transactionRepository.save(transaction)


        val productIds = request.items.map { it.productId }
        val products = productRepository.findAllById(productIds)
            .associateBy { it.id!! }


        request.items.forEach { itemDto ->

            val product = products[itemDto.productId]
                ?: throw ProductNotFound()

            val transactionItem = transactionItemMapper.toEntity(
                dto = itemDto,
                transaction = transaction,
                product = product
            )

            transactionItemRepository.save(transactionItem)

            val stock = stockRepository.findByWarehouseAndProduct(warehouse, product)

            if (stock == null) {
                stockRepository.save(
                    Stock(
                        warehouse = warehouse,
                        product = product,
                        quantity = itemDto.quantity
                    )
                )
            } else {
                stock.quantity = stock.quantity.add(itemDto.quantity)
                stockRepository.save(stock)
            }
        }
    }

    @Transactional
    override fun createSale(request: TransactionSaleCreateRequestDto) {

        val warehouse = warehouseRepository.findByIdAndDeletedFalseAndStatus(request.warehouseId)
            ?: throw WareHouseNotFoundException()

        val transaction = transactionSaleMapper.toEntity(
            dto = request,
            warehouse = warehouse,
            uniqueNumber = uniqueNumberGenerator.generate()
        )
        transactionRepository.save(transaction)

        val productIds = request.items.map { it.productId }
        val products = productRepository.findAllById(productIds)
            .associateBy { it.id!! }

        val itemsResponse = mutableListOf<TransactionItemResponseDto>()
        var totalAmount = BigDecimal.ZERO

        request.items.forEach { itemDto ->
            val product = products[itemDto.productId]
                ?: throw ProductNotFound()

            val stock = stockRepository.findByWarehouseAndProduct(warehouse, product)
                ?: throw StockNotFoundException(product.name)

            if (stock.quantity < itemDto.quantity) {
                throw InsufficientStockException(product.name)
            }

            stock.quantity = stock.quantity.subtract(itemDto.quantity)
            stockRepository.save(stock)

            val transactionItem = transactionSaleItemMapper.toEntity(
                dto = itemDto,
                transaction = transaction,
                product = product
            )

            transactionItemRepository.save(transactionItem)

            val itemResponse = transactionSaleItemMapper.toResponseDto(transactionItem)
            itemsResponse.add(itemResponse)


            totalAmount = totalAmount.add(itemResponse.amount)
        }

    }

    @Transactional
    override fun cancelTransaction(request: TransactionCancelRequestDto) {
        val transaction = transactionRepository.findById(request.transactionId)
            .orElseThrow { TransactionNotFoundException("${request.transactionId}") }
        if (transaction.status == TransactionStatus.CANCELED) {
            throw RuntimeException("Transaction already canceled")
        }

        val items = transactionItemRepository.findAllByTransaction(transaction)

        when (transaction.type) {
            TransactionType.IN -> {
                items.forEach { item ->
                    val stock = stockRepository.findByWarehouseAndProduct(transaction.wareHouse, item.product)
                        ?: throw StockNotFoundException(item.product.name)

                    if (stock.quantity < item.quantity) {
                        throw InsufficientStockException(item.product.name)
                    }

                    stock.quantity = stock.quantity.subtract(item.quantity)
                    stockRepository.save(stock)
                }
            }

            TransactionType.OUT -> {
                items.forEach { item ->
                    val stock = stockRepository.findByWarehouseAndProduct(transaction.wareHouse, item.product)

                    if (stock == null) {
                        stockRepository.save(
                            Stock(
                                warehouse = transaction.wareHouse,
                                product = item.product,
                                quantity = item.quantity
                            )
                        )
                    } else {
                        stock.quantity = stock.quantity.add(item.quantity)
                        stockRepository.save(stock)
                    }
                }
            }
        }

        transaction.status = TransactionStatus.CANCELED
        transactionRepository.save(transaction)
    }

}


interface StatisticsService {
    fun getDailyIncome(request: DailyIncomeRequestDto): List<DailyIncomeDto>
    fun getDailyTopSale(request: DailyTopSaleRequestDto): List<DailyTopSaleDto>
    fun getExpiredProducts(request: ExpiredProductRequestDto): List<ExpiredProductDto>
}

@Service
class StatisticsServiceImpl(
    private val transactionItemRepository: TransactionItemRepository,
    private val warehouseRepository: WareHouseRepository
) : StatisticsService
{

    override fun getDailyIncome(request: DailyIncomeRequestDto): List<DailyIncomeDto> {
        warehouseRepository.findByIdAndDeletedFalse(request.warehouseId)
            ?: throw WareHouseNotFoundException()
        return transactionItemRepository.findDailyIncome(request.warehouseId, request.date)
    }

    override fun getDailyTopSale(request: DailyTopSaleRequestDto): List<DailyTopSaleDto> {
        warehouseRepository.findByIdAndDeletedFalse(request.warehouseId)
            ?: throw WareHouseNotFoundException()
        return transactionItemRepository.findDailyTopSale(request.warehouseId, request.date)
    }

    override fun getExpiredProducts(request: ExpiredProductRequestDto): List<ExpiredProductDto> {
        warehouseRepository.findByIdAndDeletedFalse(request.warehouseId)
            ?: throw WareHouseNotFoundException()
        return transactionItemRepository.findExpiredProducts(request.warehouseId)
    }
}




@Service
class CustomUserDetailsService(
    private val repository: UserRepository
) : UserDetailsService {
    override fun loadUserByUsername(phone: String): UserDetails {
        return repository.findByPhone(phone)?.let {
            UserDetailsResponse(
                id = it.id!!,
                myUsername = it.phone,
                lastname = it.lastName,
                firstname = it.firstName,
                role = it.role,
                myPassword = it.password,
                wareHouse = it.wareHouse
            )
        } ?: throw UserNotFoundException()
    }
}

@Service
class UniqueNumberGenerator {

    fun generate(length: Int = 12): String {
        val digits = "0123456789"
        return (1..length)
            .map { digits.random() }
            .joinToString("")
    }
}
