package com.example.ui

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.network.YahooFinanceClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = FinanceRepository(
        database.transactionDao(),
        database.investmentDao()
    )

    // User authentication session simulation (Firebase/Google Auth schema)
    var userEmail = mutableStateOf("syahrifatih66@gmail.com")
    var userDisplayName = mutableStateOf("Syahri Fatih")
    var isLoggedIn = mutableStateOf(true)

    // Biometric & Lock state
    var isLocked = mutableStateOf(true) // Starts locked for security
    var unlockError = mutableStateOf("")

    // Status indicator
    var isRefreshingStocks = mutableStateOf(false)

    // Customizable Categories (User can append/modify)
    val customCategories = mutableStateListOf(
        "Makan", "Investasi", "Gaji", "Belanja", "Transportasi", "Pendidikan", "Kesehatan", "Lainnya"
    )

    // Flows from database
    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val investments: StateFlow<List<InvestmentEntity>> = repository.allInvestments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Calculated Cash Balance: Sum of incomes minus expenses (Starting base: IDR 25,000,000)
    val baseCash = 25000000.0
    val cashBalance: StateFlow<Double> = transactions.map { list ->
        val change = list.sumOf { tx ->
            if (tx.type == "INCOME") tx.amount else -tx.amount
        }
        baseCash + change
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), baseCash)

    // Calculated Portfolio value
    val portfolioValue: StateFlow<Double> = investments.map { list ->
        list.sumOf { inv ->
            val totalShares = inv.lotCount * 100
            totalShares * inv.lastFetchedPrice
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Calculated Portfolio Cost Basin (to figure out total gain or loss)
    val portfolioCostBasis: StateFlow<Double> = investments.map { list ->
        list.sumOf { inv ->
            val totalShares = inv.lotCount * 100
            totalShares * inv.averageBuyPrice
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Total Agreggate Wealth
    val totalWealth: StateFlow<Double> = combine(cashBalance, portfolioValue) { cash, portfolio ->
        cash + portfolio
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), baseCash)

    init {
        // Seed first-launch data if empty so user has visual elements immediately
        viewModelScope.launch {
            val currentTxs = repository.allTransactions.first()
            if (currentTxs.isEmpty()) {
                // Seed Transaction ledger
                repository.insertTransaction(
                    TransactionEntity(
                        type = "INCOME",
                        amount = 12000000.0,
                        category = "Gaji",
                        notes = "Gaji bulanan",
                        timestamp = System.currentTimeMillis() - 86400000L * 2
                    )
                )
                repository.insertTransaction(
                    TransactionEntity(
                        type = "EXPENSE",
                        amount = 450000.0,
                        category = "Makan",
                        notes = "Makan malam steak",
                        timestamp = System.currentTimeMillis() - 36000000L
                    )
                )
                repository.insertTransaction(
                    TransactionEntity(
                        type = "EXPENSE",
                        amount = 4500000.0,
                        category = "Investasi",
                        notes = "Beli lot saham BBCA",
                        timestamp = System.currentTimeMillis() - 10000000L
                    )
                )
            }

            val currentInvs = repository.allInvestments.first()
            if (currentInvs.isEmpty()) {
                // Seed Stock Portfolio
                repository.insertInvestment(
                    InvestmentEntity(
                        ticker = "BBCA",
                        lotCount = 5, // 500 shares
                        averageBuyPrice = 9800.0,
                        lastFetchedPrice = 10050.0 // seeded default
                    )
                )
                repository.insertInvestment(
                    InvestmentEntity(
                        ticker = "TLKM",
                        lotCount = 10, // 1000 shares
                        averageBuyPrice = 3300.0,
                        lastFetchedPrice = 3450.0 // seeded default
                    )
                )
                repository.insertInvestment(
                    InvestmentEntity(
                        ticker = "GOTO",
                        lotCount = 100, // 10000 shares
                        averageBuyPrice = 85.0,
                        lastFetchedPrice = 81.0 // seeded default
                    )
                )
            }

            // Perform an initial background stock refresh from Yahoo Finance
            refreshStocks()
        }
    }

    // Security Unlock Handler
    fun attemptUnlock(pin: String): Boolean {
        return if (pin == "1234" || pin == "0000") {
            isLocked.value = false
            unlockError.value = ""
            true
        } else {
            unlockError.value = "PIN Salah! (Coba Gunakan: 1234)"
            false
        }
    }

    fun bypassAuth() {
        isLocked.value = false
        unlockError.value = ""
    }

    // Ledger Actions
    fun addTransaction(type: String, amount: Double, category: String, notes: String) {
        viewModelScope.launch {
            repository.insertTransaction(
                TransactionEntity(
                    type = type,
                    amount = amount,
                    category = category,
                    notes = notes,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteTransaction(id: Int) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    // Portfolio Actions
    fun addInvestment(ticker: String, lotCount: Int, averageBuyPrice: Double) {
        viewModelScope.launch {
            val uppercaseTicker = ticker.trim().uppercase()
            // Pull the latest price immediately if possible, fallback to buy price
            val livePrice = YahooFinanceClient.fetchStockPrice(uppercaseTicker) ?: averageBuyPrice
            repository.insertInvestment(
                InvestmentEntity(
                    ticker = uppercaseTicker,
                    lotCount = lotCount,
                    averageBuyPrice = averageBuyPrice,
                    lastFetchedPrice = livePrice
                )
            )
            // Trigger background refresh to sync other prices as well
            refreshStocks()
        }
    }

    fun deleteInvestment(id: Int) {
        viewModelScope.launch {
            repository.deleteInvestment(id)
        }
    }

    fun addCategory(categoryName: String) {
        val trimmed = categoryName.trim()
        if (trimmed.isNotEmpty() && !customCategories.contains(trimmed)) {
            customCategories.add(trimmed)
        }
    }

    // Stock Market Sync
    fun refreshStocks() {
        if (isRefreshingStocks.value) return
        isRefreshingStocks.value = true
        viewModelScope.launch {
            repository.refreshStockPrices()
            isRefreshingStocks.value = false
        }
    }

    // Mock Login/Logout
    fun logout() {
        isLoggedIn.value = false
        userEmail.value = ""
        userDisplayName.value = ""
    }

    fun loginWithGoogle(email: String, name: String) {
        isLoggedIn.value = true
        userEmail.value = email.ifEmpty { "syahrifatih66@gmail.com" }
        userDisplayName.value = name.ifEmpty { "Syahri Fatih" }
    }
}
