package org.example.warehouse

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class UserMapper(
    private val passwordEncoder:PasswordEncoder
){
    fun toEntity(userRequest: UserCreateRequest,wareHouse: WareHouse, uniqueNumber:String ): User{
        userRequest.run {
            return User(
                firstName = firstname,
                lastName = lastname,
                phone = phone,
                uniqueNumber =uniqueNumber,
                password = passwordEncoder.encode(password),
                role = role,
                wareHouse = wareHouse,
                status = Status.ACTIVE
            )
        }
    }


}
@Component
class WarehouseMapper() {
    fun toEntity(wareHouseRequest: WareHouseRequest): WareHouse{
        wareHouseRequest.run {
            return WareHouse(
                name = name ,
                status = Status.ACTIVE
            )
        }
    }


    fun toDto(wareHouse: WareHouse) : WareHouseResponse{
        wareHouse.run {
            return WareHouseResponse(
                id = id,
                name  = name,
                status = status
            )
        }

    }

    fun toListDto(entity: WareHouse): WareHouseListResponse =
        WareHouseListResponse(
            id = entity.id!!,
            name = entity.name,
            status = entity.status
        )

    fun updateEntity(entity: WareHouse, request: WareHouseUpdateRequest): WareHouse {
        request.name?.let { entity.name = it }
        request.status?.let { entity.status = it }
        return entity
    }
}

@Component
class CategoryMapper(){
    fun toEntity(categoryRequest: CategoryRequest, category: Category?): Category{
        categoryRequest.run {
            return Category(
                name = name ,
                parent = category,
            )
        }
    }

    fun toDto(category: Category) : CategoryResponse{
        category.run {
            return CategoryResponse(
                id = id,
                name = name ,
                parentId = parent?.id,
                status = status
            )
        }
    }

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

}

@Component
class MeasurementUnitMapper(){
    fun toEntity(measurementUnitRequest: MeasurementUnitRequest): MeasurementUnit{
        measurementUnitRequest.run {
            return MeasurementUnit(
                name = name,
            )
        }
    }


    fun toDto(measurementUnit: MeasurementUnit): MeasurementUnitResponse{
        measurementUnit.run {
            return MeasurementUnitResponse(
                id = id,
                name  =name ,
                status = status
            )
        }
    }
}


@Component
class SupplierMapper(){
    fun toEntity(supplierRequest: SupplierRequest): Supplier{
        supplierRequest.run {
            return Supplier(
                name = name,
                phone = phone
            )
        }
    }


    fun toDto(supplier: Supplier): SupplierResponse{
        supplier.run {
            return SupplierResponse(
                id = id,
                name  =name ,
                phone = phone,
                status = status
            )
        }
    }
}

@Component
class ProductMapper(){
    fun toEntity(productRequest: ProductRequest, category: Category, measurementUnit: MeasurementUnit, uniqueCode:String): Product{
        productRequest.run {
            return Product(
                name = name,
                measurementUnit = measurementUnit,
                category = category,
                uniqueCode = uniqueCode
            )
        }
    }

    fun toDto(product: Product): ProductResponse{
        product.run {
            return ProductResponse(
                id = id,
                name = name,
                categoryId = category.id,
                measurementUnitId = measurementUnit.id,
                uniqueCode = uniqueCode,
            )
        }
    }


    fun toListDto(product: Product): ProductListResponse {
        return ProductListResponse(
            id = product.id!!,
            name = product.name,
            uniqueCode = product.uniqueCode,
            categoryId = product.category.id,
            measurementUnitId = product.measurementUnit.id
        )
    }
}

@Component
class TransactionMapper {

    fun toEntity(
        dto: TransactionCreateRequestDto,
        warehouse: WareHouse,
        supplier: Supplier?,
        uniqueNumber: String
    ): Transaction {
        dto.run {
            return Transaction(
                type = type,
                date = date,
                wareHouse = warehouse,
                supplier = supplier,
                invoiceNumber = invoiceNumber,
                uniqueNumber = uniqueNumber
            )
        }
    }

    fun toResponseDto(
        transaction: Transaction,
        items: List<TransactionItemResponseDto>,
        totalAmount: BigDecimal
    ): TransactionResponseDto {

        transaction.run {
            return TransactionResponseDto(
                id = id!!,
                type = type,
                date = date,
                warehouseId = wareHouse.id!!,
                warehouseName = wareHouse.name,
                supplierId = supplier?.id,
                supplierName = supplier?.name,
                invoiceNumber = invoiceNumber,
                uniqueNumber = uniqueNumber,
                status = status,
                items = items,
                totalAmount = totalAmount
            )
        }
    }
}


@Component
class TransactionItemMapper {


    fun toEntity(
        dto: TransactionItemRequest,
        transaction: Transaction,
        product: Product
    ): TransactionItem {
        dto.run {
            return TransactionItem(
                transaction = transaction,
                product = product,
                quantity = quantity,
                price = price,
                expireDate = expireDate,
                sellingPrice = sellingPrice
            )
        }
    }


    fun toResponseDto(item: TransactionItem): TransactionItemResponseDto {
        item.run {
            return TransactionItemResponseDto(
                productId = product.id!!,
                productName = product.name,
                quantity = quantity,
                price = price,
                amount = quantity.multiply(price),
                expireDate = expireDate,
                sellingPrice = sellingPrice
            )
        }
    }
}

@Component
class TransactionSaleItemMapper {

    fun toEntity(
        dto: TransactionSaleItemRequestDto,
        transaction: Transaction,
        product: Product
    ): TransactionItem {
        return TransactionItem(
            transaction = transaction,
            product = product,
            quantity = dto.quantity,
            price = dto.price,
            expireDate = null,
            sellingPrice = null
        )
    }

    fun toResponseDto(item: TransactionItem): TransactionItemResponseDto {
        return TransactionItemResponseDto(
            productId = item.product.id!!,
            productName = item.product.name,
            quantity = item.quantity,
            price = item.price,
            amount = item.quantity.multiply(item.price),
            expireDate = item.expireDate,
            sellingPrice = item.sellingPrice
        )
    }
}

@Component
class TransactionSaleMapper {

    fun toEntity(
        dto: TransactionSaleCreateRequestDto,
        warehouse: WareHouse,
        uniqueNumber: String
    ): Transaction {
        return Transaction(
            type = TransactionType.OUT,
            date = dto.date,
            wareHouse = warehouse,
            supplier = null,
            invoiceNumber = dto.invoiceNumber,
            uniqueNumber = uniqueNumber
        )
    }

    fun toResponseDto(
        transaction: Transaction,
        items: List<TransactionItemResponseDto>,
        totalAmount: BigDecimal
    ): TransactionResponseDto {
        return TransactionResponseDto(
            id = transaction.id!!,
            type = transaction.type,
            date = transaction.date,
            warehouseId = transaction.wareHouse.id!!,
            warehouseName = transaction.wareHouse.name,
            supplierId = null,
            supplierName = null,
            invoiceNumber = transaction.invoiceNumber,
            uniqueNumber = transaction.uniqueNumber,
            status = transaction.status,
            items = items,
            totalAmount = totalAmount
        )
    }
}







