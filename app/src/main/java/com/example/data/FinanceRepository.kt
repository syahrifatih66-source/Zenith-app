package com.example.data

import com.example.network.YahooFinanceClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val investmentDao: InvestmentDao
) {
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allInvestments: Flow<List<InvestmentEntity>> = investmentDao.getAllInvestments()

    suspend fun insertTransaction(transaction: TransactionEntity) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun deleteTransaction(id: Int) {
        transactionDao.deleteTransaction(id)
    }

    suspend fun insertInvestment(investment: InvestmentEntity) {
        investmentDao.insertInvestment(investment)
    }

    suspend fun updateInvestment(investment: InvestmentEntity) {
        investmentDao.updateInvestment(investment)
    }

    suspend fun deleteInvestment(id: Int) {
        investmentDao.deleteInvestment(id)
    }

    /**
     * Iterates over all saved investment items, fetches real-time market prices,
     * and updates lastFetchedPrice inside the DB cache.
     */
    suspend fun refreshStockPrices() {
        try {
            val list = allInvestments.first()
            for (item in list) {
                val latestPrice = YahooFinanceClient.fetchStockPrice(item.ticker)
                if (latestPrice != null && latestPrice > 0) {
                    val updated = item.copy(lastFetchedPrice = latestPrice)
                    investmentDao.updateInvestment(updated)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
