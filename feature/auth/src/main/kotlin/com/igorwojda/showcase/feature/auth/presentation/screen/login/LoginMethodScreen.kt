package com.igorwojda.showcase.feature.auth.presentation.screen.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.LockClock
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igorwojda.showcase.feature.auth.domain.model.OAuthProvider
import com.igorwojda.showcase.feature.base.presentation.compose.composable.PitchBrandHeader
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaShapes
import org.koin.androidx.compose.koinViewModel

/** AUTH-01/S1: Login method selection with AUTH-05 deleted account & AUTH-06 session expired states. */
@Composable
fun LoginMethodScreen(
    onPhoneLoginClick: () -> Unit,
    onAuthSuccess: (isNewUser: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    sessionExpired: Boolean = false,
    viewModel: LoginMethodViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(sessionExpired) {
        viewModel.setSessionExpired(sessionExpired)
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(ArenaColors.CardBg)
                .navigationBarsPadding(),
    ) {
        PitchBrandHeader(
            title = "Tạo kèo, ghép đội, đá bóng dễ dàng.",
            subtitle = "Tìm đối thủ ngang trình độ, tuyển thêm người chơi và xây dựng uy tín cho đội của bạn.",
            showLogo = true,
        )

        LoginMethodBody(
            uiState = uiState,
            onGoogleClick = {
                viewModel.signInWithOAuth(
                    provider = OAuthProvider.GOOGLE,
                    oauthToken = "mock-google-token",
                    email = "an.nguyen@arenaflow.vn",
                    onAuthSuccess = onAuthSuccess,
                )
            },
            onFacebookClick = {
                viewModel.signInWithOAuth(
                    provider = OAuthProvider.FACEBOOK,
                    oauthToken = "mock-facebook-token",
                    email = "fb.player@arenaflow.vn",
                    onAuthSuccess = onAuthSuccess,
                )
            },
            onPhoneClick = onPhoneLoginClick,
            onDismissDeletedError = viewModel::clearError,
            modifier = Modifier.weight(1f),
        )
    }
}

@Suppress("LongParameterList")
@Composable
private fun LoginMethodBody(
    uiState: LoginMethodUiState,
    onGoogleClick: () -> Unit,
    onFacebookClick: () -> Unit,
    onPhoneClick: () -> Unit,
    onDismissDeletedError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (uiState.isSessionExpired) {
            SessionExpiredBanner()
        }

        if (uiState.isDeletedAccountError) {
            DeletedAccountNotice(onDismiss = onDismissDeletedError)
        } else if (uiState.errorMessage != null) {
            GeneralErrorBanner(message = uiState.errorMessage)
        }

        Text(
            text = "Đăng nhập hoặc đăng ký",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = ArenaColors.TextHeading,
        )
        Text(
            text = "Tài khoản mới sẽ được tạo tự động ở lần đăng nhập đầu tiên",
            style = MaterialTheme.typography.bodyMedium,
            color = ArenaColors.TextMuted,
        )

        Spacer(modifier = Modifier.height(4.dp))

        GoogleOAuthButton(
            isLoading = uiState.loadingProvider == OAuthProvider.GOOGLE,
            enabled = uiState.loadingProvider == null,
            onClick = onGoogleClick,
        )

        FacebookOAuthButton(
            isLoading = uiState.loadingProvider == OAuthProvider.FACEBOOK,
            enabled = uiState.loadingProvider == null,
            onClick = onFacebookClick,
        )

        PhoneLoginButton(
            enabled = uiState.loadingProvider == null,
            onClick = onPhoneClick,
        )
    }
}

@Composable
private fun SessionExpiredBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = ArenaShapes.Notice,
        color = ArenaColors.WarningBg,
        border = BorderStroke(1.dp, ArenaColors.WarningBorder),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = Icons.Outlined.LockClock,
                contentDescription = null,
                tint = ArenaColors.Warning,
                modifier = Modifier.size(20.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Phiên đăng nhập đã hết hạn",
                    style = MaterialTheme.typography.titleSmall,
                    color = ArenaColors.TextHeading,
                )
                Text(
                    text = "Token làm mới không còn hợp lệ. Vui lòng đăng nhập lại để tiếp tục.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ArenaColors.TextPrimary,
                )
            }
        }
    }
}

@Composable
private fun DeletedAccountNotice(onDismiss: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = ArenaShapes.Card,
        color = ArenaColors.ErrorBg,
        border = BorderStroke(1.dp, ArenaColors.ErrorBorder),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.PersonOff,
                    contentDescription = null,
                    tint = ArenaColors.Error,
                    modifier = Modifier.size(22.dp),
                )
                Text(
                    text = "Tài khoản đã bị xóa (AUTH_003)",
                    style = MaterialTheme.typography.titleSmall,
                    color = ArenaColors.Error,
                )
            }
            Text(
                text = "Tài khoản này đã được xóa vĩnh viễn và không thể khôi phục. Vui lòng chọn tài khoản khác.",
                style = MaterialTheme.typography.bodySmall,
                color = ArenaColors.TextPrimary,
            )
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = ArenaShapes.Button,
                border = BorderStroke(1.dp, ArenaColors.ErrorBorder),
            ) {
                Text(
                    text = "Chọn tài khoản khác",
                    style = MaterialTheme.typography.labelLarge,
                    color = ArenaColors.Error,
                )
            }
        }
    }
}

@Composable
private fun GeneralErrorBanner(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = ArenaShapes.Notice,
        color = ArenaColors.ErrorBg,
        border = BorderStroke(1.dp, ArenaColors.ErrorBorder),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = ArenaColors.ErrorBright,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = ArenaColors.Error,
            )
        }
    }
}

@Composable
private fun GoogleOAuthButton(
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        shape = ArenaShapes.Button,
        border = BorderStroke(1.dp, ArenaColors.BorderSubtle),
        colors =
            ButtonDefaults.outlinedButtonColors(
                containerColor = ArenaColors.CardBg,
                contentColor = ArenaColors.TextHeading,
            ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = "Tiếp tục với Google",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun FacebookOAuthButton(
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        shape = ArenaShapes.Button,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = ArenaColors.FacebookBlue,
                contentColor = Color.White,
            ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = Color.White,
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = "Tiếp tục với Facebook",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun PhoneLoginButton(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        shape = ArenaShapes.Button,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = ArenaColors.Primary,
                contentColor = ArenaColors.OnPrimary,
            ),
    ) {
        Icon(
            imageVector = Icons.Outlined.Call,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Tiếp tục bằng số điện thoại",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
