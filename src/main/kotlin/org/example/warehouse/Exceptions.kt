package org.example.warehouse

import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import kotlin.code

@ControllerAdvice
class ExceptionHandler(private val errorMessageSource: ResourceBundleMessageSource) {

    @ExceptionHandler(DemoExceptionHandler::class)
    fun handleAccountException(exception: DemoExceptionHandler): ResponseEntity<BaseMessage> {
        return ResponseEntity.badRequest().body(exception.getErrorMessage(errorMessageSource))
    }
}

sealed class DemoExceptionHandler() : RuntimeException() {
    abstract fun errorCode(): ErrorCodes
    open fun getArguments(): Array<Any?>? = null


    fun getErrorMessage(resourceBundleMessageSource: ResourceBundleMessageSource): BaseMessage {
        val message = try {
            resourceBundleMessageSource.getMessage(
                errorCode().name, getArguments(), LocaleContextHolder.getLocale()
            )
        } catch (e: Exception) {
            e.message
        }

        return BaseMessage(errorCode().code, message)
    }
}

class UserNameAlreadyExistsException : DemoExceptionHandler() {
    override fun errorCode() = ErrorCodes.USERNAME_ALREADY_EXISTS
}


class UserNotFoundException : DemoExceptionHandler() {
    override fun errorCode() = ErrorCodes.USER_NOT_FOUND
}


class UserAlreadyExistsException(): DemoExceptionHandler(){
    override fun errorCode() = ErrorCodes.USER_ALREADY_EXISTS

}


class CategoryNameAlreadyExists(): DemoExceptionHandler(){
    override fun errorCode()  = ErrorCodes.CATEGORY_NAME_ALREADY_EXISTS
}

class CategoryNotFound(): DemoExceptionHandler(){
    override fun errorCode() = ErrorCodes.CATEGORY_NOT_FOUND

}
class EmailAlreadyExistsException(): DemoExceptionHandler(){
    override fun errorCode() = ErrorCodes.EMAIL_ALREADY_EXISTS

}

class ProductNameAlreadyExists : DemoExceptionHandler() {
    override fun errorCode() = ErrorCodes.PRODUCT_NAME_ALREADY_EXISTS
}

class ProductNotFound : DemoExceptionHandler() {
    override fun errorCode() = ErrorCodes.PRODUCT_NOT_FOUND
}
class PasswordIsIncorrect: DemoExceptionHandler(){
    override fun errorCode() = ErrorCodes.PASSWORD_IS_INCORRECT
}
class NotLoggedInException: DemoExceptionHandler(){
    override fun errorCode() = ErrorCodes.NOT_LOGGED_IN_EXCEPTION

}

class PendingOrderNotFound: DemoExceptionHandler(){
    override fun errorCode() = ErrorCodes.PENDING_ORDER_NOT_FOUND

}
class Forbidden(): DemoExceptionHandler(){
    override fun errorCode() = ErrorCodes.FORBIDDEN

}

class PhoneNumberAlreadyExistsException(): DemoExceptionHandler() {
    override fun errorCode() = ErrorCodes.PHONE_NUMBER_ALREADY_EXISTS
}
class WareHouseNotFoundException(): DemoExceptionHandler() {
    override fun errorCode() = ErrorCodes.WAREHOUSE_NOT_FOUND
}

class WareHouseNameAlreadyExists(): DemoExceptionHandler() {
    override fun errorCode() = ErrorCodes.WAREHOUSE_NAME_ALREADY_EXISTS
}
class MeasurementUnitNameAlreadyExists(): DemoExceptionHandler() {
    override fun errorCode() = ErrorCodes.MEASUREMENT_UNIT_NAME_ALREADY_EXISTS
}
class MeasurementUnitNotFound(): DemoExceptionHandler() {
    override fun errorCode() = ErrorCodes.MEASUREMENT_UNIT_NOT_FOUND
}

class CurrencyNameAlreadyExists(): DemoExceptionHandler() {
    override fun errorCode() = ErrorCodes.CURRENCY_NAME_ALREADY_EXISTS
}

class CurrencyNotFound(): DemoExceptionHandler() {
    override fun errorCode() = ErrorCodes.CURRENCY_NOT_FOUND
}
class SupplierPhoneAlreadyExists: DemoExceptionHandler() {
    override fun errorCode() = ErrorCodes.SUPPLIER_PHONE_NUMBER_ALREADY_EXISTS
}

class SupplierNotFound(): DemoExceptionHandler(){
    override fun errorCode() = ErrorCodes.SUPPLIER_NOT_FOUND
}

class InsufficientStockException(val productName: String) : DemoExceptionHandler() {
    override fun errorCode() = ErrorCodes.FORBIDDEN
    override fun getArguments(): Array<Any?>? = arrayOf(productName)
}

class StockNotFoundException(val productName: String) : DemoExceptionHandler() {
    override fun errorCode() = ErrorCodes.FORBIDDEN
    override fun getArguments(): Array<Any?>? = arrayOf(productName)
}

class TransactionNotFoundException(val transactionId: String) : DemoExceptionHandler() {
    override fun errorCode() = ErrorCodes.PENDING_ORDER_NOT_FOUND
    override fun getArguments(): Array<Any?>? = arrayOf(transactionId)
}

