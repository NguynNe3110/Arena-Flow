@file:Suppress(
    "CognitiveComplexMethod",
    "CyclomaticComplexMethod",
    "LabeledExpression",
    "LongMethod",
    "MagicNumber",
    "ReturnCount",
    "StringLiteralDuplication",
    "TooManyFunctions",
)

package com.igorwojda.showcase.feature.pairing.data.datasource.api

import com.igorwojda.showcase.feature.base.common.time.VietnamDateTime
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.base.data.store.ArenaInMemoryStore
import com.igorwojda.showcase.feature.base.data.store.ArenaStoreSnapshot
import com.igorwojda.showcase.feature.base.data.store.StorePairing
import com.igorwojda.showcase.feature.pairing.data.datasource.api.model.AddPairingOptionRequestDto
import com.igorwojda.showcase.feature.pairing.data.datasource.api.model.CancelPairingRequestDto
import com.igorwojda.showcase.feature.pairing.data.datasource.api.model.ConfirmPairingRequestDto
import com.igorwojda.showcase.feature.pairing.data.datasource.api.model.DisputePairingRequestDto
import com.igorwojda.showcase.feature.pairing.data.datasource.api.model.PairingCaptainDto
import com.igorwojda.showcase.feature.pairing.data.datasource.api.model.PairingOptionDto
import com.igorwojda.showcase.feature.pairing.data.datasource.api.model.PairingRoomDto
import com.igorwojda.showcase.feature.pairing.data.datasource.api.service.PairingRetrofitService
import java.util.UUID

/**
 * Debug-only mock implementation of [PairingRetrofitService] backed by [ArenaInMemoryStore].
 */
