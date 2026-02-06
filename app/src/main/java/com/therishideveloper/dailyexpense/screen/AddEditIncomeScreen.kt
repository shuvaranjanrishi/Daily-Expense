package com.therishideveloper.dailyexpense.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.therishideveloper.dailyexpense.R
import com.therishideveloper.dailyexpense.component.LoadingDialog
import com.therishideveloper.dailyexpense.component.TransactionEntryBase
import com.therishideveloper.dailyexpense.component.showToast
import com.therishideveloper.dailyexpense.data.model.TransactionType
import com.therishideveloper.dailyexpense.data.model.UiEvent
import com.therishideveloper.dailyexpense.viewmodel.TransactionViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AddEditIncomeScreen(
    transactionId: Int? = null,
    onBack: () -> Unit,
    viewModel: TransactionViewModel
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.Loading -> isLoading = true
                is UiEvent.Success -> {
                    isLoading = false
                    showToast(context, event.successMsg)
                    onBack()
                }

                is UiEvent.Error -> {
                    isLoading = false
                    showToast(context, event.errorMsg)
                }
            }
        }
    }

    if (isLoading) {
        LoadingDialog(message = stringResource(R.string.label_saving_data))
    }

    TransactionEntryBase(
        transactionId = transactionId,
        transactionType = TransactionType.INCOME,
        viewModel = viewModel,
        onBack = onBack
    )
}
