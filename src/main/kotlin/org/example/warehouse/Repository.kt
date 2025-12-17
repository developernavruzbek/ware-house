package org.example.warehouse

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.findByIdOrNull
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@NoRepositoryBean
interface BaseRepository<T : BaseEntity> : JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
    fun findByIdAndDeletedFalse(id: Long): T?
    fun trash(id: Long): T?
    fun trashList(ids: List<Long>): List<T?>
    fun findAllNotDeleted(): List<T>
    fun findAllNotDeletedForPageable(pageable: Pageable): Page<T>
    fun saveAndRefresh(t: T): T
}

class BaseRepositoryImpl<T : BaseEntity>(
    entityInformation: JpaEntityInformation<T, Long>,
    private val entityManager: EntityManager
) : SimpleJpaRepository<T, Long>(entityInformation, entityManager), BaseRepository<T> {

    val isNotDeletedSpecification = Specification<T> { root, _, cb -> cb.equal(root.get<Boolean>("deleted"), false) }

    override fun findByIdAndDeletedFalse(id: Long) = findByIdOrNull(id)?.run { if (deleted) null else this }

    @Transactional
    override fun trash(id: Long): T? = findByIdOrNull(id)?.run {
        deleted = true
        save(this)
    }

    override fun findAllNotDeleted(): List<T> = findAll(isNotDeletedSpecification)
    override fun findAllNotDeletedForPageable(pageable: Pageable): Page<T> =
        findAll(isNotDeletedSpecification, pageable)

    override fun trashList(ids: List<Long>): List<T?> = ids.map { trash(it) }

    @Transactional
    override fun saveAndRefresh(t: T): T {
        return save(t).apply { entityManager.refresh(this) }
    }
}
@Repository
interface UserRepository: BaseRepository<User>{
    fun findByPhone(phone: String): User?

}
@Repository
interface WareHouseRepository: BaseRepository<WareHouse>{

    fun findByIdAndStatus(
        id: Long,
        status: Status
    ): WareHouse?

    fun findByName(name: String): WareHouse?
}
@Repository
interface CategoryRepository: BaseRepository<Category>{
    fun findByName(name: String): Category?
    fun existsByName(name: String): Boolean
}
@Repository
interface MeasurementUnitRepository: BaseRepository<MeasurementUnit>{
    fun findByName  (name: String): MeasurementUnit?
}

@Repository
interface SupplierRepository: BaseRepository<Supplier>{
    fun findByName  (name: String): Supplier?
    fun findByPhone(phone: String): Supplier?
}

@Repository
interface ProductRepository: BaseRepository<Product>{
    fun findAllByIdIn(ids: List<Long>): List<Product>

}

@Repository
interface TransactionRepository: BaseRepository<Transaction>{

}

interface TransactionItemRepository: BaseRepository<TransactionItem>{
    fun findAllByTransaction(transaction: Transaction): List<TransactionItem>


    @Query("""
    SELECT new org.example.warehouse.DailyIncomeDto(
        p.id, p.name, SUM(ti.quantity), SUM(ti.quantity * ti.price)
    )
    FROM TransactionItem ti
    JOIN ti.transaction t
    JOIN ti.product p
    WHERE t.type = :type
      AND t.wareHouse.id = :warehouseId
      AND t.date = :date
      AND t.status = :status
    GROUP BY p.id, p.name
""")
    fun findDailyIncome(
        @Param("warehouseId") warehouseId: Long,
        @Param("date") date: LocalDate,
        @Param("type") type: TransactionType = TransactionType.IN,
        @Param("status") status: TransactionStatus = TransactionStatus.COMPLETED
    ): List<DailyIncomeDto>


    @Query("""
    SELECT new org.example.warehouse.DailyTopSaleDto(
        p.id, p.name, SUM(ti.quantity)
    )
    FROM TransactionItem ti
    JOIN ti.transaction t
    JOIN ti.product p
    WHERE t.type = :type
      AND t.wareHouse.id = :warehouseId
      AND t.date = :date
      AND t.status = :status
    GROUP BY p.id, p.name
    ORDER BY SUM(ti.quantity) DESC
""")
    fun findDailyTopSale(
        @Param("warehouseId") warehouseId: Long,
        @Param("date") date: LocalDate,
        @Param("type") type: TransactionType = TransactionType.OUT,
        @Param("status") status: TransactionStatus = TransactionStatus.COMPLETED
    ): List<DailyTopSaleDto>


    @Query("""
        SELECT new org.example.warehouse.ExpiredProductDto(
            p.id, p.name, SUM(ti.quantity), ti.expireDate
        )
        FROM TransactionItem ti
        JOIN ti.transaction t
        JOIN ti.product p
        WHERE t.wareHouse.id = :warehouseId
          AND ti.expireDate <= CURRENT_DATE
          AND t.status = 'COMPLETED'
        GROUP BY p.id, p.name, ti.expireDate
    """)
    fun findExpiredProducts(
        @Param("warehouseId") warehouseId: Long
    ): List<ExpiredProductDto>

}


@Repository
interface StockRepository : BaseRepository<Stock> {
    fun findByWarehouseAndProduct(
        warehouse: WareHouse,
        product: Product
    ): Stock?
}


