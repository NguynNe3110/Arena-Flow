@file:Suppress("LongParameterList")

package com.igorwojda.showcase.feature.auth.presentation.screen.otp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igorwojda.showcase.feature.base.presentation.compose.composable.PitchBrandHeader
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaShapes
import org.koin.androidx.compose.koinViewModel

private const val OTP_LENGTH = 6

/** AUTH-03/S2b + AUTH-04: 6-slot OTP verification screen with countdown timer and post-auth routing. */
@Composable
fun OtpVerifyScreen(
    phone: String,
    expiresInSeconds: Int,
    onBackClick: () -> Unit,
    onChangePhoneClick: () -> Unit,
    onAuthSuccess: (isNewUser: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OtpVerifyViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(phone, expiresInSeconds) {
        viewModel.initialize(phone = phone, expiresInSeconds = expiresInSeconds)
    }

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

        OtpVerifyBody(
            uiState = uiState,
            onOtpChange = viewModel::onOtpChanged,
            onChangePhoneClick = onChangePhoneClick,
            onResendClick = viewModel::resendOtp,
            onVerifyClick = { viewModel.verifyOtp(onAuthSuccess) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Suppress("LongParameterList")
@Composable
private fun OtpVerifyBody(
    uiState: OtpVerifyUiState,
    onOtpChange: (String) -> Unit,
    onChangePhoneClick: () -> Unit,
    onResendClick: () -> Unit,
    onVerifyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Nhập mã xác thực",
            style = MaterialTheme.typography.headlineLarge,
            color = ArenaColors.TextHeading,
        )

        PhoneSummaryRow(
            phone = uiState.phone,
            onChangePhoneClick = onChangePhoneClick,
        )

        OtpSlotsInput(
            otpCode = uiState.otpCode,
            hasError = uiState.errorMessage != null,
            onOtpChange = onOtpChange,
        )

        OtpErrorSection(
            errorMessage = uiState.errorMessage,
            isDeletedAccountError = uiState.isDeletedAccountError,
        )

        ResendCountdownRow(
            remainingSeconds = uiState.remainingSeconds,
            canResend = uiState.canResend,
            onResendClick = onResendClick,
        )

        Button(
            onClick = onVerifyClick,
            enabled = uiState.otpCode.length == OTP_LENGTH && !uiState.isSubmitting,
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
                text = "Xác nhận",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun PhoneSummaryRow(
    phone: String,
    onChangePhoneClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Mã gồm 6 chữ số đã được gửi tới số $phone",
            style = MaterialTheme.typography.bodyMedium,
            color = ArenaColors.TextMuted,
        )
        TextButton(
            onClick = onChangePhoneClick,
            modifier = Modifier.heightIn(min = 48.dp),
        ) {
            Text(
                text = "Đổi số điện thoại",
                style = MaterialTheme.typography.labelLarge,
                color = ArenaColors.PitchGreen,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun OtpSlotsInput(
    otpCode: String,
    hasError: Boolean,
    onOtpChange: (String) -> Unit,
) {
    BasicTextField(
        value = otpCode,
        onValueChange = onOtpChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        modifier =
            Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Mã OTP 6 chữ số" },
        decorationBox = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                repeat(OTP_LENGTH) { index ->
                    val char = otpCode.getOrNull(index)?.toString().orEmpty()
                    val isFocused = index == otpCode.length.coerceAtMost(OTP_LENGTH - 1)
                    val borderColor =
                        when {
                            hasError -> ArenaColors.Error
                            isFocused -> ArenaColors.Primary
                            else -> ArenaColors.Border
                        }
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(ArenaShapes.Input)
                                .background(ArenaColors.CardBg)
                                .border(2.dp, borderColor, ArenaShapes.Input),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = char,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = ArenaColors.TextHeading,
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun OtpErrorSection(
    errorMessage: String?,
    isDeletedAccountError: Boolean,
) {
    if (errorMessage == null) return
    if (isDeletedAccountError) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = ArenaShapes.Card,
            color = ArenaColors.ErrorBg,
            border = BorderStroke(1.dp, ArenaColors.ErrorBorder),
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = Icons.Outlined.PersonOff,
                    contentDescription = null,
                    tint = ArenaColors.Error,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = "Tài khoản này đã bị xóa vĩnh viễn và không thể khôi phục (AUTH_003).",
                    style = MaterialTheme.typography.bodySmall,
                    color = ArenaColors.Error,
                )
            }
        }
    } else {
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
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = ArenaColors.Error,
            )
        }
    }
}

@Composable
private fun ResendCountdownRow(
    remainingSeconds: Int,
    canResend: Boolean,
    onResendClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Chưa nhận được mã?",
            style = MaterialTheme.typography.bodySmall,
            color = ArenaColors.TextMuted,
        )
        TextButton(
            onClick = onResendClick,
            enabled = canResend,
            modifier = Modifier.heightIn(min = 48.dp),
        ) {
            Text(
                text = if (remainingSeconds > 0) "Gửi lại sau ${remainingSeconds}s" else "Gửi lại mã",
                style = MaterialTheme.typography.labelLarge,
                color = if (canResend) ArenaColors.Primary else ArenaColors.TextMuted,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
