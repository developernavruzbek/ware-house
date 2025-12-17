package org.example.warehouse

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Table
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import org.hibernate.annotations.ColumnDefault
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Date


@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
class BaseEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @CreatedDate @Temporal(TemporalType.TIMESTAMP) var createdDate: Date? = null,
    @LastModifiedDate @Temporal(TemporalType.TIMESTAMP) var modifiedDate: Date? = null,
    @CreatedBy var createdBy: Long? = null,
    @LastModifiedBy var lastModifiedBy: Long? = null,
    @Column(nullable = false) @ColumnDefault(value = "false") var deleted: Boolean = false // (true - o'chirilgan bo'lsa)  (false - o'chirilmagan)
)

@Entity
class Category(
    @Column(nullable = false, unique = true)
    var name: String,

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    var status: Status = Status.ACTIVE,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    var parent: Category? = null
) : BaseEntity()



@Entity
class WareHouse(
    @Column(nullable = false, unique = true)
    var name: String,

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    var status: Status = Status.ACTIVE
) : BaseEntity()



@Entity
@Table(name = "users")
class User(
    @Column(nullable = false)
    var firstName: String,

    @Column(nullable = false)
    var lastName: String,

    @Column(nullable = false, unique = true)
    var phone: String,

    @Column(nullable = false, unique = true)
    var uniqueNumber: String,

    @Column(nullable = false)
    var password: String,

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    var role: UserRole,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    var wareHouse: WareHouse,

    @Enumerated(value = EnumType.STRING)
    var status: Status = Status.ACTIVE
) : BaseEntity()

@Entity
class MeasurementUnit(
    @Column(nullable = false, unique = true)
    var name: String,
    @Enumerated(value= EnumType.STRING)
    var status: Status = Status.ACTIVE
) : BaseEntity()


@Entity
class Supplier(
    @Column(nullable = false)
    var name: String,

    @Column(nullable = true, unique = true)
    var phone: String,

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    var status: Status = Status.ACTIVE
) : BaseEntity()



@Entity
class Product(
    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, unique = true)
    var uniqueCode: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    var category: Category,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "measurement_unit_id", nullable = false)
    var measurementUnit: MeasurementUnit,
) : BaseEntity()

@Entity
@Table(name = "transactions")
class Transaction(
    var type : TransactionType,
    var date: LocalDate,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    var wareHouse: WareHouse,

    @Enumerated(value = EnumType.STRING)
    var status : TransactionStatus = TransactionStatus.COMPLETED,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = true )
    var supplier: Supplier?= null,

    @Column(nullable = false)
    var invoiceNumber:String,

    @Column(nullable = false, unique = true)
    var uniqueNumber: String,


): BaseEntity()


@Entity
@Table(name = "transaction_items")
class TransactionItem(

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "transaction_id", nullable = false)
    var transaction: Transaction,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product,

    @Column(nullable = false, precision = 19, scale = 4)
    var quantity: BigDecimal,

    @Column(nullable = false, precision = 19, scale = 4)
    var price: BigDecimal,

    var expireDate: LocalDate? = null,

    @Column(precision = 19, scale = 4)
    var sellingPrice: BigDecimal? = null

) : BaseEntity()


@Entity
class Stock(
    @ManyToOne(fetch = FetchType.LAZY)
    var warehouse: WareHouse,

    @ManyToOne(fetch = FetchType.LAZY)
    var product: Product,

    var quantity: BigDecimal
) : BaseEntity()

