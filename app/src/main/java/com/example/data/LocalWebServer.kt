package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.util.Collections

class LocalWebServer(
    private val context: Context,
    private val repository: FinanceRepository
) {
    data class PairingRequest(
        val sessionId: String,
        val pin: String,
        var isAuthorized: Boolean = false,
        var authToken: String? = null,
        val createdAt: Long = System.currentTimeMillis()
    )

    private val pendingRequests = java.util.concurrent.ConcurrentHashMap<String, PairingRequest>()
    private val validTokens = java.util.concurrent.ConcurrentHashMap<String, Long>()

    fun getPendingRequests(): List<PairingRequest> {
        val now = System.currentTimeMillis()
        // Clean up expired (>10 mins old) pending requests
        pendingRequests.entries.removeIf { now - it.value.createdAt > 600_000 }
        return pendingRequests.values.filter { !it.isAuthorized }
    }

    fun authorizePin(pin: String): Boolean {
        val trimmedPin = pin.trim()
        val match = getPendingRequests().find { it.pin.equals(trimmedPin, ignoreCase = true) }
        if (match != null) {
            val token = "tok_" + java.util.UUID.randomUUID().toString().replace("-", "")
            match.isAuthorized = true
            match.authToken = token
            validTokens[token] = System.currentTimeMillis()
            return true
        }
        return false
    }

    fun isTokenValid(token: String?): Boolean {
        if (token == null) return false
        return validTokens.containsKey(token)
    }

    private var serverSocket: ServerSocket? = null
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    var isRunning = false
        private set

    fun start(port: Int = 8282): String? {
        if (isRunning) return getAddress(port)
        try {
            serverSocket = ServerSocket(port)
            isRunning = true
            job = scope.launch {
                while (isActive && serverSocket != null && !serverSocket!!.isClosed) {
                    try {
                        val socket = serverSocket!!.accept()
                        launch { handleClient(socket) }
                    } catch (e: Exception) {
                        Log.e("LocalWebServer", "Error accepting connection", e)
                    }
                }
            }
            return getAddress(port)
        } catch (e: Exception) {
            Log.e("LocalWebServer", "Error starting web server", e)
            isRunning = false
            return null
        }
    }

    fun stop() {
        isRunning = false
        job?.cancel()
        job = null
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            Log.e("LocalWebServer", "Error closing server socket", e)
        }
        serverSocket = null
    }

    fun getAddress(port: Int = 8282): String? {
        val ip = getLocalIp() ?: return null
        return "http://$ip:$port"
    }

    private fun getLocalIp(): String? {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val sAddr = addr.hostAddress ?: ""
                        val isIPv4 = sAddr.indexOf(':') < 0
                        if (isIPv4 && sAddr.isNotEmpty()) return sAddr
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("LocalWebServer", "Error getting local IP", e)
        }
        return "127.0.0.1"
    }

    private suspend fun handleClient(socket: Socket) = withContext(Dispatchers.IO) {
        socket.use { s ->
            try {
                val reader = BufferedReader(InputStreamReader(s.getInputStream()))
                val out = s.getOutputStream()
                
                // Read HTTP request line
                val requestLine = reader.readLine() ?: return@withContext
                val tokens = requestLine.split(" ")
                if (tokens.size < 2) return@withContext
                val method = tokens[0]
                val rawPath = tokens[1]
                
                // Read headers to determine body size and authorization
                var contentLength = 0
                val headers = mutableMapOf<String, String>()
                var line: String?
                while (true) {
                    line = reader.readLine()
                    if (line == null || line.isEmpty()) break
                    val lowerLine = line.lowercase()
                    if (lowerLine.startsWith("content-length:")) {
                        contentLength = line.substringAfter(":").trim().toIntOrNull() ?: 0
                    }
                    val colonIdx = line.indexOf(":")
                    if (colonIdx > 0) {
                        val key = line.substring(0, colonIdx).trim().lowercase()
                        val value = line.substring(colonIdx + 1).trim()
                        headers[key] = value
                    }
                }

                val pathParts = rawPath.split("?")
                val path = pathParts[0]
                val queryStr = if (pathParts.size > 1) pathParts[1] else ""
                val queryParams = decodeUrlParams(queryStr)

                val token = queryParams["token"] ?: headers["authorization"] ?: headers["x-auth-token"]
                val cleanToken = token?.removePrefix("Bearer ")?.trim()

                if (method == "GET") {
                    when (path) {
                        "/" -> serveHtml(out)
                        "/api/pairing-request" -> servePairingRequest(out)
                        "/api/pairing-status" -> servePairingStatus(queryParams, out)
                        "/api/data" -> {
                            if (!isTokenValid(cleanToken)) {
                                serve401(out)
                            } else {
                                serveApiData(out)
                            }
                        }
                        else -> serve404(out)
                    }
                } else if (method == "POST") {
                    if (!isTokenValid(cleanToken)) {
                        serve401(out)
                        return@use
                    }
                    var bodyStr = ""
                    if (contentLength > 0) {
                        val bodyChars = CharArray(contentLength)
                        var totalRead = 0
                        while (totalRead < contentLength) {
                            val read = reader.read(bodyChars, totalRead, contentLength - totalRead)
                            if (read == -1) break
                            totalRead += read
                        }
                        bodyStr = String(bodyChars, 0, totalRead)
                    }
                    when (path) {
                        "/api/transactions" -> handlePostTransaction(bodyStr, out)
                        "/api/budgets" -> handlePostBudget(bodyStr, out)
                        "/api/savings_goals" -> handlePostSavingsGoal(bodyStr, out)
                        "/api/people" -> handlePostPeople(bodyStr, out)
                        "/api/loans" -> handlePostLoan(bodyStr, out)
                        "/api/insurance" -> handlePostInsurance(bodyStr, out)
                        "/api/stocks" -> handlePostStock(bodyStr, out)
                        "/api/credit_cards" -> handlePostCreditCard(bodyStr, out)
                        "/api/delete" -> handlePostDelete(bodyStr, out)
                        else -> serve404(out)
                    }
                } else {
                    serve404(out)
                }
            } catch (e: Exception) {
                Log.e("LocalWebServer", "Error handling client session", e)
            }
        }
    }

    private fun servePairingRequest(out: OutputStream) {
        val sessionId = "S_" + java.util.UUID.randomUUID().toString().substring(0, 8)
        val pin = (1000 + java.util.Random().nextInt(9000)).toString()
        val pr = PairingRequest(sessionId, pin)
        pendingRequests[sessionId] = pr
        
        val json = """{"sessionId": "$sessionId", "pin": "$pin"}"""
        sendJsonResponse(out, json)
    }

    private fun servePairingStatus(queryParams: Map<String, String>, out: OutputStream) {
        val sessionId = queryParams["sessionId"]
        if (sessionId == null) {
            sendErrorResponse(out, 400, "Missing sessionId")
            return
        }
        val req = pendingRequests[sessionId]
        if (req == null) {
            sendErrorResponse(out, 404, "Pairing session not found or expired")
            return
        }
        
        val json = if (req.isAuthorized && req.authToken != null) {
            """{"success": true, "authToken": "${req.authToken}"}"""
        } else {
            """{"success": false, "status": "pending"}"""
        }
        sendJsonResponse(out, json)
    }

    private fun serve401(out: OutputStream) {
        val res = "HTTP/1.1 401 Unauthorized\r\nContent-Type: application/json\r\nAccess-Control-Allow-Origin: *\r\nContent-Length: 47\r\n\r\n{\"success\":false,\"error\":\"Unauthorized session\"}".toByteArray()
        out.write(res)
        out.flush()
    }

    private fun sendJsonResponse(out: OutputStream, json: String, status: String = "200 OK") {
        val bytes = json.toByteArray(Charsets.UTF_8)
        out.write("HTTP/1.1 $status\r\n".toByteArray())
        out.write("Content-Type: application/json; charset=utf-8\r\n".toByteArray())
        out.write("Content-Length: ${bytes.size}\r\n".toByteArray())
        out.write("Access-Control-Allow-Origin: *\r\n".toByteArray())
        out.write("\r\n".toByteArray())
        out.write(bytes)
        out.flush()
    }

    private fun sendErrorResponse(out: OutputStream, code: Int, message: String) {
        val json = """{"success": false, "error": "${escapeJson(message)}"}"""
        val status = when (code) {
            400 -> "400 Bad Request"
            401 -> "401 Unauthorized"
            404 -> "404 Not Found"
            else -> "500 Internal Server Error"
        }
        sendJsonResponse(out, json, status)
    }

    private fun decodeUrlParams(urlParams: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        if (urlParams.isEmpty()) return map
        val pairs = urlParams.split("&")
        for (pair in pairs) {
            val idx = pair.indexOf("=")
            if (idx > 0) {
                val key = URLDecoder.decode(pair.substring(0, idx), "UTF-8")
                val value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8")
                map[key] = value
            }
        }
        return map
    }

    private fun parseBodyParams(body: String): Map<String, String> {
        val params = mutableMapOf<String, String>()
        val trimmed = body.trim()
        if (trimmed.startsWith("{")) {
            // Simple robust regex parsing of standard JSON properties
            val inner = trimmed.removePrefix("{").removeSuffix("}").trim()
            val regex = "\"([^\"]+)\"\\s*:\\s*(?:\"([^\"]*)\"|([^,{}]+))".toRegex()
            regex.findAll(inner).forEach { matchResult ->
                val key = matchResult.groupValues[1]
                val val1 = matchResult.groupValues[2]
                val val2 = matchResult.groupValues[3]
                val value = if (val1.isNotEmpty() || (matchResult.value.contains("\"\"") && val1.isEmpty())) val1 else val2.trim()
                params[key] = value.removeSurrounding("\"")
            }
        } else {
            // URL Form parameters
            params.putAll(decodeUrlParams(body))
        }
        return params
    }

    private suspend fun serveApiData(out: OutputStream) {
        try {
            val txs = repository.allTransactions.take(1).first()
            val dbCategories = repository.allCategories.take(1).first().map { it.name }
            val categories = if (dbCategories.isNotEmpty()) dbCategories else listOf(
                "Food & Groceries", "Bills & Utilities", "Rent & Housing", "Travel & Fuel", 
                "Entertainment & Leisure", "Salary & Income", "Investment Dividend", "Health & Medical", "Shopping & Retail"
            )
            
            var totalIncome = 0.0
            var totalExpense = 0.0
            var totalInvestment = 0.0
            var totalSavings = 0.0
            
            txs.forEach { tx ->
                when (tx.type) {
                    "Income" -> totalIncome += tx.amount
                    "Expense" -> totalExpense += tx.amount
                    "Investment" -> totalInvestment += tx.amount
                }
                totalSavings += tx.autoSaveContribution
            }
            val netBalance = totalIncome - totalExpense - totalInvestment
            
            val jsonTxs = txs.joinToString(",") { tx ->
                """
                {
                    "id": ${tx.id},
                    "amount": ${tx.amount},
                    "category": "${escapeJson(tx.category)}",
                    "payer": "${escapeJson(tx.payer)}",
                    "notes": "${escapeJson(tx.notes)}",
                    "type": "${escapeJson(tx.type)}",
                    "paymentMethod": "${escapeJson(tx.paymentMethod)}",
                    "date": ${tx.date},
                    "autoSaveContribution": ${tx.autoSaveContribution}
                }
                """.trimIndent()
            }

            val budgets = repository.allBudgets.take(1).first()
            val jsonBudgets = budgets.joinToString(",") { b ->
                """
                {
                    "category": "${escapeJson(b.category)}",
                    "allocatedLimit": ${b.allocatedLimit}
                }
                """.trimIndent()
            }

            val savings = repository.allSavingsGoals.take(1).first()
            val jsonSavings = savings.joinToString(",") { s ->
                """
                {
                    "id": ${s.id},
                    "name": "${escapeJson(s.name)}",
                    "targetAmount": ${s.targetAmount},
                    "currentSaved": ${s.currentSaved},
                    "targetDate": "${escapeJson(s.targetDate)}",
                    "isAutomated": ${s.isAutomated}
                }
                """.trimIndent()
            }

            val people = repository.allPeople.take(1).first()
            val jsonPeople = people.joinToString(",") { p ->
                """
                {
                    "id": ${p.id},
                    "name": "${escapeJson(p.name)}",
                    "moneyGiven": ${p.moneyGiven},
                    "moneyReceived": ${p.moneyReceived},
                    "pendingBalance": ${p.pendingBalance},
                    "status": "${escapeJson(p.status)}"
                }
                """.trimIndent()
            }

            val loans = repository.allLoans.take(1).first()
            val jsonLoans = loans.joinToString(",") { l ->
                """
                {
                    "id": ${l.id},
                    "name": "${escapeJson(l.name)}",
                    "totalAmount": ${l.totalAmount},
                    "monthlyEmi": ${l.monthlyEmi},
                    "paidAmount": ${l.paidAmount},
                    "remainingAmount": ${l.remainingAmount},
                    "endDate": "${escapeJson(l.endDate)}"
                }
                """.trimIndent()
            }

            val insurance = repository.allInsurance.take(1).first()
            val jsonInsurance = insurance.joinToString(",") { i ->
                """
                {
                    "id": ${i.id},
                    "policyName": "${escapeJson(i.policyName)}",
                    "company": "${escapeJson(i.company)}",
                    "premium": ${i.premium},
                    "coverage": ${i.coverage},
                    "renewalDate": "${escapeJson(i.renewalDate)}",
                    "status": "${escapeJson(i.status)}"
                }
                """.trimIndent()
            }

            val stocks = repository.allStocks.take(1).first()
            val jsonStocks = stocks.joinToString(",") { s ->
                """
                {
                    "id": ${s.id},
                    "buyDate": "${escapeJson(s.buyDate)}",
                    "stockName": "${escapeJson(s.stockName)}",
                    "symbol": "${escapeJson(s.symbol)}",
                    "quantity": ${s.quantity},
                    "buyPrice": ${s.buyPrice},
                    "investedAmount": ${s.investedAmount},
                    "currentPrice": ${s.currentPrice},
                    "currentValue": ${s.currentValue},
                    "profitLoss": ${s.profitLoss},
                    "returnPercent": ${s.returnPercent}
                }
                """.trimIndent()
            }

            val cards = repository.allCreditCards.take(1).first()
            val jsonCards = cards.joinToString(",") { c ->
                """
                {
                    "id": ${c.id},
                    "cardName": "${escapeJson(c.cardName)}",
                    "totalSpend": ${c.totalSpend},
                    "paidAmount": ${c.paidAmount},
                    "pendingAmount": ${c.pendingAmount},
                    "status": "${escapeJson(c.status)}"
                }
                """.trimIndent()
            }

            val jsonResponse = """
            {
                "balance": $netBalance,
                "income": $totalIncome,
                "expenses": $totalExpense,
                "savings": $totalSavings,
                "categories": [${categories.joinToString(",") { "\"${escapeJson(it)}\"" }}],
                "recentTransactions": [$jsonTxs],
                "budgets": [$jsonBudgets],
                "savingsGoals": [$jsonSavings],
                "people": [$jsonPeople],
                "loans": [$jsonLoans],
                "insurance": [$jsonInsurance],
                "stocks": [$jsonStocks],
                "creditCards": [$jsonCards]
            }
            """.trimIndent()

            val bytes = jsonResponse.toByteArray(Charsets.UTF_8)
            out.write("HTTP/1.1 200 OK\r\n".toByteArray())
            out.write("Content-Type: application/json; charset=utf-8\r\n".toByteArray())
            out.write("Content-Length: ${bytes.size}\r\n".toByteArray())
            out.write("Access-Control-Allow-Origin: *\r\n".toByteArray())
            out.write("\r\n".toByteArray())
            out.write(bytes)
            out.flush()
        } catch (e: Exception) {
            Log.e("LocalWebServer", "Error serving JSON data", e)
            serve500(out, e.message ?: "Internal Server Error")
        }
    }

    private suspend fun handlePostBudget(bodyStr: String, out: OutputStream) {
        try {
            val params = parseBodyParams(bodyStr)
            val category = params["category"] ?: "Other"
            val allocatedLimit = params["allocatedLimit"]?.toDoubleOrNull() ?: 0.0
            
            repository.insertBudget(BudgetEntity(category, allocatedLimit))
            sendJsonResponse(out, """{"success": true}""")
        } catch (e: Exception) {
            sendErrorResponse(out, 500, e.message ?: "Failed to save budget")
        }
    }

    private suspend fun handlePostSavingsGoal(bodyStr: String, out: OutputStream) {
        try {
            val params = parseBodyParams(bodyStr)
            val id = params["id"]?.toIntOrNull() ?: 0
            val name = params["name"] ?: "New Goal"
            val targetAmount = params["targetAmount"]?.toDoubleOrNull() ?: 0.0
            val currentSaved = params["currentSaved"]?.toDoubleOrNull() ?: 0.0
            val targetDate = params["targetDate"] ?: ""
            val isAutomated = params["isAutomated"]?.toBoolean() ?: false
            
            repository.insertSavingsGoal(SavingsGoalEntity(id, name, targetAmount, currentSaved, targetDate, isAutomated))
            sendJsonResponse(out, """{"success": true}""")
        } catch (e: Exception) {
            sendErrorResponse(out, 500, e.message ?: "Failed to save savings goal")
        }
    }

    private suspend fun handlePostPeople(bodyStr: String, out: OutputStream) {
        try {
            val params = parseBodyParams(bodyStr)
            val id = params["id"]?.toIntOrNull() ?: 0
            val name = params["name"] ?: "Unnamed"
            val moneyGiven = params["moneyGiven"]?.toDoubleOrNull() ?: 0.0
            val moneyReceived = params["moneyReceived"]?.toDoubleOrNull() ?: 0.0
            val pendingBalance = params["pendingBalance"]?.toDoubleOrNull() ?: (moneyGiven - moneyReceived)
            val status = params["status"] ?: "Pending"
            
            repository.insertPerson(PeopleEntity(id, name, moneyGiven, moneyReceived, pendingBalance, status))
            sendJsonResponse(out, """{"success": true}""")
        } catch (e: Exception) {
            sendErrorResponse(out, 500, e.message ?: "Failed to save person")
        }
    }

    private suspend fun handlePostLoan(bodyStr: String, out: OutputStream) {
        try {
            val params = parseBodyParams(bodyStr)
            val id = params["id"]?.toIntOrNull() ?: 0
            val name = params["name"] ?: ""
            val totalAmount = params["totalAmount"]?.toDoubleOrNull() ?: 0.0
            val monthlyEmi = params["monthlyEmi"]?.toDoubleOrNull() ?: 0.0
            val paidAmount = params["paidAmount"]?.toDoubleOrNull() ?: 0.0
            val remainingAmount = params["remainingAmount"]?.toDoubleOrNull() ?: (totalAmount - paidAmount)
            val endDate = params["endDate"] ?: ""
            
            repository.insertLoan(LoanEntity(id, name, totalAmount, monthlyEmi, paidAmount, remainingAmount, endDate))
            sendJsonResponse(out, """{"success": true}""")
        } catch (e: Exception) {
            sendErrorResponse(out, 500, e.message ?: "Failed to save loan")
        }
    }

    private suspend fun handlePostInsurance(bodyStr: String, out: OutputStream) {
        try {
            val params = parseBodyParams(bodyStr)
            val id = params["id"]?.toIntOrNull() ?: 0
            val policyName = params["policyName"] ?: ""
            val company = params["company"] ?: ""
            val premium = params["premium"]?.toDoubleOrNull() ?: 0.0
            val coverage = params["coverage"]?.toDoubleOrNull() ?: 0.0
            val renewalDate = params["renewalDate"] ?: ""
            val status = params["status"] ?: "Active"
            
            repository.insertInsurance(InsuranceEntity(id, policyName, company, premium, coverage, renewalDate, status))
            sendJsonResponse(out, """{"success": true}""")
        } catch (e: Exception) {
            sendErrorResponse(out, 500, e.message ?: "Failed to save insurance")
        }
    }

    private suspend fun handlePostStock(bodyStr: String, out: OutputStream) {
        try {
            val params = parseBodyParams(bodyStr)
            val id = params["id"]?.toIntOrNull() ?: 0
            val buyDate = params["buyDate"] ?: ""
            val stockName = params["stockName"] ?: ""
            val symbol = params["symbol"] ?: ""
            val quantity = params["quantity"]?.toIntOrNull() ?: 0
            val buyPrice = params["buyPrice"]?.toDoubleOrNull() ?: 0.0
            val investedAmount = params["investedAmount"]?.toDoubleOrNull() ?: (quantity * buyPrice)
            val currentPrice = params["currentPrice"]?.toDoubleOrNull() ?: buyPrice
            val currentValue = params["currentValue"]?.toDoubleOrNull() ?: (quantity * currentPrice)
            val profitLoss = params["profitLoss"]?.toDoubleOrNull() ?: (currentValue - investedAmount)
            val returnPercent = params["returnPercent"]?.toDoubleOrNull() ?: if (investedAmount > 0) (profitLoss / investedAmount * 100.0) else 0.0
            
            repository.insertStock(StockEntity(id, buyDate, stockName, symbol, quantity, buyPrice, investedAmount, currentPrice, currentValue, profitLoss, returnPercent))
            sendJsonResponse(out, """{"success": true}""")
        } catch (e: Exception) {
            sendErrorResponse(out, 500, e.message ?: "Failed to save stock")
        }
    }

    private suspend fun handlePostCreditCard(bodyStr: String, out: OutputStream) {
        try {
            val params = parseBodyParams(bodyStr)
            val id = params["id"]?.toIntOrNull() ?: 0
            val cardName = params["cardName"] ?: ""
            val totalSpend = params["totalSpend"]?.toDoubleOrNull() ?: 0.0
            val paidAmount = params["paidAmount"]?.toDoubleOrNull() ?: 0.0
            val pendingAmount = params["pendingAmount"]?.toDoubleOrNull() ?: (totalSpend - paidAmount)
            val status = params["status"] ?: "Due"
            
            repository.insertCreditCard(CreditCardEntity(id, cardName, totalSpend, paidAmount, pendingAmount, status))
            sendJsonResponse(out, """{"success": true}""")
        } catch (e: Exception) {
            sendErrorResponse(out, 500, e.message ?: "Failed to save credit card")
        }
    }

    private suspend fun handlePostDelete(bodyStr: String, out: OutputStream) {
        try {
            val params = parseBodyParams(bodyStr)
            val target = params["target"] ?: ""
            val id = params["id"]?.toIntOrNull() ?: 0
            
            if (id > 0) {
                when (target) {
                    "transaction" -> repository.deleteTransaction(id)
                    "savings_goal" -> repository.deleteSavingsGoal(id)
                    "person" -> repository.deletePerson(id)
                    "loan" -> repository.deleteLoan(id)
                    "insurance" -> repository.deleteInsurance(id)
                    "stock" -> repository.deleteStock(id)
                    "credit_card" -> repository.deleteCreditCard(id)
                    else -> throw IllegalArgumentException("Unknown delete target: $target")
                }
                sendJsonResponse(out, """{"success": true}""")
            } else {
                sendErrorResponse(out, 400, "Invalid ID for deletion")
            }
        } catch (e: Exception) {
            sendErrorResponse(out, 500, e.message ?: "Failed to delete item")
        }
    }



    private suspend fun handlePostTransaction(bodyStr: String, out: OutputStream) {
        try {
            val params = parseBodyParams(bodyStr)
            val amount = params["amount"]?.toDoubleOrNull() ?: 0.0
            val category = params["category"] ?: "Food & Groceries"
            val payer = params["payer"] ?: "Self"
            val notes = params["notes"] ?: ""
            val type = params["type"] ?: "Expense"
            val paymentMethod = params["paymentMethod"] ?: "UPI"
            val autoSaveRule = params["autoSaveRule"] ?: "Round Up ($1)"

            if (amount <= 0) {
                val res = """{"success": false, "error": "Amount must be greater than zero"}"""
                val bytes = res.toByteArray()
                out.write("HTTP/1.1 400 Bad Request\r\n".toByteArray())
                out.write("Content-Type: application/json\r\n".toByteArray())
                out.write("Content-Length: ${bytes.size}\r\n".toByteArray())
                out.write("\r\n".toByteArray())
                out.write(bytes)
                out.flush()
                return
            }

            // Insert into the Room database
            repository.insertTransaction(
                amount = amount,
                category = category,
                payer = payer,
                notes = notes,
                type = type,
                paymentMethod = paymentMethod,
                autoSaveRule = autoSaveRule,
                customDate = System.currentTimeMillis()
            )

            val res = """{"success": true}"""
            val bytes = res.toByteArray()
            out.write("HTTP/1.1 200 OK\r\n".toByteArray())
            out.write("Content-Type: application/json\r\n".toByteArray())
            out.write("Content-Length: ${bytes.size}\r\n".toByteArray())
            out.write("Access-Control-Allow-Origin: *\r\n".toByteArray())
            out.write("\r\n".toByteArray())
            out.write(bytes)
            out.flush()

        } catch (e: Exception) {
            Log.e("LocalWebServer", "Error inserting web transaction", e)
            val res = """{"success": false, "error": "${escapeJson(e.message ?: "Unknown error")}"}"""
            val bytes = res.toByteArray()
            out.write("HTTP/1.1 500 Internal Server Error\r\n".toByteArray())
            out.write("Content-Type: application/json\r\n".toByteArray())
            out.write("Content-Length: ${bytes.size}\r\n".toByteArray())
            out.write("\r\n".toByteArray())
            out.write(bytes)
            out.flush()
        }
    }

    private fun escapeJson(str: String): String {
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t")
    }

    private fun serveHtml(out: OutputStream) {
        val rawHtml = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Family Wealth Vault | Sync Terminal</title>
    <!-- Embed standard QRCode library for offline browser rendering -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/qrcodejs/1.0.0/qrcode.min.js"></script>
    <style>
        :root {
            --bg-color: #F4F6FB;
            --card-color: #FFFFFF;
            --border-color: #E2E8F0;
            --teal-primary: #3B66F6;
            --text-white: #111E38;
            --text-gray: #64748B;
            --accent-green: #10B981;
            --accent-red: #EF4444;
            --accent-purple: #8B5CF6;
            --accent-yellow: #F59E0B;
        }
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
        }
        body {
            background-color: var(--bg-color);
            color: var(--text-white);
            line-height: 1.5;
            padding: 24px;
        }
        .container {
            max-width: 1240px;
            margin: 0 auto;
            transition: max-width 0.3s ease-in-out;
        }
        @media (min-width: 1200px) {
            .container {
                max-width: 1560px;
            }
        }
        
        .fade-in {
            animation: fadeIn 0.4s ease-out forwards;
        }
        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(10px); }
            to { opacity: 1; transform: translateY(0); }
        }

        header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding-bottom: 20px;
            border-bottom: 1px solid var(--border-color);
            margin-bottom: 20px;
        }
        .brand-logo {
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .brand-logo h1 {
            font-size: 20px;
            font-weight: 800;
            letter-spacing: 0.5px;
            background: linear-gradient(135deg, var(--teal-primary), var(--accent-purple));
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }
        .brand-logo span {
            font-size: 11px;
            font-weight: 700;
            background: rgba(59, 102, 246, 0.08);
            color: var(--teal-primary);
            padding: 4px 8px;
            border-radius: 4px;
            display: inline-block;
        }
        .status-badge {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 12px;
            font-weight: 600;
            color: var(--text-gray);
            background-color: var(--card-color);
            padding: 6px 14px;
            border-radius: 20px;
            border: 1px solid var(--border-color);
        }
        .pulse-dot {
            width: 8px;
            height: 8px;
            background-color: var(--teal-primary);
            border-radius: 50%;
            box-shadow: 0 0 10px var(--teal-primary);
            animation: pulse 1.8s infinite;
        }
        @keyframes pulse {
            0% { transform: scale(0.9); opacity: 0.8; }
            50% { transform: scale(1.15); opacity: 1; }
            100% { transform: scale(0.9); opacity: 0.8; }
        }
        
        /* Pairing Screen Styles */
        .auth-view {
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            min-height: 70vh;
            text-align: center;
        }
        .auth-card {
            background-color: var(--card-color);
            border: 1px solid var(--border-color);
            border-radius: 20px;
            padding: 40px;
            width: 100%;
            max-width: 480px;
            box-shadow: 0 15px 35px rgba(59, 102, 246, 0.05);
            display: flex;
            flex-direction: column;
            align-items: center;
        }
        .auth-card h2 {
            font-size: 20px;
            font-weight: 800;
            margin-bottom: 8px;
            letter-spacing: 0.5px;
            color: var(--text-white);
        }
        .auth-card p {
            font-size: 13px;
            color: var(--text-gray);
            margin-bottom: 24px;
            line-height: 1.4;
        }
        .qr-placeholder {
            width: 220px;
            height: 220px;
            background-color: var(--card-color);
            border: 2px solid var(--teal-primary);
            border-radius: 12px;
            padding: 10px;
            margin-bottom: 20px;
            display: flex;
            justify-content: center;
            align-items: center;
            box-shadow: 0 10px 25px rgba(204, 255, 0, 0.08);
        }
        .qr-placeholder img {
            border-radius: 4px;
        }
        .pairing-or {
            font-size: 11px;
            font-weight: 700;
            color: var(--text-gray);
            text-transform: uppercase;
            letter-spacing: 1px;
            margin: 10px 0;
            position: relative;
            width: 100%;
        }
        .pairing-or::before, .pairing-or::after {
            content: '';
            position: absolute;
            top: 50%;
            width: 30%;
            height: 1px;
            background-color: var(--border-color);
        }
        .pairing-or::before { left: 0; }
        .pairing-or::after { right: 0; }
        
        .pin-container {
            background-color: var(--bg-color);
            border: 1px dashed var(--border-color);
            border-radius: 8px;
            width: 100%;
            padding: 14px;
            margin-top: 10px;
        }
        .pin-label {
            font-size: 10px;
            font-weight: 700;
            color: var(--text-gray);
            text-transform: uppercase;
            letter-spacing: 0.8px;
            margin-bottom: 4px;
        }
        .pin-code {
            font-size: 32px;
            font-weight: 900;
            letter-spacing: 10px;
            color: var(--teal-primary);
            text-indent: 10px;
        }
        .auth-footer {
            font-size: 11px;
            color: var(--text-gray);
            margin-top: 24px;
            display: flex;
            align-items: center;
            gap: 6px;
        }

        /* Web Nav Tab Bar */
        .nav-tabs {
            display: flex;
            gap: 8px;
            background-color: var(--card-color);
            border: 1px solid var(--border-color);
            padding: 6px;
            border-radius: 12px;
            margin-bottom: 24px;
            overflow-x: auto;
        }
        .nav-tab-btn {
            background: none;
            border: none;
            color: var(--text-gray);
            font-size: 13px;
            font-weight: 700;
            padding: 10px 18px;
            cursor: pointer;
            border-radius: 8px;
            transition: all 0.2s;
            white-space: nowrap;
            display: flex;
            align-items: center;
            gap: 8px;
            flex: 1;
        }
        .nav-tab-btn:hover {
            color: var(--text-white);
            background-color: rgba(59, 102, 246, 0.04);
        }
        .nav-tab-btn.active {
            background-color: var(--teal-primary);
            color: #FFFFFF !important;
        }

        /* Stats Dashboard Row - Highlight and Scale Net Balance on wide laptops */
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 20px;
            margin-bottom: 24px;
        }
        @media (min-width: 1100px) {
            .stats-grid {
                grid-template-columns: 1.6fr 1.1fr 1.1fr 1.1fr;
            }
            .stat-balance {
                background: linear-gradient(135deg, var(--card-color), rgba(5, 178, 146, 0.08));
                border-color: rgba(5, 178, 146, 0.3) !important;
                position: relative;
                overflow: hidden;
            }
            .stat-balance::after {
                content: '🛡️';
                position: absolute;
                right: 20px;
                bottom: 4px;
                font-size: 55px;
                opacity: 0.12;
                pointer-events: none;
            }
        }
        .stat-card {
            background-color: var(--card-color);
            border: 1px solid var(--border-color);
            border-radius: 12px;
            padding: 20px;
            display: flex;
            flex-direction: column;
            gap: 4px;
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.03);
            transition: all 0.25s ease;
        }
        .stat-card:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 16px -2px rgba(0, 0, 0, 0.08);
        }
        .stat-card .label {
            font-size: 11px;
            font-weight: 700;
            color: var(--text-gray);
            letter-spacing: 0.5px;
            text-transform: uppercase;
        }
        .stat-card .value {
            font-size: 24px;
            font-weight: 900;
        }
        .stat-balance .value { color: var(--text-white); }
        .stat-income .value { color: var(--accent-green); }
        .stat-expense .value { color: var(--accent-red); }
        .stat-savings .value { color: var(--teal-primary); }

        /* Double Column Layout & Dashboard Widescreen Grid */
        .main-grid {
            display: grid;
            grid-template-columns: 1fr;
            gap: 24px;
        }
        @media (min-width: 900px) {
            .main-grid {
                grid-template-columns: 380px 1fr;
            }
        }

        .dashboard-layout {
            display: grid;
            grid-template-columns: 1fr;
            gap: 24px;
        }
        @media (min-width: 900px) and (max-width: 1149px) {
            .dashboard-layout {
                grid-template-columns: 380px 1fr;
            }
            .dashboard-people-panel {
                grid-column: 1 / -1;
            }
        }
        @media (min-width: 1150px) {
            .dashboard-layout {
                grid-template-columns: 350px 1fr 380px;
            }
        }
        
        .section-panel {
            background-color: var(--card-color);
            border: 1px solid var(--border-color);
            border-radius: 14px;
            padding: 22px;
            box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.04);
            height: fit-content;
            margin-bottom: 24px;
        }
        .section-title {
            font-size: 12px;
            font-weight: 800;
            color: var(--text-gray);
            letter-spacing: 0.8px;
            text-transform: uppercase;
            border-bottom: 1px solid var(--border-color);
            padding-bottom: 10px;
            margin-bottom: 16px;
        }
        
        /* Forms */
        .form-group {
            margin-bottom: 14px;
        }
        label {
            display: block;
            font-size: 11px;
            font-weight: 700;
            color: var(--text-gray);
            margin-bottom: 6px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        input, select, textarea {
            width: 100%;
            background-color: var(--bg-color);
            border: 1px solid var(--border-color);
            border-radius: 8px;
            padding: 10px 12px;
            font-size: 14px;
            color: var(--text-white);
            outline: none;
            transition: border-color 0.2s, box-shadow 0.2s;
        }
        input:focus, select:focus, textarea:focus {
            border-color: var(--teal-primary);
            box-shadow: 0 0 0 2px rgba(59, 102, 246, 0.12);
        }
        .form-row {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 12px;
        }
        button.action-btn {
            width: 100%;
            background-color: var(--teal-primary);
            color: #FFFFFF !important;
            border: none;
            border-radius: 8px;
            padding: 12px 20px;
            font-size: 13px;
            font-weight: 800;
            cursor: pointer;
            transition: transform 0.1s, opacity 0.2s;
            display: flex;
            justify-content: center;
            align-items: center;
            gap: 8px;
        }
        button.action-btn:hover {
            opacity: 0.95;
        }
        button.action-btn:active {
            transform: scale(0.98);
        }
        
        /* Table / List */
        .custom-tbl-container {
            overflow-x: auto;
            max-height: 500px;
            overflow-y: auto;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            text-align: left;
        }
        th {
            font-size: 10px;
            font-weight: 700;
            color: var(--text-gray);
            text-transform: uppercase;
            padding: 12px 14px;
            border-bottom: 1px solid var(--border-color);
            letter-spacing: 0.5px;
        }
        td {
            padding: 12px 14px;
            font-size: 13px;
            border-bottom: 1px solid var(--border-color);
        }
        tr:hover td {
            background-color: rgba(59, 102, 246, 0.03);
        }
        
        .badge {
            font-size: 9px;
            font-weight: 800;
            padding: 3px 6px;
            border-radius: 4px;
            display: inline-block;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        .badge-expense { background-color: rgba(239, 68, 68, 0.12); color: var(--accent-red); }
        .badge-income { background-color: rgba(16, 185, 129, 0.12); color: var(--accent-green); }
        .badge-investment { background-color: rgba(139, 92, 246, 0.12); color: var(--accent-purple); }
        .badge-transfer { background-color: rgba(100, 116, 139, 0.12); color: var(--text-gray); }
        
        .badge-pending { background-color: rgba(245, 158, 11, 0.12); color: var(--accent-yellow); }
        .badge-closed { background-color: rgba(16, 185, 129, 0.12); color: var(--accent-green); }
        .badge-active { background-color: rgba(16, 185, 129, 0.12); color: var(--accent-green); }
        .badge-due { background-color: rgba(239, 68, 68, 0.12); color: var(--accent-red); }
        .badge-paid { background-color: rgba(16, 185, 129, 0.12); color: var(--accent-green); }

         /* Progress track */
        .progress-track {
            background-color: #E2E8F0;
            border-radius: 4px;
            height: 6px;
            width: 100%;
            margin-top: 6px;
            overflow: hidden;
        }
        .progress-bar {
            height: 100%;
            background: linear-gradient(90deg, var(--teal-primary), var(--accent-green));
            border-radius: 4px;
        }

        /* Delete action Button */
        .btn-delete {
            background: none;
            border: none;
            color: var(--accent-red);
            font-weight: 700;
            font-size: 11px;
            cursor: pointer;
            padding: 4px 8px;
            border-radius: 4px;
            transition: background 0.2s;
            display: inline-flex;
            align-items: center;
        }
        .btn-delete:hover {
            background-color: rgba(239, 68, 68, 0.12);
        }
        
        /* Toast notification */
        .toast {
            position: fixed;
            bottom: 24px;
            right: 24px;
            background-color: var(--accent-green);
            color: #FFFFFF !important;
            font-weight: bold;
            padding: 12px 24px;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
            display: flex;
            align-items: center;
            gap: 8px;
            transform: translateY(150%);
            transition: transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
            z-index: 1000;
        }
        .toast.show {
            transform: translateY(0);
        }
        .logout-btn {
            background-color: rgba(239, 68, 68, 0.12);
            color: var(--accent-red);
            padding: 6px 14px;
            border-radius: 6px;
            font-size: 11px;
            font-weight: 800;
            border: 1px solid rgba(239, 68, 68, 0.2);
            cursor: pointer;
            width: auto;
            transition: background-color 0.2s;
        }
        .logout-btn:hover {
            background-color: var(--accent-red);
            color: #FFFFFF !important;
        }

        .cards-list-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
            gap: 16px;
        }
        .item-data-card {
            background-color: #F8FAFC;
            border: 1px solid var(--border-color);
            border-radius: 10px;
            padding: 14px;
            display: flex;
            flex-direction: column;
            gap: 8px;
        }
    </style>
</head>
<body>
    <div class="container">
        <!-- HEADER -->
        <header>
            <div class="brand-logo">
                <h1>⚡ NAME FN VAULT</h1>
                <span>Desktop Terminal</span>
            </div>
            <div id="header-right" class="status-badge" style="gap: 16px;">
                <div style="display: flex; align-items: center; gap: 8px;">
                    <div class="pulse-dot"></div>
                    <span id="node-status">Live Sync Online</span>
                </div>
                <button id="logout-head" class="logout-btn" style="display: none;" onclick="disconnectSession()">Disconnect</button>
            </div>
        </header>

        <!-- pairing screen node -->
        <div id="pairing-screen" class="auth-view fade-in">
            <div class="auth-card">
                <h2>🔗 Laptop Pairing Required</h2>
                <p>Authorize this browser session with your mobile app to synchronize financial ledgers.</p>
                
                <!-- Beautiful QR code component generator -->
                <div class="qr-placeholder" id="qrcode">
                    <span style="font-size: 11px; color: var(--text-gray);">Generating QR code...</span>
                </div>
                
                <div class="pairing-or">Or use PIN</div>
                
                <div class="pin-container">
                    <div class="pin-label">Secure Pairing Code</div>
                    <div class="pin-code" id="auth-pin">----</div>
                </div>
                
                <div class="auth-footer">
                    <div class="pulse-dot" style="background-color: var(--teal-primary); box-shadow: 0 0 10px var(--teal-primary);"></div>
                    <span>Standby: Open mobile app settings & scan</span>
                </div>
            </div>
        </div>

        <!-- authenticated workspace dashboard -->
        <div id="dashboard-space" style="display: none;" class="fade-in">
            <!-- TABS BAR -->
            <div class="nav-tabs">
                <button class="nav-tab-btn active" id="tab-btn-dashboard" onclick="switchTab('dashboard')">📊 Main Ledger</button>
                <button class="nav-tab-btn" id="tab-btn-budgets" onclick="switchTab('budgets')">🎯 Budgets & Savings</button>
                <button class="nav-tab-btn" id="tab-btn-people" onclick="switchTab('people')">🤝 Family Members</button>
                <button class="nav-tab-btn" id="tab-btn-loans" onclick="switchTab('loans')">🏦 Loans & Insurance</button>
                <button class="nav-tab-btn" id="tab-btn-wealth" onclick="switchTab('wealth')">📈 Stocks & Credit Cards</button>
            </div>

            <!-- STATS CARDS -->
            <div class="stats-grid">
                <div class="stat-card stat-balance">
                    <span class="label">Vault Net Balance</span>
                    <span class="value" id="val-balance">₹0.00</span>
                </div>
                <div class="stat-card stat-income">
                    <span class="label">Cumulative Earnings</span>
                    <span class="value" id="val-income">₹0.00</span>
                </div>
                <div class="stat-card stat-expense">
                    <span class="label">Total Spent Outflow</span>
                    <span class="value" id="val-expense">₹0.00</span>
                </div>
                <div class="stat-card stat-savings">
                    <span class="label">Automated Micro-Savings</span>
                    <span class="value" id="val-savings">₹0.00</span>
                </div>
            </div>

            <!-- TAB 1: MAIN LEDGER (DASHBOARD) -->
            <div id="tab-content-dashboard" class="dashboard-layout fade-in">
                <div class="section-panel">
                    <h2 class="section-title">➕ Direct Ledger Entry</h2>
                    <form id="tx-form">
                        <div class="form-group">
                            <label for="payer">Authorized Payer Unit</label>
                            <select id="payer" required>
                                <option value="Self">Self (Default)</option>
                                <option value="Dad">Dad</option>
                                <option value="Mom">Mom</option>
                                <option value="Bro">Brother</option>
                                <option value="Sis">Sister</option>
                            </select>
                        </div>

                        <div class="form-row">
                            <div class="form-group">
                                <label for="type">Flow Type</label>
                                <select id="type" required>
                                    <option value="Expense" selected>Expense Outflow</option>
                                    <option value="Income">Income Inflow</option>
                                    <option value="Investment">Investment Capital</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label for="paymentMethod">Payment Method</label>
                                <select id="paymentMethod" required>
                                    <option value="UPI" selected>UPI Mobile Payment</option>
                                    <option value="Credit Card">Credit Card</option>
                                    <option value="Cash">Physical Cash</option>
                                    <option value="Debit Card">Debit Card</option>
                                    <option value="Bank Transfer">Bank Wire</option>
                                </select>
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="category">Allocation Channel Category</label>
                            <select id="category" required>
                                <!-- Will be populated dynamically by JavaScript from DB! -->
                            </select>
                        </div>

                        <div class="form-row">
                            <div class="form-group">
                                <label for="amount">Transaction Amount (₹)</label>
                                <input type="number" id="amount" step="0.01" min="0.01" placeholder="0.00" required>
                            </div>
                            <div class="form-group">
                                <label for="autoSaveRule">Round-up Savings Rule</label>
                                <select id="autoSaveRule" required>
                                    <option value="Round Up ($1)" selected>Round Up (₹1)</option>
                                    <option value="Round Up ($5)">Round Up (₹5)</option>
                                    <option value="5% Auto-Save">5% Contribution</option>
                                    <option value="10% Auto-Save">10% Contribution</option>
                                    <option value="None">Disable Savings</option>
                                </select>
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="notes">Reference Memo / Audit Notes</label>
                            <input type="text" id="notes" placeholder="e.g. Supermarket retail items purchase" required>
                        </div>

                        <button type="submit" id="submit-btn" class="action-btn">
                            <span>Save Sync to Android Vault</span>
                        </button>
                    </form>
                </div>

                <div class="section-panel" style="min-width: 0;">
                    <h2 class="section-title">⏳ Recent Ledger Audits</h2>
                    <div class="custom-tbl-container">
                        <table>
                            <thead>
                                <tr>
                                    <th>Date</th>
                                    <th>Payer</th>
                                    <th>Category</th>
                                    <th>Notes</th>
                                    <th>Method</th>
                                    <th>Type</th>
                                    <th>Amount</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                            <tbody id="ledger-body">
                                <tr>
                                    <td colspan="8" style="text-align: center; color: var(--text-gray); padding: 40px 0;">
                                        Terminal fetching vault records...
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Column 3: People Tracker Widget (Always visible on Laptop viewports) -->
                <div class="section-panel dashboard-people-panel" style="min-width: 0;">
                    <h2 class="section-title">🤝 Dashboard Family Tracker</h2>
                    <div style="margin-bottom: 12px; font-size: 11px; color: var(--text-gray); display: flex; justify-content: space-between; align-items: center;">
                        <span>Active Family Members</span>
                        <span id="dashboard-people-count" style="font-weight: 800; color: var(--teal-primary); background-color: rgba(5, 178, 146, 0.12); padding: 2px 7px; border-radius: 4px;">0</span>
                    </div>
                    <div id="dashboard-people-list" style="display: grid; grid-template-columns: 1fr; gap: 12px; max-height: 480px; overflow-y: auto; padding-right: 4px;">
                        <span style="font-size: 11px; color: var(--text-gray);">Fetching active member balances...</span>
                    </div>
                </div>
            </div>

            <!-- TAB 2: BUDGETS & SAVINGS -->
            <div id="tab-content-budgets" class="main-grid fade-in" style="display: none;">
                <div class="section-panel">
                    <h2 class="section-title">➕ Manage Budgets & Savings</h2>
                    
                    <form id="budget-form" style="margin-bottom: 24px;">
                        <span style="font-size: 11px; font-weight: bold; color: var(--teal-primary); display: block; margin-bottom: 12px; text-transform: uppercase;">Set Allocation/Budget Limit</span>
                        <div class="form-group">
                            <label for="budget-category">Category</label>
                            <select id="budget-category" required>
                                <option value="Food">Food</option>
                                <option value="Grocery">Grocery</option>
                                <option value="Rent">Rent</option>
                                <option value="Bills">Bills</option>
                                <option value="Fuel">Fuel</option>
                                <option value="Medical">Medical</option>
                                <option value="Shopping">Shopping</option>
                                <option value="Other">Other</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label for="budget-limit">Limit Amount (₹)</label>
                            <input type="number" id="budget-limit" step="1" min="100" placeholder="e.g. 5000" required>
                        </div>
                        <button type="submit" id="budget-submit-btn" class="action-btn">Update Budget Limit</button>
                    </form>

                    <hr style="border: 0; border-top: 1px solid var(--border-color); margin: 20px 0;" />

                    <form id="savings-form">
                        <span style="font-size: 11px; font-weight: bold; color: var(--teal-primary); display: block; margin-bottom: 12px; text-transform: uppercase;">Create Savings Target Goal</span>
                        <div class="form-group">
                            <label for="saving-name">Goal Name</label>
                            <input type="text" id="saving-name" placeholder="e.g. New SUV car fund" required>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label for="saving-target">Target Amount (₹)</label>
                                <input type="number" id="saving-target" step="100" min="100" placeholder="e.g. 100000" required>
                            </div>
                            <div class="form-group">
                                <label for="saving-current">Current Saved (₹)</label>
                                <input type="number" id="saving-current" step="100" min="0" placeholder="e.g. 15000" required>
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label for="saving-date">Target Month/Year</label>
                                <input type="text" id="saving-date" placeholder="e.g. Dec 2026" required>
                            </div>
                            <div class="form-group" style="display: flex; flex-direction: column; justify-content: center; align-items: start; padding-top: 14px;">
                                <label style="display: flex; align-items: center; gap: 8px; cursor: pointer; text-transform: none;">
                                    <input type="checkbox" id="saving-auto" style="width: auto;"> Receive Automated Round-ups
                                </label>
                            </div>
                        </div>
                        <button type="submit" id="savings-submit-btn" class="action-btn">Add Savings Goal</button>
                    </form>
                </div>

                <div class="section-panel">
                    <h2 class="section-title">🎯 Active Budgets & Savings Targets</h2>
                    
                    <div style="margin-bottom: 24px;">
                        <h4 style="font-size: 11px; font-weight: 800; text-transform: uppercase; color: var(--text-gray); margin-bottom: 12px; letter-spacing: 0.5px;">Monthly Allocation Budgets</h4>
                        <div class="custom-tbl-container" style="max-height: 200px;">
                            <table>
                                <thead>
                                    <tr>
                                        <th>Category Channel</th>
                                        <th>Allocated Limit (₹)</th>
                                    </tr>
                                </thead>
                                <tbody id="budget-list-body">
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <div>
                        <h4 style="font-size: 11px; font-weight: 800; text-transform: uppercase; color: var(--text-gray); margin-bottom: 12px; letter-spacing: 0.5px;">Savings Goals</h4>
                        <div id="savings-goal-list" class="cards-list-grid">
                        </div>
                    </div>
                </div>
            </div>

            <!-- TAB 3: FAMILY PEOPLE -->
            <div id="tab-content-people" class="main-grid fade-in" style="display: none;">
                <div class="section-panel">
                    <h2 class="section-title">➕ Record Family Lending/Borrow</h2>
                    <form id="people-form">
                        <div class="form-group">
                            <label for="person-name">Associated Person / Member Name</label>
                            <input type="text" id="person-name" placeholder="e.g. Kumar Shinde" required>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label for="person-given">Amount Given (Lent) (₹)</label>
                                <input type="number" id="person-given" step="1" min="0" placeholder="0" required>
                            </div>
                            <div class="form-group">
                                <label for="person-received">Amount Received (Borrowed) (₹)</label>
                                <input type="number" id="person-received" step="1" min="0" placeholder="0" required>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="person-status">Settlement Status</label>
                            <select id="person-status" required>
                                <option value="Pending" selected>Pending</option>
                                <option value="Closed">Closed / Settled</option>
                            </select>
                        </div>
                        <button type="submit" id="people-submit-btn" class="action-btn">Add Lending Entry</button>
                    </form>
                </div>

                <div class="section-panel">
                    <h2 class="section-title">🤝 Family Ledger / Mutual Lending Track</h2>
                    <div id="people-list" class="cards-list-grid">
                    </div>
                </div>
            </div>

            <!-- TAB 4: LOANS & INSURANCE -->
            <div id="tab-content-loans" class="main-grid fade-in" style="display: none;">
                <div class="section-panel">
                    <h2 class="section-title">➕ Add Loan / Insurance Policy</h2>
                    
                    <form id="loans-form" style="margin-bottom: 24px;">
                        <span style="font-size: 11px; font-weight: bold; color: var(--teal-primary); display: block; margin-bottom: 12px; text-transform: uppercase;">Record Loan Liability Ledger</span>
                        <div class="form-group">
                            <label for="loan-name">Loan Identifier</label>
                            <input type="text" id="loan-name" placeholder="e.g. SBI Home Loan" required>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label for="loan-total">Principal Amount (₹)</label>
                                <input type="number" id="loan-total" step="100" min="1000" placeholder="e.g. 1500000" required>
                            </div>
                            <div class="form-group">
                                <label for="loan-emi">Monthly EMI (₹)</label>
                                <input type="number" id="loan-emi" step="10" min="100" placeholder="e.g. 18500" required>
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label for="loan-paid">Accumulated Paid (₹)</label>
                                <input type="number" id="loan-paid" step="100" min="0" placeholder="e.g. 450000" required>
                            </div>
                            <div class="form-group">
                                <label for="loan-end">Maturity / End Date</label>
                                <input type="text" id="loan-end" placeholder="e.g. Dec 2032" required>
                            </div>
                        </div>
                        <button type="submit" id="loan-submit-btn" class="action-btn">Register Loan Liability</button>
                    </form>

                    <hr style="border: 0; border-top: 1px solid var(--border-color); margin: 20px 0;" />

                    <form id="insurance-form">
                        <span style="font-size: 11px; font-weight: bold; color: var(--teal-primary); display: block; margin-bottom: 12px; text-transform: uppercase;">Register Protection Insurance</span>
                        <div class="form-group">
                            <label for="ins-name">Policy Name / Cover</label>
                            <input type="text" id="ins-name" placeholder="e.g. Comprehensive Term Plan" required>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label for="ins-company">Insurance Provider Company</label>
                                <input type="text" id="ins-company" placeholder="e.g. HDFC ERGO" required>
                            </div>
                            <div class="form-group">
                                <label for="ins-premium">Premium Amount (₹)</label>
                                <input type="number" id="ins-premium" step="10" min="0" placeholder="e.g. 24000" required>
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label for="ins-coverage">Coverage Benefit (₹)</label>
                                <input type="number" id="ins-coverage" step="10000" min="10000" placeholder="e.g. 10000000" required>
                            </div>
                            <div class="form-group">
                                <label for="ins-renewal">Renewal Due Date</label>
                                <input type="text" id="ins-renewal" placeholder="e.g. 15 Oct 2026" required>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="ins-status">Policy Status</label>
                            <select id="ins-status" required>
                                <option value="Active" selected>Active</option>
                                <option value="Near Expiry">Near Expiry</option>
                                <option value="Expired">Expired</option>
                            </select>
                        </div>
                        <button type="submit" id="ins-submit-btn" class="action-btn">Register Protection Policy</button>
                    </form>
                </div>

                <div class="section-panel">
                    <h2 class="section-title">🏛️ Active Family Liabilities & Insurances</h2>
                    
                    <div style="margin-bottom: 24px;">
                        <h4 style="font-size: 11px; font-weight: 800; text-transform: uppercase; color: var(--text-gray); margin-bottom: 12px; letter-spacing: 0.5px;">Outstanding Loans Checklist</h4>
                        <div id="loans-list" class="cards-list-grid">
                        </div>
                    </div>

                    <div>
                        <h4 style="font-size: 11px; font-weight: 800; text-transform: uppercase; color: var(--text-gray); margin-bottom: 12px; letter-spacing: 0.5px;">Security / Insurance Coverage</h4>
                        <div id="insurances-list" class="cards-list-grid">
                        </div>
                    </div>
                </div>
            </div>

            <!-- TAB 5: WEALTH PORTFOLIO & CREDIT CARDS -->
            <div id="tab-content-wealth" class="main-grid fade-in" style="display: none;">
                <div class="section-panel">
                    <h2 class="section-title">➕ Sync Stocks & Credit Cards</h2>
                    
                    <form id="stock-form" style="margin-bottom: 24px;">
                        <span style="font-size: 11px; font-weight: bold; color: var(--teal-primary); display: block; margin-bottom: 12px; text-transform: uppercase;">Log Equity Stock Purchase</span>
                        <div class="form-row">
                            <div class="form-group">
                                <label for="stock-name">Stock Name</label>
                                <input type="text" id="stock-name" placeholder="e.g. Tata Motors Ltd" required>
                            </div>
                            <div class="form-group">
                                <label for="stock-sym">Symbol code</label>
                                <input type="text" id="stock-sym" placeholder="e.g. TATAMOTORS" required>
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label for="stock-qty">Quantity purchased</label>
                                <input type="number" id="stock-qty" step="1" min="1" placeholder="e.g. 50" required>
                            </div>
                            <div class="form-group">
                                <label for="stock-price">Buy Unit Price (₹)</label>
                                <input type="number" id="stock-price" step="0.05" min="0.05" placeholder="e.g. 820.00" required>
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label for="stock-curr">Current Market Price (₹)</label>
                                <input type="number" id="stock-curr" step="0.05" min="0.05" placeholder="e.g. 910.00" required>
                            </div>
                            <div class="form-group">
                                <label for="stock-date">Purchase Date</label>
                                <input type="text" id="stock-date" placeholder="e.g. 15 Mar 2025" required>
                            </div>
                        </div>
                        <button type="submit" id="stock-submit-btn" class="action-btn">Log Equity Purchase</button>
                    </form>

                    <hr style="border: 0; border-top: 1px solid var(--border-color); margin: 20px 0;" />

                    <form id="card-form">
                        <span style="font-size: 11px; font-weight: bold; color: var(--teal-primary); display: block; margin-bottom: 12px; text-transform: uppercase;">Credit Card Balance Statement</span>
                        <div class="form-group">
                            <label for="card-name">Credit Card Name</label>
                            <input type="text" id="card-name" placeholder="e.g. Regalia Gold HDFC" required>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label for="card-spend">Total Spent Limit (₹)</label>
                                <input type="number" id="card-spend" step="10" min="0" placeholder="e.g. 45000" required>
                            </div>
                            <div class="form-group">
                                <label for="card-paid">Paid Amount Statement (₹)</label>
                                <input type="number" id="card-paid" step="10" min="0" placeholder="e.g. 15000" required>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="card-status">Billing Status</label>
                            <select id="card-status" required>
                                <option value="Due" selected>Due (Outstanding balance)</option>
                                <option value="Paid">Paid (Current tier settled)</option>
                            </select>
                        </div>
                        <button type="submit" id="card-submit-btn" class="action-btn">Log Card Statement</button>
                    </form>
                </div>

                <div class="section-panel">
                    <h2 class="section-title">💹 Family Equity & Credit Card Ledgers</h2>
                    
                    <div style="margin-bottom: 24px;">
                        <h4 style="font-size: 11px; font-weight: 800; text-transform: uppercase; color: var(--text-gray); margin-bottom: 12px; letter-spacing: 0.5px;">Equity Markets Portfolio</h4>
                        <div id="stocks-list" class="cards-list-grid">
                        </div>
                    </div>

                    <div>
                        <h4 style="font-size: 11px; font-weight: 800; text-transform: uppercase; color: var(--text-gray); margin-bottom: 12px; letter-spacing: 0.5px;">Synced Credit Cards Outstanding</h4>
                        <div id="cards-list" class="cards-list-grid">
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- TOAST NOTIFICATION -->
    <div class="toast" id="toast">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="20 6 9 17 4 12"></polyline>
        </svg>
        <span id="toast-msg">Entry recorded instantly!</span>
    </div>

    <script>
        const formatter = new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: 'INR',
            maximumFractionDigits: 2
        });

        function formatCurrency(v) {
            return formatter.format(v);
        }

        function showToast(msg) {
            const toast = document.getElementById('toast');
            document.getElementById('toast-msg').innerText = msg;
            toast.classList.add('show');
            setTimeout(() => {
                toast.classList.remove('show');
            }, 3000);
        }

        let pollInterval = null;
        let pairingPollInterval = null;
        let activeTab = 'dashboard';

        // Switch panel tabs
        function switchTab(tabId) {
            // Hide all tab screens
            document.getElementById('tab-content-dashboard').style.display = 'none';
            document.getElementById('tab-content-budgets').style.display = 'none';
            document.getElementById('tab-content-people').style.display = 'none';
            document.getElementById('tab-content-loans').style.display = 'none';
            document.getElementById('tab-content-wealth').style.display = 'none';
            
            // Remove active button highlighting
            document.getElementById('tab-btn-dashboard').classList.remove('active');
            document.getElementById('tab-btn-budgets').classList.remove('active');
            document.getElementById('tab-btn-people').classList.remove('active');
            document.getElementById('tab-btn-loans').classList.remove('active');
            document.getElementById('tab-btn-wealth').classList.remove('active');

            // Show targets
            document.getElementById('tab-content-' + tabId).style.display = tabId === 'dashboard' ? 'grid' : 'grid';
            if (tabId !== 'dashboard' && tabId !== 'budgets' && tabId !== 'people' && tabId !== 'loans' && tabId !== 'wealth') {
               // Defensive logic
            } else {
               document.getElementById('tab-content-' + tabId).style.display = 'grid';
            }
            
            document.getElementById('tab-btn-' + tabId).classList.add('active');
            activeTab = tabId;
        }

        // Initialize state
        window.addEventListener('DOMContentLoaded', () => {
            const token = localStorage.getItem("vault_auth_token");
            if (token) {
                switchToAuthenticated();
            } else {
                switchToPairing();
            }
        });

        function switchToPairing() {
            document.getElementById("dashboard-space").style.display = "none";
            document.getElementById("pairing-screen").style.display = "flex";
            document.getElementById("logout-head").style.display = "none";
            document.getElementById("node-status").innerText = "Awaiting Pairing";
            
            if (pollInterval) {
                clearInterval(pollInterval);
                pollInterval = null;
            }
            startPairingProcedure();
        }

        function switchToAuthenticated() {
            document.getElementById("pairing-screen").style.display = "none";
            document.getElementById("dashboard-space").style.display = "block";
            document.getElementById("logout-head").style.display = "block";
            document.getElementById("node-status").innerText = "Vault Authorized";
            
            if (pairingPollInterval) {
                clearInterval(pairingPollInterval);
                pairingPollInterval = null;
            }
            
            fetchVaultRecords();
            pollInterval = setInterval(fetchVaultRecords, 4000);
        }

        function disconnectSession() {
            localStorage.removeItem("vault_auth_token");
            switchToPairing();
        }

        async function startPairingProcedure() {
            try {
                const res = await fetch("/api/pairing-request");
                const data = await res.json();
                
                const sessionId = data.sessionId;
                const pin = data.pin;
                
                document.getElementById("auth-pin").innerText = pin;
                
                // Build visual QR code
                const qrContainer = document.getElementById("qrcode");
                qrContainer.innerHTML = "";
                if (window.QRCode) {
                    new QRCode(qrContainer, {
                        text: "vaultpair:" + pin,
                        width: 200,
                        height: 200,
                        colorDark : "#CCFF00",
                        colorLight : "#111D19",
                        correctLevel : QRCode.CorrectLevel.H
                    });
                } else {
                    qrContainer.innerHTML = "<span style='font-size: 12px; color: var(--teal-primary); font-weight: bold;'>PIN: " + pin + "</span>";
                }

                // Poll pairing confirmation status
                if (pairingPollInterval) clearInterval(pairingPollInterval);
                pairingPollInterval = setInterval(async () => {
                    try {
                        const statusRes = await fetch(`/api/pairing-status?sessionId=${'$'}{sessionId}`);
                        if (statusRes.status === 404) {
                            clearInterval(pairingPollInterval);
                            startPairingProcedure();
                            return;
                        }
                        const statusData = await statusRes.json();
                        if (statusData.success && statusData.authToken) {
                            clearInterval(pairingPollInterval);
                            pairingPollInterval = null;
                            localStorage.setItem("vault_auth_token", statusData.authToken);
                            switchToAuthenticated();
                            showToast("✔️ Laptop Linked Successfully!");
                        }
                    } catch (e) {
                        console.error("Error checking pairing status", e);
                    }
                }, 1500);

            } catch (e) {
                console.error("Pairing failure, retrying...", e);
                setTimeout(startPairingProcedure, 4000);
            }
        }

        // Global central generic delete operation
        async function deleteDatabaseItem(target, id) {
            if (!confirm(`Are you sure you want to delete this ${'$'}{target} entry from the secure database?`)) return;
            
            const token = localStorage.getItem("vault_auth_token") || "";
            try {
                const res = await fetch(`/api/delete?token=${'$'}{token}`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'x-auth-token': token,
                        'authorization': 'Bearer ' + token
                    },
                    body: JSON.stringify({ target: target, id: parseInt(id) })
                });
                const result = await res.json();
                if (result.success) {
                    showToast('🗑️ Entry removed successfully!');
                    fetchVaultRecords();
                } else {
                    alert('Deletion failed: ' + result.error);
                }
            } catch (err) {
                console.error(err);
                alert('Connection to Android device interrupted.');
            }
        }

        // Fetch overall records periodically
        async function fetchVaultRecords() {
            const token = localStorage.getItem("vault_auth_token") || "";
            try {
                const res = await fetch(`/api/data?token=${'$'}{token}`, {
                    headers: {
                        'x-auth-token': token,
                        'authorization': 'Bearer ' + token
                    }
                });
                
                if (res.status === 401) {
                    disconnectSession();
                    return;
                }

                const data = await res.json();
                
                // 1. Update stats
                document.getElementById('val-balance').innerText = formatCurrency(data.balance);
                document.getElementById('val-income').innerText = formatCurrency(data.income);
                document.getElementById('val-expense').innerText = formatCurrency(data.expenses);
                document.getElementById('val-savings').innerText = formatCurrency(data.savings);

                // 2. Direct entry category list populate
                const catDropdown = document.getElementById('category');
                if (catDropdown.children.length === 0) {
                    data.categories.forEach(cat => {
                        const opt = document.createElement('option');
                        opt.value = cat;
                        opt.innerText = cat;
                        catDropdown.appendChild(opt);
                    });
                }

                // 3. Render Dashboard / Main Ledger table
                const ledgerBody = document.getElementById('ledger-body');
                ledgerBody.innerHTML = '';
                
                if (!data.recentTransactions || data.recentTransactions.length === 0) {
                    ledgerBody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: var(--text-gray); padding: 40px 0;">No ledger operations found.</td></tr>`;
                } else {
                    data.recentTransactions.forEach(tx => {
                        const row = document.createElement('tr');
                        const d = new Date(tx.date);
                        const dateStr = d.toLocaleDateString('en-IN', {day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit'});
                        
                        let badgeClass = 'badge-expense';
                        if (tx.type === 'Income') badgeClass = 'badge-income';
                        else if (tx.type === 'Investment') badgeClass = 'badge-investment';

                        row.innerHTML = `
                            <td style="color: var(--text-gray); font-size: 11px;">${'$'}{dateStr}</td>
                            <td style="font-weight: 700;">${'$'}{tx.payer}</td>
                            <td>${'$'}{tx.category}</td>
                            <td style="color: var(--text-gray); max-width: 140px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">${'$'}{tx.notes}</td>
                            <td style="font-size: 11px; color: var(--text-gray);">${'$'}{tx.paymentMethod}</td>
                            <td><span class="badge ${'$'}{badgeClass}">${'$'}{tx.type}</span></td>
                            <td style="font-weight: 800; text-align: right; color: ${'$'}{tx.type === 'Income' ? 'var(--accent-green)' : 'var(--text-white)'}">
                                ${'$'}{formatCurrency(tx.amount)}
                            </td>
                            <td>
                                <button class="btn-delete" onclick="deleteDatabaseItem('transaction', '${'$'}{tx.id}')">🗑️</button>
                            </td>
                        `;
                        ledgerBody.appendChild(row);
                    });
                }

                // 4. Render Budgets List
                const budgetListBody = document.getElementById('budget-list-body');
                budgetListBody.innerHTML = '';
                if (!data.budgets || data.budgets.length === 0) {
                    budgetListBody.innerHTML = `<tr><td colspan="2" style="text-align: center; color: var(--text-gray); padding: 20px 0;">No budgets registered.</td></tr>`;
                } else {
                    data.budgets.forEach(b => {
                        const tr = document.createElement('tr');
                        tr.innerHTML = `
                            <td style="font-weight: bold; color: var(--text-white);">${'$'}{b.category}</td>
                            <td style="font-weight: 800; color: var(--teal-primary);">${'$'}{formatCurrency(b.allocatedLimit)}</td>
                        `;
                        budgetListBody.appendChild(tr);
                    });
                }

                // 5. Render Savings Goals
                const savingsContainer = document.getElementById('savings-goal-list');
                savingsContainer.innerHTML = '';
                if (!data.savingsGoals || data.savingsGoals.length === 0) {
                    savingsContainer.innerHTML = `<div style="grid-column: 1/-1; text-align: center; color: var(--text-gray); padding: 20px 0;">No savings targets mapped in DB.</div>`;
                } else {
                    data.savingsGoals.forEach(g => {
                        const pct = Math.min(100, Math.round((g.currentSaved / g.targetAmount) * 100)) || 0;
                        const div = document.createElement('div');
                        div.className = 'item-data-card';
                        div.innerHTML = `
                            <div style="display: flex; justify-content: space-between; align-items: start;">
                                <span style="font-weight: 800; color: var(--text-white); font-size: 13px;">${'$'}{g.name}</span>
                                <button class="btn-delete" onclick="deleteDatabaseItem('savings_goal', '${'$'}{g.id}')">🗑️</button>
                            </div>
                            <div style="display: flex; justify-content: space-between; font-size: 12px; font-weight: 700; color: var(--text-gray); margin-top: 4px;">
                                <span>Saved: <b style="color: var(--teal-primary);">${'$'}{formatCurrency(g.currentSaved)}</b></span>
                                <span>Target: <b>${'$'}{formatCurrency(g.targetAmount)}</b></span>
                            </div>
                            <div class="progress-track">
                                <div class="progress-bar" style="width: ${'$'}{pct}%;"></div>
                            </div>
                            <div style="display: flex; justify-content: space-between; font-size: 11px; color: var(--text-gray); margin-top: 2px;">
                                <span>Completion: <b>${'$'}{pct}%</b></span>
                                <span>Date: <b>${'$'}{g.targetDate}</b></span>
                            </div>
                            ${'$'}{g.isAutomated ? '<div style="font-size: 9px; font-weight: bold; color: var(--accent-green);">⚡ MICRO-SAVINGS ENABLED</div>' : ''}
                        `;
                        savingsContainer.appendChild(div);
                    });
                }

                // 6. Render People Ledger (Tab and Dashboard Widget)
                const peopleContainer = document.getElementById('people-list');
                const dashPeopleContainer = document.getElementById('dashboard-people-list');
                const dashPeopleCount = document.getElementById('dashboard-people-count');
                
                if (peopleContainer) peopleContainer.innerHTML = '';
                if (dashPeopleContainer) dashPeopleContainer.innerHTML = '';
                if (dashPeopleCount) dashPeopleCount.innerText = (data.people && data.people.length) ? data.people.length : 0;
                
                if (!data.people || data.people.length === 0) {
                    const fallbackHtml = `<div style="grid-column: 1/-1; text-align: center; color: var(--text-gray); padding: 40px 0;">No family borrow/lend entries.</div>`;
                    if (peopleContainer) peopleContainer.innerHTML = fallbackHtml;
                    if (dashPeopleContainer) dashPeopleContainer.innerHTML = fallbackHtml;
                } else {
                    data.people.forEach(p => {
                        const statusClass = p.status === 'Closed' ? 'badge-closed' : 'badge-pending';
                        const balanceColor = p.pendingBalance > 0 ? 'var(--accent-red)' : p.pendingBalance < 0 ? 'var(--accent-green)' : 'var(--text-white)';
                        const balanceText = p.pendingBalance > 0 ? `Needs to Pay Back: ${'$'}{formatCurrency(p.pendingBalance)}` : p.pendingBalance < 0 ? `Owes You: ${'$'}{formatCurrency(Math.abs(p.pendingBalance))}` : 'All Settled / Square';

                        const cardInnerHtml = `
                            <div style="display: flex; justify-content: space-between; align-items: start;">
                                <span style="font-weight: 800; color: var(--text-white); font-size: 13px;">👤 ${'$'}{p.name}</span>
                                <div style="display: flex; gap: 4px; align-items: center;">
                                    <span class="badge ${'$'}{statusClass}">${'$'}{p.status}</span>
                                    <button class="btn-delete" onclick="deleteDatabaseItem('person', '${'$'}{p.id}')">🗑️</button>
                                </div>
                            </div>
                            <div style="display: flex; justify-content: space-between; font-size: 11px; color: var(--text-gray); margin-top: 4px;">
                                <span>Lent Out: <b>${'$'}{formatCurrency(p.moneyGiven)}</b></span>
                                <span>Borrowed: <b>${'$'}{formatCurrency(p.moneyReceived)}</b></span>
                            </div>
                            <div style="font-size: 12px; font-weight: 800; color: ${'$'}{balanceColor}; margin-top: 6px;">
                                ${'$'}{balanceText}
                            </div>
                        `;

                        if (peopleContainer) {
                            const div = document.createElement('div');
                            div.className = 'item-data-card';
                            div.innerHTML = cardInnerHtml;
                            peopleContainer.appendChild(div);
                        }

                        if (dashPeopleContainer) {
                            const div = document.createElement('div');
                            div.className = 'item-data-card';
                            div.innerHTML = cardInnerHtml;
                            dashPeopleContainer.appendChild(div);
                        }
                    });
                }

                // 7. Render Loans
                const loansContainer = document.getElementById('loans-list');
                loansContainer.innerHTML = '';
                if (!data.loans || data.loans.length === 0) {
                    loansContainer.innerHTML = `<div style="grid-column: 1/-1; text-align: center; color: var(--text-gray); padding: 20px 0;">No loans registered in liability ledger.</div>`;
                } else {
                    data.loans.forEach(l => {
                        const pct = Math.min(100, Math.round((l.paidAmount / l.totalAmount) * 100)) || 0;
                        const div = document.createElement('div');
                        div.className = 'item-data-card';
                        div.innerHTML = `
                            <div style="display: flex; justify-content: space-between; align-items: start;">
                                <span style="font-weight: 800; color: var(--text-white); font-size: 13px;">${'$'}{l.name}</span>
                                <button class="btn-delete" onclick="deleteDatabaseItem('loan', '${'$'}{l.id}')">🗑️</button>
                            </div>
                            <div style="display: flex; justify-content: space-between; font-size: 11px; color: var(--text-gray); margin-top: 4px;">
                                <span>Paid: <b>${'$'}{formatCurrency(l.paidAmount)}</b></span>
                                <span>Principal: <b>${'$'}{formatCurrency(l.totalAmount)}</b></span>
                            </div>
                            <div class="progress-track">
                                <div class="progress-bar" style="width: ${'$'}{pct}%; background: linear-gradient(90deg, var(--accent-purple), var(--accent-red));"></div>
                            </div>
                            <div style="display: flex; justify-content: space-between; font-size: 11px; color: var(--text-gray); margin-top: 2px;">
                                <span>EMI: <b style="color: var(--accent-red);">${'$'}{formatCurrency(l.monthlyEmi)}/mo</b></span>
                                <span>Progress: <b>${'$'}{pct}%</b></span>
                            </div>
                            <div style="font-size: 11px; font-weight: bold; color: var(--teal-primary); margin-top: 4px;">
                                Outstanding Left: ${'$'}{formatCurrency(l.remainingAmount)} (Mature: ${'$'}{l.endDate})
                            </div>
                        `;
                        loansContainer.appendChild(div);
                    });
                }

                // 8. Render Insurances
                const insurancesContainer = document.getElementById('insurances-list');
                insurancesContainer.innerHTML = '';
                if (!data.insurance || data.insurance.length === 0) {
                    insurancesContainer.innerHTML = `<div style="grid-column: 1/-1; text-align: center; color: var(--text-gray); padding: 20px 0;">No insurance data listed.</div>`;
                } else {
                    data.insurance.forEach(ins => {
                        let statusClass = 'badge-active';
                        if (ins.status === 'Expired') statusClass = 'badge-due';
                        else if (ins.status === 'Near Expiry') statusClass = 'badge-pending';

                        const div = document.createElement('div');
                        div.className = 'item-data-card';
                        div.innerHTML = `
                            <div style="display: flex; justify-content: space-between; align-items: start;">
                                <span style="font-weight: 800; color: var(--text-white); font-size: 13px;">${'$'}{ins.policyName}</span>
                                <div style="display: flex; gap: 4px; align-items: center;">
                                    <span class="badge ${'$'}{statusClass}">${'$'}{ins.status}</span>
                                    <button class="btn-delete" onclick="deleteDatabaseItem('insurance', '${'$'}{ins.id}')">🗑️</button>
                                </div>
                            </div>
                            <div style="font-size: 12px; font-weight: bold; color: var(--text-white); margin-top: 4px;">
                                Company: <span style="color: var(--teal-primary);">${'$'}{ins.company}</span>
                            </div>
                            <div style="display: flex; justify-content: space-between; font-size: 11px; color: var(--text-gray); margin-top: 2px;">
                                <span>Premium: <b>${'$'}{formatCurrency(ins.premium)}/yr</b></span>
                                <span>Coverage: <b style="color: var(--accent-green);">${'$'}{formatCurrency(ins.coverage)}</b></span>
                            </div>
                            <div style="font-size: 11px; color: var(--text-gray); margin-top: 4px;">
                                Next Renewal: <b style="color: var(--accent-yellow);">${'$'}{ins.renewalDate}</b>
                            </div>
                        `;
                        insurancesContainer.appendChild(div);
                    });
                }

                // 9. Render Stocks Equity Portfolio
                const stocksContainer = document.getElementById('stocks-list');
                stocksContainer.innerHTML = '';
                if (!data.stocks || data.stocks.length === 0) {
                    stocksContainer.innerHTML = `<div style="grid-column: 1/-1; text-align: center; color: var(--text-gray); padding: 20px 0;">No equity purchases registered.</div>`;
                } else {
                    data.stocks.forEach(s => {
                        const isProfit = s.profitLoss >= 0;
                        const plColor = isProfit ? 'var(--accent-green)' : 'var(--accent-red)';
                        const plSign = isProfit ? '▲ +' : '▼ ';

                        const div = document.createElement('div');
                        div.className = 'item-data-card';
                        div.innerHTML = `
                            <div style="display: flex; justify-content: space-between; align-items: start;">
                                <div>
                                    <span style="font-weight: 800; color: var(--text-white); font-size: 13.sp;">${'$'}{s.stockName}</span>
                                    <span style="font-size: 9px; font-weight: bold; color: var(--teal-primary); display: block;">${'$'}{s.symbol}</span>
                                </div>
                                <button class="btn-delete" onclick="deleteDatabaseItem('stock', '${'$'}{s.id}')">🗑️</button>
                            </div>
                            <div style="display: flex; justify-content: space-between; font-size: 11px; color: var(--text-gray); margin-top: 4px;">
                                <span>Quantity: <b>${'$'}{s.quantity} units</b></span>
                                <span>Buy Price: <b>${'$'}{formatCurrency(s.buyPrice)}</b></span>
                            </div>
                            <div style="display: flex; justify-content: space-between; font-size: 11px; color: var(--text-gray);">
                                <span>Invested: <b>${'$'}{formatCurrency(s.investedAmount)}</b></span>
                                <span>Current Valuation: <b style="color: var(--text-white);">${'$'}{formatCurrency(s.currentValue)}</b></span>
                            </div>
                            <div style="font-size: 12px; font-weight: 800; color: ${'$'}{plColor}; margin-top: 6px; display: flex; justify-content: space-between;">
                                <span>PnL Total: ${'$'}{plSign}${'$'}{formatCurrency(s.profitLoss)}</span>
                                <span>(${'$'}{s.returnPercent.toFixed(2)}%)</span>
                            </div>
                            <div style="font-size: 10px; color: var(--text-gray); text-align: right;">Purchased: ${'$'}{s.buyDate}</div>
                        `;
                        stocksContainer.appendChild(div);
                    });
                }

                // 10. Render Credit Cards Outstanding List
                const cardsContainer = document.getElementById('cards-list');
                cardsContainer.innerHTML = '';
                if (!data.creditCards || data.creditCards.length === 0) {
                    cardsContainer.innerHTML = `<div style="grid-column: 1/-1; text-align: center; color: var(--text-gray); padding: 20px 0;">No credit card statements linked.</div>`;
                } else {
                    data.creditCards.forEach(c => {
                        const statusClass = c.status === 'Paid' ? 'badge-paid' : 'badge-due';
                        const balanceColor = c.pendingAmount > 0 ? 'var(--accent-red)' : 'var(--accent-green)';

                        const div = document.createElement('div');
                        div.className = 'item-data-card';
                        div.innerHTML = `
                            <div style="display: flex; justify-content: space-between; align-items: start;">
                                <span style="font-weight: 800; color: var(--text-white); font-size: 13.sp;">${'$'}{c.cardName}</span>
                                <div style="display: flex; gap: 4px; align-items: center;">
                                    <span class="badge ${'$'}{statusClass}">${'$'}{c.status}</span>
                                    <button class="btn-delete" onclick="deleteDatabaseItem('credit_card', '${'$'}{c.id}')">🗑️</button>
                                </div>
                            </div>
                            <div style="display: flex; justify-content: space-between; font-size: 11px; color: var(--text-gray); margin-top: 4px;">
                                <span>Paid Statement: <b>${'$'}{formatCurrency(c.paidAmount)}</b></span>
                                <span>Total Spent: <b>${'$'}{formatCurrency(c.totalSpend)}</b></span>
                            </div>
                            <div style="font-size: 12px; font-weight: 800; color: ${'$'}{balanceColor}; margin-top: 6px;">
                                Outstanding Due Balance: ${'$'}{formatCurrency(c.pendingAmount)}
                            </div>
                        `;
                        cardsContainer.appendChild(div);
                    });
                }

            } catch (e) {
                console.error("Vault terminal connection interrupted", e);
            }
        }

        // Generic central REST submit utility
        async function submitGenericForm(endpoint, payload, submitBtnId, successMsg, clearInputsCallback) {
            const btn = document.getElementById(submitBtnId);
            const origText = btn.innerHTML;
            btn.disabled = true;
            btn.innerText = 'Syncing secure ledger...';

            const token = localStorage.getItem("vault_auth_token") || "";

            try {
                const res = await fetch(`${'$'}{endpoint}?token=${'$'}{token}`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'x-auth-token': token,
                        'authorization': 'Bearer ' + token
                    },
                    body: JSON.stringify(payload)
                });
                
                if (res.status === 401) {
                    disconnectSession();
                    return;
                }

                const result = await res.json();
                if (result.success) {
                    showToast(successMsg);
                    clearInputsCallback();
                    fetchVaultRecords();
                } else {
                    alert('Sync Error: ' + result.error);
                }
            } catch (err) {
                alert('Connection timed out. Ensure the laptop is connected directly to the Android Wi-Fi node.');
                console.error(err);
            } finally {
                btn.disabled = false;
                btn.innerHTML = origText;
            }
        }

        // Form Submit Listeners
        
        // 1. Transaction Form
        document.getElementById('tx-form').addEventListener('submit', (e) => {
            e.preventDefault();
            const payload = {
                payer: document.getElementById('payer').value,
                type: document.getElementById('type').value,
                paymentMethod: document.getElementById('paymentMethod').value,
                category: document.getElementById('category').value,
                amount: parseFloat(document.getElementById('amount').value),
                autoSaveRule: document.getElementById('autoSaveRule').value,
                notes: document.getElementById('notes').value
            };
            submitGenericForm('/api/transactions', payload, 'submit-btn', '✔️ Direct entry logged directly in SQLite!', () => {
                document.getElementById('amount').value = '';
                document.getElementById('notes').value = '';
            });
        });

        // 2. Budget Form
        document.getElementById('budget-form').addEventListener('submit', (e) => {
            e.preventDefault();
            const payload = {
                category: document.getElementById('budget-category').value,
                allocatedLimit: parseFloat(document.getElementById('budget-limit').value)
            };
            submitGenericForm('/api/budgets', payload, 'budget-submit-btn', '✔️ Monthly budget updated successfully!', () => {
                document.getElementById('budget-limit').value = '';
            });
        });

        // 3. Savings Goal Form
        document.getElementById('savings-form').addEventListener('submit', (e) => {
            e.preventDefault();
            const payload = {
                name: document.getElementById('saving-name').value,
                targetAmount: parseFloat(document.getElementById('saving-target').value),
                currentSaved: parseFloat(document.getElementById('saving-current').value),
                targetDate: document.getElementById('saving-date').value,
                isAutomated: document.getElementById('saving-auto').checked
            };
            submitGenericForm('/api/savings_goals', payload, 'savings-submit-btn', '✔️ Savings goal added successfully!', () => {
                document.getElementById('saving-name').value = '';
                document.getElementById('saving-target').value = '';
                document.getElementById('saving-current').value = '';
                document.getElementById('saving-date').value = '';
                document.getElementById('saving-auto').checked = false;
            });
        });

        // 4. Family Lending/Borrowing Form
        document.getElementById('people-form').addEventListener('submit', (e) => {
            e.preventDefault();
            const payload = {
                name: document.getElementById('person-name').value,
                moneyGiven: parseFloat(document.getElementById('person-given').value) || 0.0,
                moneyReceived: parseFloat(document.getElementById('person-received').value) || 0.0,
                status: document.getElementById('person-status').value
            };
            submitGenericForm('/api/people', payload, 'people-submit-btn', '✔️ Repayment / Borrowing logged in Ledger!', () => {
                document.getElementById('person-name').value = '';
                document.getElementById('person-given').value = '';
                document.getElementById('person-received').value = '';
                document.getElementById('person-status').value = 'Pending';
            });
        });

        // 5. Loan Form
        document.getElementById('loans-form').addEventListener('submit', (e) => {
            e.preventDefault();
            const payload = {
                name: document.getElementById('loan-name').value,
                totalAmount: parseFloat(document.getElementById('loan-total').value),
                monthlyEmi: parseFloat(document.getElementById('loan-emi').value),
                paidAmount: parseFloat(document.getElementById('loan-paid').value),
                endDate: document.getElementById('loan-end').value
            };
            submitGenericForm('/api/loans', payload, 'loan-submit-btn', '✔️ Loan Liability ledger updated successfully!', () => {
                document.getElementById('loan-name').value = '';
                document.getElementById('loan-total').value = '';
                document.getElementById('loan-emi').value = '';
                document.getElementById('loan-paid').value = '';
                document.getElementById('loan-end').value = '';
            });
        });

        // 6. Insurance Form
        document.getElementById('insurance-form').addEventListener('submit', (e) => {
            e.preventDefault();
            const payload = {
                policyName: document.getElementById('ins-name').value,
                company: document.getElementById('ins-company').value,
                premium: parseFloat(document.getElementById('ins-premium').value),
                coverage: parseFloat(document.getElementById('ins-coverage').value),
                renewalDate: document.getElementById('ins-renewal').value,
                status: document.getElementById('ins-status').value
            };
            submitGenericForm('/api/insurance', payload, 'ins-submit-btn', '✔️ Protection policy registered successfully!', () => {
                document.getElementById('ins-name').value = '';
                document.getElementById('ins-company').value = '';
                document.getElementById('ins-premium').value = '';
                document.getElementById('ins-coverage').value = '';
                document.getElementById('ins-renewal').value = '';
                document.getElementById('ins-status').value = 'Active';
            });
        });

        // 7. Stocks Form
        document.getElementById('stock-form').addEventListener('submit', (e) => {
            e.preventDefault();
            const payload = {
                stockName: document.getElementById('stock-name').value,
                symbol: document.getElementById('stock-sym').value,
                quantity: parseInt(document.getElementById('stock-qty').value),
                buyPrice: parseFloat(document.getElementById('stock-price').value),
                currentPrice: parseFloat(document.getElementById('stock-curr').value),
                buyDate: document.getElementById('stock-date').value
            };
            submitGenericForm('/api/stocks', payload, 'stock-submit-btn', '✔️ Market investment synchronized securely!', () => {
                document.getElementById('stock-name').value = '';
                document.getElementById('stock-sym').value = '';
                document.getElementById('stock-qty').value = '';
                document.getElementById('stock-price').value = '';
                document.getElementById('stock-curr').value = '';
                document.getElementById('stock-date').value = '';
            });
        });

        // 8. Credit Card Form
        document.getElementById('card-form').addEventListener('submit', (e) => {
            e.preventDefault();
            const payload = {
                cardName: document.getElementById('card-name').value,
                totalSpend: parseFloat(document.getElementById('card-spend').value),
                paidAmount: parseFloat(document.getElementById('card-paid').value),
                status: document.getElementById('card-status').value
            };
            submitGenericForm('/api/credit_cards', payload, 'card-submit-btn', '✔️ Credit card billing balance updated!', () => {
                document.getElementById('card-name').value = '';
                document.getElementById('card-spend').value = '';
                document.getElementById('card-paid').value = '';
                document.getElementById('card-status').value = 'Due';
            });
        });
    </script>
</body>
</html>
""".trimIndent()
        
        val bytes = rawHtml.toByteArray(Charsets.UTF_8)
        out.write("HTTP/1.1 200 OK\r\n".toByteArray())
        out.write("Content-Type: text/html; charset=utf-8\r\n".toByteArray())
        out.write("Content-Length: ${bytes.size}\r\n".toByteArray())
        out.write("\r\n".toByteArray())
        out.write(bytes)
        out.flush()
    }

    private fun serve404(out: OutputStream) {
        val res = "HTTP/1.1 404 Not Found\r\nContent-Type: text/plain\r\nContent-Length: 9\r\n\r\nNot Found".toByteArray()
        out.write(res)
        out.flush()
    }

    private fun serve500(out: OutputStream, error: String) {
        val rStr = "Internal Server Error: $error"
        val bytes = rStr.toByteArray()
        out.write("HTTP/1.1 500 Internal Server Error\r\n".toByteArray())
        out.write("Content-Type: text/plain\r\n".toByteArray())
        out.write("Content-Length: ${bytes.size}\r\n".toByteArray())
        out.write("\r\n".toByteArray())
        out.write(bytes)
        out.flush()
    }
}
