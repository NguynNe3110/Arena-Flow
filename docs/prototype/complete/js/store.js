/**
 * ArenaFlow Central Store (Reactive State Machine)
 * Version: 1.0.0
 * Provides reactive pub/sub, localStorage persistence, character switcher,
 * mock clock, GPS simulation, error injection, and business rule transitions.
 */

import { INITIAL_FIXTURES } from './fixtures.js';

const STORAGE_KEY = 'arenaflow_prototype_v1';

class Store {
  constructor() {
    this.listeners = new Set();
    this.state = this._loadInitialState();
  }

  _clone(obj) {
    return JSON.parse(JSON.stringify(obj));
  }

  _loadInitialState() {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      if (saved) {
        const parsed = JSON.parse(saved);
        if (parsed?.meta?.version === INITIAL_FIXTURES.meta.version) {
          return parsed;
        }
      }
    } catch (e) {
      console.warn('Failed to load state from localStorage, falling back to seed.', e);
    }
    const state = this._clone(INITIAL_FIXTURES);
    state.currentUserId = 'user-cap-alpha'; // Default active character
    state.actingTeamId = 'team-alpha';
    return state;
  }

  _persist() {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(this.state));
    } catch (e) {
      console.error('Failed to persist store to localStorage', e);
    }
  }

  _notify() {
    this._persist();
    for (const listener of this.listeners) {
      try {
        listener(this.getState());
      } catch (err) {
        console.error('Store listener error:', err);
      }
    }
  }

  subscribe(listener) {
    this.listeners.add(listener);
    return () => this.listeners.delete(listener);
  }

  getState() {
    return this.state;
  }

  setState(updaterOrPartial) {
    if (typeof updaterOrPartial === 'function') {
      this.state = updaterOrPartial(this.state);
    } else {
      this.state = { ...this.state, ...updaterOrPartial };
    }
    this._notify();
  }

  resetAll() {
    localStorage.removeItem(STORAGE_KEY);
    const fresh = this._clone(INITIAL_FIXTURES);
    fresh.currentUserId = 'user-cap-alpha';
    fresh.actingTeamId = 'team-alpha';
    this.state = fresh;
    this._notify();
  }

  /* -------------------------------------------------------------------------
     Character & Acting Context
     ------------------------------------------------------------------------- */
  getCurrentUser() {
    return this.state.characters[this.state.currentUserId] || null;
  }

  switchUser(characterId) {
    if (!this.state.characters[characterId]) {
      console.warn(`User ${characterId} not found in fixtures`);
      return;
    }
    this.state.currentUserId = characterId;

    // Auto-select first active team captained by this user
    const captainedTeams = this.getMyCaptainedTeams();
    this.state.actingTeamId = captainedTeams.length > 0 ? captainedTeams[0].id : null;

    this._notify();
  }

  setActingTeam(teamId) {
    this.state.actingTeamId = teamId;
    this._notify();
  }

  getMyCaptainedTeams() {
    const userId = this.state.currentUserId;
    return this.state.teams.filter(t => t.captainId === userId && t.status === 'ACTIVE');
  }

  getMyTeams() {
    const userId = this.state.currentUserId;
    const memberTeamIds = this.state.teamMembers
      .filter(m => m.userId === userId && m.status === 'ACTIVE')
      .map(m => m.teamId);
    return this.state.teams.filter(t => memberTeamIds.includes(t.id));
  }

  /* -------------------------------------------------------------------------
     Clock, GPS, and Error Simulation
     ------------------------------------------------------------------------- */
  setClock(isoTimestamp) {
    this.state.meta.mockClock = isoTimestamp;
    this._notify();
  }

  getClock() {
    return new Date(this.state.meta.mockClock || Date.now());
  }

  setGpsStatus(status) {
    this.state.meta.gpsStatus = status; // 'granted' | 'denied' | 'prompt'
    this._notify();
  }

  setSimulatedError(errorType) {
    this.state.meta.simulatedError = errorType; // 'none' | '403' | '404' | '409' | 'network'
    this._notify();
  }

  checkSimulatedError() {
    const err = this.state.meta.simulatedError;
    if (err && err !== 'none') {
      // Consume one-shot error or keep active based on scenario
      return err;
    }
    return null;
  }

  consumeSimulatedError() {
    const err = this.state.meta.simulatedError;
    if (err && err !== 'none') {
      this.state.meta.simulatedError = 'none';
      this._notify();
      return err;
    }
    return null;
  }

  /* -------------------------------------------------------------------------
     Scenario Presets (Section 8.2 & Journey Testing)
     ------------------------------------------------------------------------- */
  loadScenario(scenarioKey) {
    this.resetAll();
    this.state.meta.activeScenario = scenarioKey;

    switch (scenarioKey) {
      case 'auth_new_user':
        // J-01: User mới chưa có hồ sơ
        this.switchUser('user-new');
        break;

      case 'team_transfer_disband':
        // J-05 & J-06: Captain Alpha with team, ready for captain transfer or block delete
        this.switchUser('user-cap-alpha');
        this.setActingTeam('team-alpha');
        break;

      case 'match_a_flow':
        // J-07 & J-08: Match Kind A requests pending approval
        this.switchUser('user-cap-alpha');
        this.setActingTeam('team-alpha');
        break;

      case 'pairing_b_flow':
        // J-09 & J-10: Match Kind B negotiation conflict / superseded
        this.switchUser('user-cap-beta');
        this.setActingTeam('team-beta');
        break;

      case 'mercenary_privacy':
        // J-12 & J-13: User Player testing hidden venue on pending vs visible on approved
        this.switchUser('user-player');
        break;

      case 'noshow_dispute':
        // J-21: NOSHOW dispute within 24h
        this.switchUser('user-cap-beta');
        this.setActingTeam('team-beta');
        break;

      case 'admin_moderation':
        // J-22: Admin portal
        this.switchUser('user-admin');
        break;

      default:
        this.switchUser('user-cap-alpha');
        break;
    }

    this._notify();
  }

  /* -------------------------------------------------------------------------
     User & Profile Actions (UC-U01..U03)
     ------------------------------------------------------------------------- */
  updateUserProfile(userId, data) {
    const err = this.consumeSimulatedError();
    if (err) throw new Error(`Simulated Error: ${err}`);

    const user = this.state.characters[userId];
    if (!user) throw new Error('User not found');

    // Validation per Section 6.1 (PROF-02)
    if (data.displayName !== undefined && !data.displayName.trim()) {
      throw new Error('Tên hiển thị không được để trống hoặc chỉ chứa khoảng trắng');
    }
    if (data.dateOfBirth) {
      const birth = new Date(data.dateOfBirth);
      const now = this.getClock();
      const age = (now - birth) / (365.25 * 24 * 60 * 60 * 1000);
      if (age < 13) {
        throw new Error('Bạn phải đủ ít nhất 13 tuổi để sử dụng ArenaFlow');
      }
    }

    Object.assign(user, data, { isNewUser: false });
    this._notify();
    return user;
  }

  canDeleteAccount(userId) {
    // AC-P02: Block account deletion if user is captain of any ACTIVE team
    const activeCaptained = this.state.teams.filter(
      t => t.captainId === userId && t.status === 'ACTIVE'
    );
    return {
      canDelete: activeCaptained.length === 0,
      blockingTeams: activeCaptained
    };
  }

  deleteAccount(userId) {
    const err = this.consumeSimulatedError();
    if (err) throw new Error(`Simulated Error: ${err}`);

    const check = this.canDeleteAccount(userId);
    if (!check.canDelete) {
      throw new Error('Không thể xóa tài khoản: Bạn vẫn đang là đội trưởng của đội hoạt động.');
    }

    const user = this.state.characters[userId];
    if (user) {
      user.isDeleted = true;
      user.displayName = 'Người dùng đã xóa';
      user.bio = 'Tài khoản đã bị xóa.';
    }
    this._notify();
    return true;
  }

  /* -------------------------------------------------------------------------
     Team Actions (UC-T01..T06)
     ------------------------------------------------------------------------- */
  createTeam(teamData) {
    const err = this.consumeSimulatedError();
    if (err) throw new Error(`Simulated Error: ${err}`);

    const currentUserId = this.state.currentUserId;
    const newTeamId = 'team-' + Date.now();
    const newTeam = {
      id: newTeamId,
      name: teamData.name,
      shortName: teamData.shortName || teamData.name.slice(0, 4).toUpperCase(),
      logoUrl: teamData.logoUrl || null,
      description: teamData.description || '',
      captainId: currentUserId,
      wardId: teamData.wardId || 'ward-ak',
      status: 'ACTIVE',
      skillLevels: teamData.skillLevels || ['INTERMEDIATE'],
      reputationScore: 100,
      ratingAvg: 5.0,
      completionRate: null, // < 5 matches
      matchCount: 0,
      createdAt: this.state.meta.mockClock,
      captainHistory: [{ captainId: currentUserId, assignedAt: this.state.meta.mockClock }]
    };

    this.state.teams.push(newTeam);
    this.state.teamMembers.push({
      teamId: newTeamId,
      userId: currentUserId,
      role: 'CAPTAIN',
      joinedAt: this.state.meta.mockClock,
      status: 'ACTIVE'
    });

    this.state.actingTeamId = newTeamId;
    this._notify();
    return newTeam;
  }

  transferCaptaincy(teamId, newCaptainUserId) {
    const err = this.consumeSimulatedError();
    if (err) throw new Error(`Simulated Error: ${err}`);

    const team = this.state.teams.find(t => t.id === teamId);
    if (!team) throw new Error('Đội không tồn tại');

    if (team.captainId !== this.state.currentUserId) {
      throw new Error('Chỉ đội trưởng hiện tại mới có quyền chuyển giao đội');
    }

    // Instant transfer per USER-001 & Section 6.2 (TEAM-09)
    const oldCaptainId = team.captainId;
    team.captainId = newCaptainUserId;
    team.captainHistory.push({
      captainId: newCaptainUserId,
      assignedAt: this.state.meta.mockClock
    });

    // Update member roles
    const oldMember = this.state.teamMembers.find(m => m.teamId === teamId && m.userId === oldCaptainId);
    if (oldMember) oldMember.role = 'MEMBER';

    const newMember = this.state.teamMembers.find(m => m.teamId === teamId && m.userId === newCaptainUserId);
    if (newMember) {
      newMember.role = 'CAPTAIN';
    } else {
      this.state.teamMembers.push({
        teamId,
        userId: newCaptainUserId,
        role: 'CAPTAIN',
        joinedAt: this.state.meta.mockClock,
        status: 'ACTIVE'
      });
    }

    // Refresh acting team if current user was acting as captain
    if (this.state.actingTeamId === teamId) {
      const remainingCaptained = this.getMyCaptainedTeams();
      this.state.actingTeamId = remainingCaptained.length > 0 ? remainingCaptained[0].id : null;
    }

    this._notify();
    return team;
  }

  disbandTeam(teamId, reason) {
    const err = this.consumeSimulatedError();
    if (err) throw new Error(`Simulated Error: ${err}`);

    const team = this.state.teams.find(t => t.id === teamId);
    if (!team) throw new Error('Đội không tồn tại');

    if (team.captainId !== this.state.currentUserId) {
      throw new Error('Chỉ đội trưởng mới có quyền giải tán đội');
    }

    team.status = 'DISBANDED';
    team.disbandReason = reason || 'Đội trưởng giải tán đội';
    team.disbandedAt = this.state.meta.mockClock;

    // Refresh acting team
    if (this.state.actingTeamId === teamId) {
      const remainingCaptained = this.getMyCaptainedTeams();
      this.state.actingTeamId = remainingCaptained.length > 0 ? remainingCaptained[0].id : null;
    }

    this._notify();
    return team;
  }

  leaveTeam(teamId, userId) {
    const member = this.state.teamMembers.find(m => m.teamId === teamId && m.userId === userId);
    if (!member) throw new Error('Bạn không phải thành viên đội này');
    if (member.role === 'CAPTAIN') {
      throw new Error('Đội trưởng phải chuyển giao quyền hoặc giải tán trước khi rời đội');
    }
    member.status = 'LEFT';
    member.leftAt = this.state.meta.mockClock;
    this._notify();
    return true;
  }

  /* -------------------------------------------------------------------------
     Match Kind A Flow (UC-M01..M04, AC-P03)
     ------------------------------------------------------------------------- */
  createMatch(matchData) {
    const err = this.consumeSimulatedError();
    if (err) throw new Error(`Simulated Error: ${err}`);

    const newMatch = {
      id: 'match-' + Date.now(),
      kind: matchData.kind, // 'A' or 'B'
      hostTeamId: matchData.hostTeamId || this.state.actingTeamId,
      sportId: matchData.sportId || 'sport-football',
      format: matchData.format || 'SEVEN_A_SIDE',
      skillLevel: matchData.skillLevel || 'INTERMEDIATE',
      venueId: matchData.venueId || null,
      proposedVenues: matchData.proposedVenues || [],
      proposedTimeSlots: matchData.proposedTimeSlots || [],
      matchDate: matchData.matchDate,
      startTime: matchData.startTime || '19:00',
      endTime: matchData.endTime || '20:30',
      expiresAt: matchData.expiresAt,
      status: 'OPEN',
      totalCost: matchData.totalCost || 600000,
      costSharing: matchData.costSharing || 'SPLIT_50_50',
      depositAmount: matchData.depositAmount || 0,
      depositHolder: matchData.depositHolder || 'HOST',
      notes: matchData.notes || '',
      confirmedGuestTeamId: null,
      createdAt: this.state.meta.mockClock
    };

    this.state.matches.unshift(newMatch);
    this._notify();
    return newMatch;
  }

  sendMatchRequest(matchId, requesterTeamId, message) {
    const err = this.consumeSimulatedError();
    if (err) throw new Error(`Simulated Error: ${err}`);

    const match = this.state.matches.find(m => m.id === matchId);
    if (!match || match.status !== 'OPEN') throw new Error('Kèo không còn mở để gửi yêu cầu');

    const newReq = {
      id: 'req-' + Date.now(),
      matchId,
      requesterTeamId,
      status: 'PENDING',
      message: message || '',
      createdAt: this.state.meta.mockClock
    };

    this.state.matchRequests.push(newReq);

    // Create room for conversation
    const hostTeam = this.state.teams.find(t => t.id === match.hostTeamId);
    const guestTeam = this.state.teams.find(t => t.id === requesterTeamId);
    if (hostTeam && guestTeam) {
      this.state.conversations.push({
        id: 'conv-' + newReq.id,
        contextType: 'MATCH_REQUEST',
        contextId: newReq.id,
        participantIds: [hostTeam.captainId, guestTeam.captainId],
        canSend: true,
        messages: [
          {
            id: 'msg-' + Date.now(),
            senderId: guestTeam.captainId,
            text: message || 'Đội chúng tôi muốn gửi yêu cầu ghép trận!',
            timestamp: this.state.meta.mockClock,
            readBy: [guestTeam.captainId]
          }
        ]
      });
    }

    this._notify();
    return newReq;
  }

  acceptMatchRequest(requestId) {
    const err = this.consumeSimulatedError();
    if (err) throw new Error(`Simulated Error: ${err}`);

    const targetReq = this.state.matchRequests.find(r => r.id === requestId);
    if (!targetReq || targetReq.status !== 'PENDING') throw new Error('Yêu cầu không còn hợp lệ');

    const match = this.state.matches.find(m => m.id === targetReq.matchId);
    if (!match || match.status !== 'OPEN') throw new Error('Kèo đã kết thúc hoặc không còn mở');

    // AC-P03: One request accepted, match confirmed, all other pending requests SUPERSEDED!
    targetReq.status = 'ACCEPTED';
    match.status = 'CONFIRMED';
    match.guestTeamId = targetReq.requesterTeamId;
    match.confirmedGuestTeamId = targetReq.requesterTeamId;

    this.state.matchRequests.forEach(r => {
      if (r.matchId === match.id && r.id !== targetReq.id && r.status === 'PENDING') {
        r.status = 'SUPERSEDED';
      }
    });

    this._notify();
    return match;
  }

  rejectMatchRequest(requestId) {
    const targetReq = this.state.matchRequests.find(r => r.id === requestId);
    if (!targetReq) throw new Error('Yêu cầu không tồn tại');
    targetReq.status = 'REJECTED';
    this._notify();
    return targetReq;
  }

  withdrawMatchRequest(requestId) {
    const targetReq = this.state.matchRequests.find(r => r.id === requestId);
    if (!targetReq) throw new Error('Yêu cầu không tồn tại');
    if (targetReq.status === 'ACCEPTED') {
      // Withdrawing accepted is equivalent to cancelling match per Section 6.3 (MATCH-11)
      const match = this.state.matches.find(m => m.id === targetReq.matchId);
      if (match) {
        match.status = 'CANCELLED';
        match.cancelReason = 'Đội khách rút sau khi đã chốt kèo';
      }
    }
    targetReq.status = 'WITHDRAWN';
    this._notify();
    return targetReq;
  }

  /* -------------------------------------------------------------------------
     Match Kind B & Pairing Negotiation Flow (UC-M05..M07, AC-P04)
     ------------------------------------------------------------------------- */
  updatePairingPackage(pairingId, newPackage) {
    const err = this.consumeSimulatedError();
    if (err) throw new Error(`Simulated Error: ${err}`);

    const pairing = this.state.pairings.find(p => p.id === pairingId);
    if (!pairing) throw new Error('Thương lượng không tồn tại');
    if (pairing.status !== 'NEGOTIATING') throw new Error('Thương lượng đã đóng');

    // AC-P04: When package changes, clear confirmations of BOTH parties and increment revision!
    pairing.package = {
      ...pairing.package,
      ...newPackage,
      revision: (pairing.package.revision || 1) + 1,
      confirms: {
        [pairing.hostTeamId]: false,
        [pairing.guestTeamId]: false
      }
    };

    this._notify();
    return pairing;
  }

  confirmPairingPackage(pairingId, teamId) {
    const err = this.consumeSimulatedError();
    if (err) throw new Error(`Simulated Error: ${err}`);

    const pairing = this.state.pairings.find(p => p.id === pairingId);
    if (!pairing) throw new Error('Thương lượng không tồn tại');
    if (pairing.status !== 'NEGOTIATING') throw new Error('Thương lượng không ở trạng thái đàm phán');

    if (!pairing.package.venueId || !pairing.package.timeSlot) {
      throw new Error('Phải thống nhất địa điểm và khung giờ trước khi xác nhận');
    }

    pairing.package.confirms[teamId] = true;

    // Check if both sides have confirmed
    const hostConfirmed = pairing.package.confirms[pairing.hostTeamId];
    const guestConfirmed = pairing.package.confirms[pairing.guestTeamId];

    if (hostConfirmed && guestConfirmed) {
      // Both confirmed! Match confirmed, this pairing confirmed, other pairings SUPERSEDED!
      pairing.status = 'CONFIRMED';
      const match = this.state.matches.find(m => m.id === pairing.matchId);
      if (match) {
        match.status = 'CONFIRMED';
        match.venueId = pairing.package.venueId;
        const [start, end] = pairing.package.timeSlot.split(' - ');
        match.startTime = start;
        match.endTime = end;
        match.guestTeamId = pairing.guestTeamId;
        match.confirmedGuestTeamId = pairing.guestTeamId;
        match.totalCost = pairing.package.totalCost;
      }

      // Supersede all other pairings for this match
      this.state.pairings.forEach(p => {
        if (p.matchId === pairing.matchId && p.id !== pairing.id && p.status === 'NEGOTIATING') {
          p.status = 'SUPERSEDED';
        }
      });
    }

    this._notify();
    return pairing;
  }

  /* -------------------------------------------------------------------------
     Mercenary (Đá Lẻ) Flow (UC-ME01..ME09, AC-P05, AC-P06)
     ------------------------------------------------------------------------- */
  applyRecruitment(recruitmentId, userId, message) {
    const err = this.consumeSimulatedError();
    if (err) throw new Error(`Simulated Error: ${err}`);

    const rec = this.state.recruitments.find(r => r.id === recruitmentId);
    if (!rec || rec.status !== 'OPEN') throw new Error('Tin tuyển đã đủ người hoặc đóng');

    // Check duplicate
    const existing = this.state.applications.find(
      a => a.recruitmentId === recruitmentId && a.applicantId === userId && a.status === 'PENDING'
    );
    if (existing) throw new Error('Bạn đã có đơn đang chờ duyệt cho tin này');

    const newApp = {
      id: 'app-' + Date.now(),
      recruitmentId,
      applicantId: userId,
      status: 'PENDING',
      message: message || '',
      createdAt: this.state.meta.mockClock
    };

    this.state.applications.push(newApp);

    // Open chat room between captain and applicant
    const team = this.state.teams.find(t => t.id === rec.teamId);
    if (team) {
      this.state.conversations.push({
        id: 'conv-' + newApp.id,
        contextType: 'MERCENARY',
        contextId: newApp.id,
        participantIds: [team.captainId, userId],
        canSend: true,
        messages: [
          {
            id: 'msg-' + Date.now(),
            senderId: userId,
            text: message || 'Tôi muốn ứng tuyển vị trí này!',
            timestamp: this.state.meta.mockClock,
            readBy: [userId]
          }
        ]
      });
    }

    this._notify();
    return newApp;
  }

  approveApplication(applicationId) {
    const err = this.consumeSimulatedError();
    if (err) throw new Error(`Simulated Error: ${err}`);

    const app = this.state.applications.find(a => a.id === applicationId);
    if (!app || app.status !== 'PENDING') throw new Error('Đơn không ở trạng thái chờ duyệt');

    const rec = this.state.recruitments.find(r => r.id === app.recruitmentId);
    if (!rec) throw new Error('Tin tuyển không tồn tại');

    if (rec.approvedCount >= rec.quantity) {
      throw new Error('Tin tuyển đã đủ slot, không thể duyệt thêm');
    }

    app.status = 'APPROVED';
    app.approvedAt = this.state.meta.mockClock;
    rec.approvedCount += 1;

    if (rec.approvedCount >= rec.quantity) {
      rec.status = 'FILLED';
    }

    this._notify();
    return app;
  }

  rejectApplication(applicationId) {
    const app = this.state.applications.find(a => a.id === applicationId);
    if (!app) throw new Error('Đơn không tồn tại');
    app.status = 'REJECTED';
    this._notify();
    return app;
  }

  withdrawApplication(applicationId) {
    const err = this.consumeSimulatedError();
    if (err) throw new Error(`Simulated Error: ${err}`);

    const app = this.state.applications.find(a => a.id === applicationId);
    if (!app) throw new Error('Đơn không tồn tại');

    const rec = this.state.recruitments.find(r => r.id === app.recruitmentId);
    const applicant = this.state.characters[app.applicantId];

    if (app.status === 'APPROVED') {
      // AC-P06: Withdrawing APPROVED application deducts reputation score and re-opens recruitment if filled
      if (applicant) {
        applicant.reputationScore = Math.max(0, (applicant.reputationScore || 100) - 5);
      }
      if (rec) {
        rec.approvedCount = Math.max(0, rec.approvedCount - 1);
        if (rec.status === 'FILLED') {
          rec.status = 'OPEN';
        }
      }
    }
    // Withdrawing PENDING has NO reputation deduction per AC-P06

    app.status = 'WITHDRAWN';
    this._notify();
    return app;
  }

  /* -------------------------------------------------------------------------
     Reputation, Reports & Disputes (REPUTATION-001, AC-P07, AC-P08)
     ------------------------------------------------------------------------- */
  submitReport(reportData) {
    const err = this.consumeSimulatedError();
    if (err) throw new Error(`Simulated Error: ${err}`);

    const newReport = {
      id: 'rep-' + Date.now(),
      reporterType: reportData.reporterType || 'TEAM',
      reporterId: reportData.reporterId,
      reportedId: reportData.reportedId,
      matchId: reportData.matchId || null,
      type: reportData.type, // 'NOSHOW', 'BAD_COMMUNICATION', 'FAKE_INFO', 'OTHER'
      reason: reportData.reason,
      status: reportData.type === 'NOSHOW' ? 'AUTO_ACCEPTED' : 'PENDING',
      createdAt: this.state.meta.mockClock,
      canDisputeUntil: reportData.type === 'NOSHOW'
        ? new Date(new Date(this.state.meta.mockClock).getTime() + 24 * 3600 * 1000).toISOString()
        : null,
      dispute: null
    };

    this.state.reports.push(newReport);
    this._notify();
    return newReport;
  }

  submitDispute(reportId, evidenceText) {
    const err = this.consumeSimulatedError();
    if (err) throw new Error(`Simulated Error: ${err}`);

    const report = this.state.reports.find(r => r.id === reportId);
    if (!report) throw new Error('Báo cáo không tồn tại');

    // AC-P08: Dispute ONLY allowed for NOSHOW within 24h
    if (report.type !== 'NOSHOW') {
      throw new Error('Chỉ có thể khiếu nại báo cáo bùng kèo (NOSHOW)');
    }

    const now = new Date(this.state.meta.mockClock).getTime();
    const disputeDeadline = new Date(report.canDisputeUntil).getTime();
    if (now > disputeDeadline) {
      throw new Error('Đã quá thời hạn khiếu nại 24 giờ cho báo cáo này');
    }

    if (!evidenceText || !evidenceText.trim()) {
      throw new Error('Bằng chứng giải trình không được để trống');
    }

    report.status = 'DISPUTED';
    report.dispute = {
      evidenceText,
      createdAt: this.state.meta.mockClock
    };

    this._notify();
    return report;
  }

  adminResolveDispute(reportId, outcome, adminNote) {
    // outcome: 'RESOLVED' (agree with report) or 'REJECTED' (dismiss report)
    const err = this.consumeSimulatedError();
    if (err) throw new Error(`Simulated Error: ${err}`);

    if (!adminNote || adminNote.trim().length < 1 || adminNote.trim().length > 500) {
      throw new Error('Ghi chú quyết định xử lý bắt buộc từ 1 đến 500 ký tự');
    }

    const report = this.state.reports.find(r => r.id === reportId);
    if (!report) throw new Error('Báo cáo không tồn tại');

    report.status = outcome;
    report.adminDecision = {
      outcome,
      note: adminNote.trim(),
      resolvedAt: this.state.meta.mockClock
    };

    this._notify();
    return report;
  }

  /* -------------------------------------------------------------------------
     Notifications & Chat
     ------------------------------------------------------------------------- */
  markNotificationRead(notificationId) {
    const notif = this.state.notifications.find(n => n.id === notificationId);
    if (notif) {
      notif.read = true;
      this._notify();
    }
  }

  markAllNotificationsRead() {
    const currentUserId = this.state.currentUserId;
    this.state.notifications.forEach(n => {
      if (n.userId === currentUserId) n.read = true;
    });
    this._notify();
  }

  sendMessage(conversationId, text) {
    const err = this.consumeSimulatedError();
    if (err) throw new Error(`Simulated Error: ${err}`);

    const conv = this.state.conversations.find(c => c.id === conversationId);
    if (!conv) throw new Error('Cuộc trò chuyện không tồn tại');
    if (!conv.canSend) throw new Error('Cuộc trò chuyện ở chế độ chỉ đọc');

    const msg = {
      id: 'msg-' + Date.now(),
      senderId: this.state.currentUserId,
      text: text.trim(),
      timestamp: this.state.meta.mockClock,
      readBy: [this.state.currentUserId]
    };

    conv.messages.push(msg);
    this._notify();
    return msg;
  }
}

export const AppStore = new Store();
window.AppStore = AppStore;
