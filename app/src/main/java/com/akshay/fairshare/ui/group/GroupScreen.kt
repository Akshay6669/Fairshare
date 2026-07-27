package com.akshay.fairshare.ui.group

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.akshay.fairshare.domain.model.Balance
import com.akshay.fairshare.domain.model.Group
import com.akshay.fairshare.domain.model.Settlement

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScreen(viewModel: GroupViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text(state.group?.name ?: "FairShare") }) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            state.error?.let { message ->
                item { Text(message, style = MaterialTheme.typography.bodyMedium) }
            }

            item { Text("Balances", style = MaterialTheme.typography.titleMedium) }
            items(state.balances, key = { it.memberId }) { balance ->
                BalanceRow(balance, state.group)
            }

            item { HorizontalDivider() }
            item { Text("Settle up", style = MaterialTheme.typography.titleMedium) }

            if (state.settlements.isEmpty()) {
                item { Text("Everyone's square.", style = MaterialTheme.typography.bodyMedium) }
            } else {
                items(state.settlements) { settlement ->
                    SettlementRow(settlement, state.group)
                }
            }
        }
    }
}

@Composable
private fun BalanceRow(balance: Balance, group: Group?) {
    val name = group?.members?.firstOrNull { it.id == balance.memberId }?.name ?: balance.memberId
    val detail = when {
        balance.net.isZero -> "settled"
        balance.net.isPositive -> "is owed ${balance.net.format()}"
        else -> "owes ${balance.net.absolute.format()}"
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(name, style = MaterialTheme.typography.bodyLarge)
        Text(detail, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SettlementRow(settlement: Settlement, group: Group?) {
    fun nameOf(id: String) = group?.members?.firstOrNull { it.id == id }?.name ?: id
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "${nameOf(settlement.fromMemberId)} pays ${nameOf(settlement.toMemberId)}",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(settlement.amount.format(), style = MaterialTheme.typography.headlineSmall)
        }
    }
}
