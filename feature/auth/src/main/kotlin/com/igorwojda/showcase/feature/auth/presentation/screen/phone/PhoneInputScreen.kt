package com.igorwojda.showcase.feature.auth.presentation.screen.phone

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igorwojda.showcase.feature.base.presentation.compose.composable.InlineNotice
import com.igorwojda.showcase.feature.base.presentation.compose.composable.PitchBrandHeader
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaShapes
import org.koin.androidx.compose.koinViewModel

/** AUTH-02/S2a: Phone number input screen with `+84`/`0` normalization and OTP request. */
@Composable
fun PhoneInputScreen(
    onBackClick: () -> Unit,
    onOtpSend: (phone: String, expiresInSeconds: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PhoneInputViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(ArenaColors.CardBg)
                .navigationBarsPadding()
                .imePadding(),
    ) {
        PitchBrandHeader(
            compact = true,
            showLogo = true,
            showBack = true,
            onBackClick = onBackClick,
        )

        PhoneInputBody(
            uiState = uiState,
            onPhoneChange = viewModel::onPhoneChanged,
            onSubmit = { viewModel.submitPhone(onOtpSend) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PhoneInputBody(
    uiState: PhoneInputUiState,
    onPhoneChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
    ) {
        Text(
            text = "Nhập số điện thoại",
            style = MaterialTheme.typography.headlineLarge,
            color = ArenaColors.TextHeading,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Chúng tôi sẽ gửi mã xác thực gồm 6 chữ số tới số này",
            style = MaterialTheme.typography.bodyMedium,
            color = ArenaColors.TextMuted,
        )
        Spacer(modifier = Modifier.height(24.dp))

        PhoneField(
            value = uiState.phoneInput,
            error = uiState.phoneError,
            onValueChange = onPhoneChange,
            onDone = onSubmit,
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onSubmit,
            enabled = !uiState.isSubmitting,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            shape = ArenaShapes.Button,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = ArenaColors.Primary,
                    contentColor = ArenaColors.OnPrimary,
                ),
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = Color.White,
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = "Gửi mã xác thực",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(modifier = Modifier.weight(1f, fill = true))
        Spacer(modifier = Modifier.height(24.dp))

        InlineNotice(
            text = "Bằng việc tiếp tục, bạn đồng ý với Điều khoản sử dụng và Chính sách bảo mật của ArenaFlow.",
        )
    }
}

@Composable
private fun PhoneField(
    value: String,
    error: String?,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Số điện thoại",
            style = MaterialTheme.typography.labelMedium,
            color = ArenaColors.TextPrimary,
            fontWeight = FontWeight.SemiBold,
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("Ví dụ: 0987654321 hoặc +84987654321") },
            singleLine = true,
            isError = error != null,
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done,
                ),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            shape = ArenaShapes.Input,
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ArenaColors.Primary,
                    unfocusedBorderColor = ArenaColors.Border,
                    errorBorderColor = ArenaColors.Error,
                ),
            modifier = Modifier.fillMaxWidth(),
        )
        if (error != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    tint = ArenaColors.Error,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = ArenaColors.Error,
                )
            }
        }
    }
}
