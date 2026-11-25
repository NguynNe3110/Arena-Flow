@file:Suppress("LongMethod")

package com.igorwojda.showcase.app.presentation.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igorwojda.showcase.feature.base.domain.model.ArenaSessionState
import com.igorwojda.showcase.feature.base.domain.model.PersonaRole
import com.igorwojda.showcase.feature.base.domain.model.SimulatedError
import com.igorwojda.showcase.feature.base.domain.repository.SessionRepository
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppFilterChip
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppSecondaryButton
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

const val IS_DEMO_CONTROLS_AVAILABLE: Boolean = true

/** Debug-only Demo Control Panel for switching personas, simulated errors, and resetting fixtures. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DebugDemoControlOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!visible) return

    val sessionRepository: SessionRepository = koinInject()
    val sessionState by sessionRepository.observeSession().collectAsStateWithLifecycle(ArenaSessionState())
    val scope = rememberCoroutineScope()
    var selectedPersona by rememberSaveable { mutableStateOf<PersonaRole?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        containerColor = ArenaColors.CardBg,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Bảng điều khiển Demo (Debug)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ArenaColors.TextPrimary,
            )

            Text(
                text = "Chuyển vai trò (Persona)",
                style = MaterialTheme.typography.labelLarge,
                color = ArenaColors.TextMuted,
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PersonaRole.entries.forEach { role ->
                    AppFilterChip(
                        text = role.label,
                        selected = selectedPersona == role,
                        onClick = {
                            selectedPersona = role
                            scope.launch { sessionRepository.switchPersona(role) }
                        },
                    )
                }
            }

            Text(
                text = "Giả lập lỗi API",
                style = MaterialTheme.typography.labelLarge,
                color = ArenaColors.TextMuted,
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SimulatedError.entries.forEach { errorMode ->
                    AppFilterChip(
                        text = errorMode.label,
                        selected = sessionState.simulatedError == errorMode,
                        onClick = { scope.launch { sessionRepository.updateDemoControls(simulatedError = errorMode) } },
                    )
                }
            }

            AppSecondaryButton(
                text = "Khôi phục dữ liệu mẫu",
                onClick = {
                    scope.launch {
                        sessionRepository.resetDemoData()
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            AppButton(
                text = "Đóng",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}
