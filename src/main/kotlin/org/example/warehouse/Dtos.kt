package org.example.warehouse


import org.jetbrains.annotations.NotNull
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.math.BigDecimal
import java.time.LocalDate

data class BaseMessage(
    val code: Long? = null,
    val message: String? = null
)

data class UserCreateRequest(
    val firstname: String,
    val lastname: String,
    val phone:String,
    val role: UserRole,
    val warehouseId:Long,
    val password: String
)


data class UserUpdateRequest(
    val phone: String?,
    val firstname: String?,
    val lastname: String?,
    val role: UserRole?,
    val password :String?,
    val wareHouseId:Long? =null,
    val status: Status?
)


data class UserResponse(
    val id: Long?,
    val firstName: String?,
    val role: UserRole?,
    val status: Status
)


data class UserFullResponse(
    val id: Long?,
    val firstName: String,
    val lastName: String,
    val phone:String,
    val role: UserRole,
    val wareHouseId:Long?,
    val wareHouseName:String,
    val status: Status
)


data class UserDetailsResponse(
    val id: Long,
    val myUsername: String,
    val firstname: String?,
    val lastname: String?,
    val role: UserRole,
    val myPassword: String,
    val wareHouse: WareHouse
) : UserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return mutableListOf(SimpleGrantedAuthority(role.name))
    }

    override fun getPassword(): String {
        return myPassword
    }

    override fun getUsername(): String {
        return myUsername
    }
    override fun isAccountNonExpired() = true
    override fun isAccountNonLocked() = true
    override fun isCredentialsNonExpired() = true
    override fun isEnabled() = true


}

data class WareHouseRequest(
    val name:String
)

data class WareHouseResponse(
    val id: Long?,
    val name:String,
    val status: Status
)

data class WareHouseUpdateRequest(
    val name: String?,
    val status: Status?
)

data class WareHouseListResponse(
    val id: Long,
    val name: String,
    val status: Status
)


data class CategoryRequest(
    val name:String,
    val parentId:Long?,
    )

data class CategoryResponse(
    val id:Long?,
    val name:String,
    val parentId:Long?,
    val status: Status
)

data class CategoryUpdateRequest(
    val name: String?,
    val parentId: Long? = null,
    val status: Status?
)

data class CategoryListResponse(
    val id: Long,
    val name: String,
    val parentId: Long?,
    val status: Status
)

fun toListDto(entity: Category): CategoryListResponse =
    CategoryListResponse(
        id = entity.id!!,
        name = entity.name,
        parentId = entity.parent?.id,
        status = entity.status
    )

fun updateEntity(entity: Category, request: CategoryUpdateRequest, parent: Category?): Category {
    request.name?.let { entity.name = it }
    request.parentId?.let { entity.parent = parent }
    request.status?.let { entity.status = it }
    return entity
}

data class MeasurementUnitRequest(
    val name:String
)

data class MeasurementUnitResponse(
    val id:Long?,
    val name :String,
    val status: Status
)

data class MeasurementUnitUpdate(
    val name:String?,
    val status: Status?
)



data class SupplierRequest(
    val name:String,
    val phone:String
)

data class SupplierResponse(
    val id:Long?,
    val name:String,
    val phone:String,
    val status: Status
)

data class SupplierUpdateRequest(
    val name:String?,
    val phone:String?,
    val status: Status?
)


data class ProductRequest(
    val name:String,
    val measurementUnitId:Long,
    val categoryId:Long
)

data class ProductResponse(
    val id:Long?,
    val name:String,
    val uniqueCode:String,
    val measurementUnitId:Long?,
    val categoryId:Long?
)

data class ProductUpdateRequest(
    val name: String?,
    val categoryId: Long? = null,
    val measurementUnitId: Long? = null
)

data class ProductListResponse(
    val id: Long,
    val name: String,
    val uniqueCode: String,
    val categoryId: Long?,
    val measurementUnitId: Long?
)


data class TransactionItemRequest(
    val productId: Long,
    val quantity: BigDecimal,
    val price: BigDecimal,
    val expireDate: LocalDate? = null,
    val sellingPrice: BigDecimal? = null
)

data class TransactionCreateRequestDto(
    val type: TransactionType,
    val date: LocalDate,
    val warehouseId: Long,
    val supplierId: Long?,
    val invoiceNumber: String,
    val items: List<TransactionItemRequest>
)

data class TransactionItemResponseDto(
    val productId: Long,
    val productName: String,
    val quantity: BigDecimal,
    val price: BigDecimal,
    val amount: BigDecimal,
    val expireDate: LocalDate?,
    val sellingPrice: BigDecimal?
)

data class TransactionResponseDto(
    val id: Long,
    val type: TransactionType,
    val date: LocalDate,
    val warehouseId: Long,
    val warehouseName: String,
    val supplierId: Long?,
    val supplierName: String?,
    val invoiceNumber: String,
    val uniqueNumber: String,
    val status: TransactionStatus,
    val items: List<TransactionItemResponseDto>,
    val totalAmount: BigDecimal
)

data class TransactionSaleItemRequestDto(
    val productId: Long,
    val quantity: BigDecimal,
    val price: BigDecimal
)

data class TransactionSaleCreateRequestDto(
    val date: LocalDate,
    val warehouseId: Long,
    val invoiceNumber: String,
    val items: List<TransactionSaleItemRequestDto>
)
data class TransactionCancelRequestDto(
    val transactionId: Long
)

data class DailyIncomeRequestDto(
    val date: LocalDate,
    val warehouseId: Long
)

data class DailyIncomeDto(
    val productId: Long,
    val productName: String,
    val totalQuantity: BigDecimal,
    val totalAmount: BigDecimal
)

data class DailyTopSaleRequestDto(
    val date: LocalDate,
    val warehouseId: Long
)

data class DailyTopSaleDto(
    val productId: Long,
    val productName: String,
    val totalQuantity: BigDecimal
)

data class ExpiredProductRequestDto(
    val warehouseId: Long
)

data class ExpiredProductDto(
    val productId: Long,
    val productName: String,
    val expiredQuantity: BigDecimal,
    val expireDate: LocalDate
)



data class LoginRequest(val phone: String, val password: String)
data class JwtResponse(val token: String)