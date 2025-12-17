package org.example.warehouse.notification

import org.example.warehouse.StockRepository
import org.example.warehouse.TransactionItemRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class TelegramBotService(
    private val restTemplate: RestTemplate
) {
    private val token = "8413346311:AAHkKs0iIaq-EK8NRHoaTrN73dvP1451ggA"

    fun sendMessage(chatId: String, message: String) {
        val url = "https://api.telegram.org/bot$token/sendMessage?chat_id=$chatId&text=$message"
        val response = restTemplate.getForObject(url, String::class.java)
        println("Telegram response: $response")
    }
}


@Service
class NotificationService(
    private val telegramBotService: TelegramBotService
) {

    fun notifyExpiringProduct(
        chatId: String,
        productName: String,
        warehouseId: Long?,
        warehouseName: String,
        quantity: BigDecimal,
        daysLeft: Long
    ) {

        val message = """
            ⚠️ Yaroqlilik muddati yaqin!

            📦 Mahsulot: $productName
            🏬 Ombor: $warehouseName (ID: $warehouseId)
            📊 Qoldiq: $quantity
            ⏳ Tugashiga: $daysLeft kun qoldi
        """.trimIndent()

        telegramBotService.sendMessage(chatId, message)
    }
}


@Service
class ExpiredProductNotifier(
    private val transactionItemRepository: TransactionItemRepository,
    private val stockRepository: StockRepository,
    private val notificationService: NotificationService
) {

    private val myChatId = "6378358684"

    @Scheduled(cron = "0 0 9 * * ?")
    @Transactional
    fun checkExpiringProducts() {

        val today = LocalDate.now()
        val warningDays = 3L

        val expiringItems = transactionItemRepository.findAll()
            .filter { it.expireDate != null }
            .filter { !it.expireDate!!.isBefore(today) }
            .filter { it.expireDate!!.minusDays(warningDays).isBefore(today) }

        expiringItems.forEach { item ->

            val warehouse = item.transaction.wareHouse
            val product = item.product

            val stock = stockRepository
                .findByWarehouseAndProduct(warehouse, product)
                ?: return@forEach

            if (stock.quantity <= BigDecimal.ZERO) return@forEach

            val daysLeft = ChronoUnit.DAYS
                .between(today, item.expireDate)

            println(
                "Xabar yuboriladi: ${product.name}, " +
                        "warehouse=${warehouse.name}, qty=${stock.quantity}, daysLeft=$daysLeft"
            )

            notificationService.notifyExpiringProduct(
                chatId = myChatId,
                productName = product.name,
                warehouseId = warehouse.id,
                warehouseName = warehouse.name,
                quantity = stock.quantity,
                daysLeft = daysLeft
            )
        }

    }
}

