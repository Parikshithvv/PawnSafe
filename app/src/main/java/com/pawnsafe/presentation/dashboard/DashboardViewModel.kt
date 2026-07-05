package com.pawnsafe.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pawnsafe.BuildConfig
import com.pawnsafe.data.remote.GeminiClient
import com.pawnsafe.data.remote.GeminiContent
import com.pawnsafe.data.remote.GeminiPart
import com.pawnsafe.data.remote.GeminiRequest
import com.pawnsafe.domain.model.Pledge
import com.pawnsafe.domain.repository.IPledgeRepository
import com.pawnsafe.domain.repository.IRedemptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class MonthStat(
    val month: String,
    val pledgeCount: Int,
    val totalLoan: Double,
    val pledges: List<Pledge>
)

data class DashboardData(
    val totalActive: Int,
    val totalLoanOut: Double,
    val overdueCount: Int,
    val totalRedeemed: Int,
    val totalRedeemedAmount: Double,
    val monthStats: List<MonthStat>
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val pledgeRepository: IPledgeRepository,
    private val redemptionRepository: IRedemptionRepository
) : ViewModel() {

    private val _dashboardData = MutableStateFlow<DashboardData?>(null)
    val dashboardData: StateFlow<DashboardData?> = _dashboardData

    private val _aiInsight = MutableStateFlow<String?>(null)
    val aiInsight: StateFlow<String?> = _aiInsight

    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading

    private val _selectedMonth = MutableStateFlow<MonthStat?>(null)
    val selectedMonth: StateFlow<MonthStat?> = _selectedMonth

    init { loadDashboard() }

    fun loadDashboard() {
        viewModelScope.launch {
            val pledges     = pledgeRepository.getAllPledges().first()
            val redemptions = redemptionRepository.getAllRedemptions().first()

            val active   = pledges.filter { it.status == "ACTIVE" }
            val overdue  = pledges.filter { it.status == "OVERDUE" }
            val redeemed = pledges.filter { it.status == "REDEEMED" }

            val totalLoanOut     = active.sumOf { it.loanAmountRs.toDoubleOrNull() ?: 0.0 } +
                                   overdue.sumOf { it.loanAmountRs.toDoubleOrNull() ?: 0.0 }
            val totalRedeemedAmt = redemptions.sumOf { it.totalAmount }

            val fmt = DateTimeFormatter.ofPattern("yyyy-MM")
            val grouped = pledges
                .groupBy {
                    try { LocalDate.parse(it.date).format(fmt) } catch (e: Exception) { "Unknown" }
                }
                .entries
                .sortedBy { it.key }
                .takeLast(6)
                .map { (month, list) ->
                    MonthStat(
                        month       = month,
                        pledgeCount = list.size,
                        totalLoan   = list.sumOf { it.loanAmountRs.toDoubleOrNull() ?: 0.0 },
                        pledges     = list
                    )
                }

            _dashboardData.value = DashboardData(
                totalActive         = active.size,
                totalLoanOut        = totalLoanOut,
                overdueCount        = overdue.size,
                totalRedeemed       = redeemed.size,
                totalRedeemedAmount = totalRedeemedAmt,
                monthStats          = grouped
            )
        }
    }

    fun onMonthSelected(stat: MonthStat?) {
        _selectedMonth.value = if (_selectedMonth.value?.month == stat?.month) null else stat
    }

    fun fetchAiInsight() {
        val data = _dashboardData.value ?: return
        _aiLoading.value = true
        _aiInsight.value = null
        viewModelScope.launch {
            try {
                val prompt = """
                    You are a financial assistant for a pawn shop called Sri Nanjundeshwara Jewellers.
                    Analyze this business data and give 3-4 short, actionable insights in simple English.
                    Be specific with numbers. Keep each point under 2 lines.

                    Data:
                    - Active pledges: ${data.totalActive}
                    - Overdue pledges: ${data.overdueCount}
                    - Total loan amount outstanding: Rs. ${"%.0f".format(data.totalLoanOut)}
                    - Total redeemed this period: ${data.totalRedeemed} pledges worth Rs. ${"%.0f".format(data.totalRedeemedAmount)}
                    - Monthly breakdown (last 6 months): ${data.monthStats.joinToString { "${it.month}: ${it.pledgeCount} pledges, Rs.${"%.0f".format(it.totalLoan)}" }}
                """.trimIndent()

                val response = GeminiClient.service.generate(
                    apiKey  = BuildConfig.GEMINI_API_KEY,
                    request = GeminiRequest(
                        contents = listOf(GeminiContent(parts = listOf(GeminiPart(prompt))))
                    )
                )
                _aiInsight.value = response.candidates
                    ?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "No insights available."
            } catch (e: Exception) {
                _aiInsight.value = "Could not fetch insights: ${e.message}"
            } finally {
                _aiLoading.value = false
            }
        }
    }
}