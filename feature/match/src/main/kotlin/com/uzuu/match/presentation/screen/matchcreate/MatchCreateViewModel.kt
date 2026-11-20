package com.uzuu.match.presentation.screen.matchcreate

import androidx.lifecycle.ViewModel
import com.uzuu.match.domain.model.MatchDraft
import com.uzuu.match.domain.model.MatchType
import com.uzuu.match.domain.model.MatchVenue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class MatchCreateUiState(
    val draft: MatchDraft = MatchDraft(),
    val availableTeams: List<Pair<String, String>> = listOf(
        "team-alpha" to "Bình Minh FC",
        "team-phoenix" to "Phượng Hoàng FC",
        "team-saigon" to "Sài Gòn United",
    ),
    val availableVenues: List<MatchVenue> = listOf(
        MatchVenue("ven-anphu", "Sân bóng An Phú", "Số 8 Đường số 2, Phường An Khánh, TP. Thủ Đức", "ACTIVE"),
        MatchVenue("ven-binhtrung", "Sân bóng Bình Trưng", "12 Đỗ Xuân Hợp, Phường Bình Trưng Tây, TP. Thủ Đức", "ACTIVE"),
        MatchVenue("ven-thaodien", "Sân bóng Thảo Điền", "28 Thảo Điền, P. Thảo Điền, TP. Thủ Đức", "ACTIVE"),
        MatchVenue("ven-chienthang", "Sân bóng Chiến Thắng", "102 Ung Văn Khiêm, P. 25, Q. Bình Thạnh", "ACTIVE"),
    ),
    val availableTimeSlots: List<String> = listOf(
        "17:30 - 19:00",
        "19:00 - 20:30",
        "20:30 - 22:00",
    ),
    val errors: Map<String, String> = emptyMap(),
)

internal class MatchCreateViewModel(
    private val matchDraftHolder: MatchDraftHolder,
) : ViewModel() {
    private val mutableState = MutableStateFlow(MatchCreateUiState(draft = matchDraftHolder.draftFlow.value))
    val uiState = mutableState.asStateFlow()

    fun updateDraft(transform: (MatchDraft) -> MatchDraft) {
        val newDraft = transform(mutableState.value.draft)
        mutableState.value = mutableState.value.copy(draft = newDraft, errors = emptyMap())
        matchDraftHolder.updateDraft(newDraft)
    }

    fun validateAndProceed(onValid: () -> Unit) {
        val draft = mutableState.value.draft
        val errors = mutableMapOf<String, String>()

        if (draft.kind == MatchType.HAS_VENUE) {
            if (draft.venueId.isNullOrBlank()) {
                errors["venue"] = "Vui lòng chọn sân thi đấu"
            }
            if (draft.startTime >= draft.endTime) {
                errors["time"] = "Giờ kết thúc phải sau giờ bắt đầu"
            }
        } else {
            if (draft.proposedVenues.isEmpty()) {
                errors["venues"] = "Vui lòng chọn ít nhất 1 sân đề xuất"
            } else if (draft.proposedVenues.size > 5) {
                errors["venues"] = "Tối đa 5 địa điểm đề xuất"
            }
            if (draft.proposedTimeSlots.isEmpty()) {
                errors["slots"] = "Vui lòng chọn ít nhất 1 khung giờ đề xuất"
            } else if (draft.proposedTimeSlots.size > 5) {
                errors["slots"] = "Tối đa 5 khung giờ đề xuất"
            }
        }

        if (errors.isNotEmpty()) {
            mutableState.value = mutableState.value.copy(errors = errors)
        } else {
            matchDraftHolder.updateDraft(draft)
            onValid()
        }
    }
}