internal class MockPairingRetrofitService(
    private val store: ArenaInMemoryStore,
) : PairingRetrofitService {
    private val roomOptionsMap = mutableMapOf<String, MutableList<PairingOptionDto>>()
    private val roomMetaMap = mutableMapOf<String, RoomMeta>()

    private data class RoomMeta(
        val selectedByAOptionId: String? = null,
        val selectedByBOptionId: String? = null,
        val disputeReason: String? = null,
        val cancelReason: String? = null,
    )

    override suspend fun getPairingRoom(roomId: String): ApiResult<ApiEnvelope<PairingRoomDto>> =
        store.executeApi(path = "/api/v1/pairing-rooms/$roomId") { snapshot ->
            val pairing =
                snapshot.pairings.firstOrNull { it.id == roomId }
                    ?: return@executeApi notFoundError(roomId)
            val resolved = checkDeadlineTransition(snapshot, pairing)
            ApiResult.Success(ApiEnvelope(buildRoomDto(store.current, resolved)))
        }

    override suspend fun addPairingOption(
        roomId: String,
        request: AddPairingOptionRequestDto,
    ): ApiResult<ApiEnvelope<PairingRoomDto>> =
        store.executeApi(path = "/api/v1/pairing-rooms/$roomId/options") { snapshot ->
            val pairing =
                snapshot.pairings.firstOrNull { it.id == roomId }
                    ?: return@executeApi notFoundError(roomId)
            val current = checkDeadlineTransition(snapshot, pairing)
            if (current.status != STATUS_NEGOTIATING) {
                return@executeApi notNegotiatingError(roomId)
            }
            val venue = snapshot.venues.firstOrNull { it.id == request.venueId }
            if (venue?.status == VENUE_INACTIVE) {
                return@executeApi inactiveVenueError(roomId)
            }
            val role = resolveRole(snapshot, current)
            val proposerTeamId =
                if (role == ROLE_CAPTAIN_A) current.hostTeamId else current.guestTeamId
            val proposerTeamName =
                snapshot.teams.firstOrNull { it.id == proposerTeamId }?.name ?: "Đội bóng"
            val options = ensureOptions(snapshot, current)
            val newOption =
                PairingOptionDto(
                    optionId = "opt-${UUID.randomUUID().toString().take(8)}",
                    venueId = request.venueId,
                    venueName = request.venueName,
                    venueAddress = request.venueAddress,
                    venueStatus = venue?.status ?: VENUE_ACTIVE,
                    dateTimeLabel = request.dateTimeLabel,
                    feeSplitLabel = request.feeSplitLabel,
                    totalCost = request.totalCost,
                    depositAmount = request.depositAmount,
                    depositHolder = request.depositHolder,
                    proposedByTeamId = proposerTeamId,
                    proposedByTeamName = proposerTeamName,
                    isSelectedByA = role == ROLE_CAPTAIN_A,
                    isSelectedByB = role == ROLE_CAPTAIN_B,
                )
            options.add(newOption)
            updateSelectionMeta(roomId, role, newOption.optionId)
            updateStorePairing(roomId) {
                it.copy(
                    venueId = newOption.venueId,
                    timeSlot = newOption.dateTimeLabel,
                    totalCost = newOption.totalCost,
                    depositAmount = newOption.depositAmount,
                    depositHolder = newOption.depositHolder,
                    revision = it.revision + 1,
                    hostConfirmed = false,
                    guestConfirmed = false,
                )
            }
            val updatedPairing = store.current.pairings.first { it.id == roomId }
            ApiResult.Success(ApiEnvelope(buildRoomDto(store.current, updatedPairing)))
        }

    override suspend fun selectPairingOption(
        roomId: String,
        optionId: String,
    ): ApiResult<ApiEnvelope<PairingRoomDto>> =
        store.executeApi(path = "/api/v1/pairing-rooms/$roomId/options/$optionId/select") { snapshot ->
            val pairing =
                snapshot.pairings.firstOrNull { it.id == roomId }
                    ?: return@executeApi notFoundError(roomId)
            val current = checkDeadlineTransition(snapshot, pairing)
            if (current.status != STATUS_NEGOTIATING) {
                return@executeApi notNegotiatingError(roomId)
            }
            val options = ensureOptions(snapshot, current)
            val targetOption =
                options.firstOrNull { it.optionId == optionId }
                    ?: return@executeApi store.apiError(422, "PAIRING_008", "Phương án không thuộc kèo này.")
            if (targetOption.venueStatus == VENUE_INACTIVE) {
                return@executeApi inactiveVenueError(roomId)
            }
            val role = resolveRole(snapshot, current)
            updateSelectionMeta(roomId, role, optionId)
            val meta = roomMetaMap[roomId] ?: RoomMeta()
            val updatedOptions =
                options.map { opt ->
                    opt.copy(
                        isSelectedByA = opt.optionId == meta.selectedByAOptionId,
                        isSelectedByB = opt.optionId == meta.selectedByBOptionId,
                    )
                }
            options.clear()
            options.addAll(updatedOptions)
            val changed =
                current.venueId != targetOption.venueId || current.timeSlot != targetOption.dateTimeLabel
            updateStorePairing(roomId) {
                it.copy(
                    venueId = targetOption.venueId,
                    timeSlot = targetOption.dateTimeLabel,
                    totalCost = targetOption.totalCost,
                    depositAmount = targetOption.depositAmount,
                    depositHolder = targetOption.depositHolder,
                    revision = if (changed) it.revision + 1 else it.revision,
                    hostConfirmed = if (changed) false else it.hostConfirmed,
                    guestConfirmed = if (changed) false else it.guestConfirmed,
                )
            }
            val updatedPairing = store.current.pairings.first { it.id == roomId }
            ApiResult.Success(ApiEnvelope(buildRoomDto(store.current, updatedPairing)))
        }

    override suspend fun confirmPairing(
        roomId: String,
        request: ConfirmPairingRequestDto,
    ): ApiResult<ApiEnvelope<PairingRoomDto>> =
        store.executeApi(path = "/api/v1/pairing-rooms/$roomId/confirm") { snapshot ->
            val pairing =
                snapshot.pairings.firstOrNull { it.id == roomId }
                    ?: return@executeApi notFoundError(roomId)
            val current = checkDeadlineTransition(snapshot, pairing)
            if (current.status != STATUS_NEGOTIATING) {
                return@executeApi notNegotiatingError(roomId)
            }
            if (request.revision != current.revision) {
                return@executeApi store.apiError(
                    409,
                    "PAIRING_006",
                    "Phương án đã thay đổi, vui lòng tải lại.",
                )
            }
            val options = ensureOptions(snapshot, current)
            val selectedOpt =
                options.firstOrNull { it.optionId == request.optionId }
                    ?: return@executeApi store.apiError(422, "PAIRING_007", "Chưa chọn phương án hợp lệ.")
            if (selectedOpt.venueStatus == VENUE_INACTIVE) {
                return@executeApi inactiveVenueError(roomId)
            }
            val role = resolveRole(snapshot, current)
            val newHostConfirmed = current.hostConfirmed || role == ROLE_CAPTAIN_A
            val newGuestConfirmed = current.guestConfirmed || role == ROLE_CAPTAIN_B
            val bothConfirmed = newHostConfirmed && newGuestConfirmed
            store.update { snap ->
                val updatedPairings =
                    snap.pairings.map { item ->
                        when {
                            item.id == roomId -> {
                                item.copy(
                                    hostConfirmed = newHostConfirmed,
                                    guestConfirmed = newGuestConfirmed,
                                    status = if (bothConfirmed) STATUS_CONFIRMED else STATUS_NEGOTIATING,
                                )
                            }

                            bothConfirmed && item.matchId == current.matchId && item.status == STATUS_NEGOTIATING -> {
                                item.copy(status = STATUS_SUPERSEDED)
                            }

                            else -> {
                                item
                            }
                        }
                    }
                val updatedMatches =
                    if (bothConfirmed) {
                        snap.matches.map { match ->
                            if (match.id == current.matchId) {
                                match.copy(
                                    status = STATUS_CONFIRMED,
                                    guestTeamId = current.guestTeamId,
                                    venueId = selectedOpt.venueId,
                                )
                            } else {
                                match
                            }
                        }
                    } else {
                        snap.matches
                    }
                snap.copy(pairings = updatedPairings, matches = updatedMatches)
            }
            val updatedPairing = store.current.pairings.first { it.id == roomId }
            ApiResult.Success(ApiEnvelope(buildRoomDto(store.current, updatedPairing)))
        }

    override suspend fun disputePairing(
        roomId: String,
        request: DisputePairingRequestDto,
    ): ApiResult<ApiEnvelope<PairingRoomDto>> =
        store.executeApi(path = "/api/v1/pairing-rooms/$roomId/dispute") { snapshot ->
            val pairing =
                snapshot.pairings.firstOrNull { it.id == roomId }
                    ?: return@executeApi notFoundError(roomId)
            if (request.reason.isBlank()) {
                return@executeApi store.apiError(400, "VALIDATION_ERROR", "Vui lòng nhập lý do tranh chấp.")
            }
            if (pairing.status == STATUS_CANCELLED || pairing.status == STATUS_EXPIRED) {
                return@executeApi notNegotiatingError(roomId)
            }
            val prevMeta = roomMetaMap[roomId] ?: RoomMeta()
            roomMetaMap[roomId] = prevMeta.copy(disputeReason = request.reason.trim())
            updateStorePairing(roomId) { it.copy(status = STATUS_DISPUTED) }
            val updatedPairing = store.current.pairings.first { it.id == roomId }
            ApiResult.Success(ApiEnvelope(buildRoomDto(store.current, updatedPairing)))
        }

    override suspend fun cancelPairing(
        roomId: String,
        request: CancelPairingRequestDto,
    ): ApiResult<ApiEnvelope<PairingRoomDto>> =
        store.executeApi(path = "/api/v1/pairing-rooms/$roomId/cancel") { snapshot ->
            val pairing =
                snapshot.pairings.firstOrNull { it.id == roomId }
                    ?: return@executeApi notFoundError(roomId)
            if (request.reason.isBlank()) {
                return@executeApi store.apiError(400, "VALIDATION_ERROR", "Vui lòng nhập lý do hủy phòng.")
            }
            if (pairing.status == STATUS_CANCELLED || pairing.status == STATUS_EXPIRED) {
                return@executeApi notNegotiatingError(roomId)
            }
            val prevMeta = roomMetaMap[roomId] ?: RoomMeta()
            roomMetaMap[roomId] = prevMeta.copy(cancelReason = request.reason.trim())
            updateStorePairing(roomId) { it.copy(status = STATUS_CANCELLED) }
            val updatedPairing = store.current.pairings.first { it.id == roomId }
            ApiResult.Success(ApiEnvelope(buildRoomDto(store.current, updatedPairing)))
        }

    private fun checkDeadlineTransition(
        snapshot: ArenaStoreSnapshot,
        pairing: StorePairing,
    ): StorePairing {
        if (pairing.status != STATUS_NEGOTIATING) return pairing
        val deadlineIso = computeDeadlineIso(pairing.createdAt)
        val nowMs = VietnamDateTime.currentEpochMillis(snapshot.mockClockIso)
        val deadlineMs = VietnamDateTime.parseToEpochMillis(deadlineIso) ?: Long.MAX_VALUE
        if (nowMs >= deadlineMs) {
            updateStorePairing(pairing.id) { it.copy(status = STATUS_EXPIRED) }
            return pairing.copy(status = STATUS_EXPIRED)
        }
        return pairing
    }

    private fun computeDeadlineIso(createdAt: String): String {
        val created = VietnamDateTime.parseToLocalDateTime(createdAt)
        return if (created != null) {
            VietnamDateTime.formatLocal(created.plusHours(DEADLINE_HOURS))
        } else {
            "2026-11-21T17:00:00"
        }
    }

    private fun buildRoomDto(
        snapshot: ArenaStoreSnapshot,
        pairing: StorePairing,
    ): PairingRoomDto {
        val options = ensureOptions(snapshot, pairing)
        val meta = roomMetaMap[pairing.id] ?: RoomMeta()
        val hostTeam = snapshot.teams.firstOrNull { it.id == pairing.hostTeamId }
        val guestTeam = snapshot.teams.firstOrNull { it.id == pairing.guestTeamId }
        val hostCaptainName = hostTeam?.captainId?.let { snapshot.users[it]?.displayName }
        val guestCaptainName = guestTeam?.captainId?.let { snapshot.users[it]?.displayName }
        val selectedOptionId =
            meta.selectedByBOptionId ?: meta.selectedByAOptionId ?: options.firstOrNull()?.optionId
        return PairingRoomDto(
            roomId = pairing.id,
            matchId = pairing.matchId,
            conversationId = pairing.conversationId,
            status = pairing.status,
            revision = pairing.revision,
            deadlineAt = computeDeadlineIso(pairing.createdAt),
            captainA =
                PairingCaptainDto(
                    teamId = pairing.hostTeamId,
                    teamName = hostTeam?.name ?: "Đội chủ nhà",
                    shortName = hostTeam?.shortName ?: "A",
                    captainName = hostCaptainName,
                    reliabilityScore = hostTeam?.reputationScore ?: 90,
                    hasConfirmed = pairing.hostConfirmed,
                    selectedOptionId = meta.selectedByAOptionId,
                ),
            captainB =
                PairingCaptainDto(
                    teamId = pairing.guestTeamId,
                    teamName = guestTeam?.name ?: "Đội nhận kèo",
                    shortName = guestTeam?.shortName ?: "B",
                    captainName = guestCaptainName,
                    reliabilityScore = guestTeam?.reputationScore ?: 85,
                    hasConfirmed = pairing.guestConfirmed,
                    selectedOptionId = meta.selectedByBOptionId,
                ),
            currentUserRole = resolveRole(snapshot, pairing),
            options = options.toList(),
            selectedOptionId = selectedOptionId,
            disputeReason = meta.disputeReason,
            cancelReason = meta.cancelReason,
            createdAt = pairing.createdAt,
            updatedAt = VietnamDateTime.nowIso(snapshot.mockClockIso),
        )
    }

    private fun ensureOptions(
        snapshot: ArenaStoreSnapshot,
        pairing: StorePairing,
    ): MutableList<PairingOptionDto> {
        val existing = roomOptionsMap[pairing.id]
        if (existing != null) return existing

        val match = snapshot.matches.firstOrNull { it.id == pairing.matchId }
        val hostTeamName =
            snapshot.teams.firstOrNull { it.id == pairing.hostTeamId }?.name ?: "Đội chủ nhà"
        val venueIds =
            match?.proposedVenues?.ifEmpty { listOf(pairing.venueId) } ?: listOf(pairing.venueId)
        val timeSlots =
            match?.proposedTimeSlots?.ifEmpty { listOf(pairing.timeSlot) } ?: listOf(pairing.timeSlot)
        val matchDate = match?.matchDate ?: "2026-11-21"
        val feeSplitLabel =
            if (pairing.costSharing == "FIFTY_FIFTY") "Chia đôi 50-50" else pairing.costSharing

        val generated = mutableListOf<PairingOptionDto>()
        var defaultOptionId: String? = null
        venueIds.forEachIndexed { vIdx, venueId ->
            val venue = snapshot.venues.firstOrNull { it.id == venueId }
            val slot = timeSlots.getOrElse(vIdx) { timeSlots.first() }
            val optionId = "opt-${pairing.id}-${vIdx + 1}"
            val isDefault = venueId == pairing.venueId && slot == pairing.timeSlot
            if (isDefault || defaultOptionId == null) {
                defaultOptionId = optionId
            }
            generated.add(
                PairingOptionDto(
                    optionId = optionId,
                    venueId = venueId,
                    venueName = venue?.name ?: "Sân bóng đề xuất",
                    venueAddress = venue?.address ?: "TP. Hồ Chí Minh",
                    venueStatus = venue?.status ?: VENUE_ACTIVE,
                    dateTimeLabel = "$matchDate · $slot",
                    feeSplitLabel = feeSplitLabel,
                    totalCost = pairing.totalCost,
                    depositAmount = pairing.depositAmount,
                    depositHolder = pairing.depositHolder,
                    proposedByTeamId = pairing.hostTeamId,
                    proposedByTeamName = hostTeamName,
                    isSelectedByA = isDefault && pairing.hostConfirmed,
                    isSelectedByB = isDefault && pairing.guestConfirmed,
                ),
            )
        }
        roomOptionsMap[pairing.id] = generated
        roomMetaMap[pairing.id] =
            RoomMeta(
                selectedByAOptionId = if (pairing.hostConfirmed) defaultOptionId else null,
                selectedByBOptionId = if (pairing.guestConfirmed) defaultOptionId else null,
            )
        return generated
    }

    private fun resolveRole(
        snapshot: ArenaStoreSnapshot,
        pairing: StorePairing,
    ): String {
        val actingTeamId = snapshot.actingTeamId
        return when (actingTeamId) {
            pairing.hostTeamId -> {
                ROLE_CAPTAIN_A
            }

            pairing.guestTeamId -> {
                ROLE_CAPTAIN_B
            }

            else -> {
                val userId = snapshot.currentUserId
                val hostCap = snapshot.teams.firstOrNull { it.id == pairing.hostTeamId }?.captainId
                val guestCap = snapshot.teams.firstOrNull { it.id == pairing.guestTeamId }?.captainId
                when (userId) {
                    hostCap -> ROLE_CAPTAIN_A
                    guestCap -> ROLE_CAPTAIN_B
                    else -> ROLE_OBSERVER
                }
            }
        }
    }

    private fun updateSelectionMeta(
        roomId: String,
        role: String,
        optionId: String,
    ) {
        val prev = roomMetaMap[roomId] ?: RoomMeta()
        roomMetaMap[roomId] =
            when (role) {
                ROLE_CAPTAIN_A -> prev.copy(selectedByAOptionId = optionId)
                ROLE_CAPTAIN_B -> prev.copy(selectedByBOptionId = optionId)
                else -> prev.copy(selectedByBOptionId = optionId)
            }
    }

    private fun updateStorePairing(
        roomId: String,
        transform: (StorePairing) -> StorePairing,
    ) {
        store.update { snap ->
            snap.copy(
                pairings = snap.pairings.map { if (it.id == roomId) transform(it) else it },
            )
        }
    }

    private fun notFoundError(roomId: String): ApiResult.Error<ApiEnvelope<PairingRoomDto>> =
        store.apiError(
            httpStatus = 404,
            code = "PAIRING_001",
            message = "Không tìm thấy phòng thương lượng.",
            path = "/api/v1/pairing-rooms/$roomId",
        )

    private fun notNegotiatingError(roomId: String): ApiResult.Error<ApiEnvelope<PairingRoomDto>> =
        store.apiError(
            httpStatus = 409,
            code = "PAIRING_005",
            message = "Thương lượng không còn ở trạng thái đang trao đổi.",
            path = "/api/v1/pairing-rooms/$roomId",
        )

    private fun inactiveVenueError(roomId: String): ApiResult.Error<ApiEnvelope<PairingRoomDto>> =
        store.apiError(
            httpStatus = 422,
            code = "VENUE_002",
            message = "Địa điểm được chọn đã ngừng hoạt động.",
            path = "/api/v1/pairing-rooms/$roomId",
        )

    private companion object {
        const val DEADLINE_HOURS = 2L
        const val STATUS_NEGOTIATING = "NEGOTIATING"
        const val STATUS_CONFIRMED = "CONFIRMED"
        const val STATUS_DISPUTED = "DISPUTED"
        const val STATUS_CANCELLED = "CANCELLED"
        const val STATUS_SUPERSEDED = "SUPERSEDED"
        const val STATUS_EXPIRED = "EXPIRED"
        const val VENUE_ACTIVE = "ACTIVE"
        const val VENUE_INACTIVE = "INACTIVE"
        const val ROLE_CAPTAIN_A = "CAPTAIN_A"
        const val ROLE_CAPTAIN_B = "CAPTAIN_B"
        const val ROLE_OBSERVER = "OBSERVER"
    }
}
