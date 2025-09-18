package rabbit.utils.database

import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.Connection.TRANSACTION_READ_COMMITTED

fun <T> transactionOverride(statements: Transaction.() -> T): T {
    return TransactionManager.currentOrNew(TRANSACTION_READ_COMMITTED).run {
        val result = try {
            statements()
        } catch (e: Exception) {
            throw e
        } catch (e: ExposedSQLException) {
            throw e
        }

        result
    }
}