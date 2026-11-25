@file:Suppress("LongMethod")

package com.igorwojda.showcase.feature.base.presentation.compose.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.igorwojda.showcase.feature.base.domain.model.ActingTeamOption
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaShapes

/** TEAM-14 reusable acting-team selector sheet for multi-team captains. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActingTeamSelectorSheet(
    teams: List<ActingTeamOption>,
    selectedTeamId: String?,
    onSelectTeam: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        containerColor = ArenaColors.CardBg,
        shape = ArenaShapes.Sheet,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Chọn đội đang thao tác",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ArenaColors.TextHeading,
            )
            Text(
                text = "Quyền và lịch sử hành động sẽ gắn với đội bạn chọn.",
                style = MaterialTheme.typography.bodySmall,
                color = ArenaColors.TextMuted,
            )

            teams.forEach { team ->
                val isSelected = team.id == selectedTeamId
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .clickable {
                                onSelectTeam(team.id)
                                onDismiss()
                            }.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TeamAvatar(name = team.name, size = 40.dp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = team.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = ArenaColors.TextHeading,
                        )
                        Text(
                            text = "${team.wardName} · Uy tín ${team.reputationScore}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ArenaColors.TextMuted,
                        )
                    }
                    RadioButton(
                        selected = isSelected,
                        onClick = {
                            onSelectTeam(team.id)
                            onDismiss()
                        },
                        colors = RadioButtonDefaults.colors(selectedColor = ArenaColors.Primary),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
