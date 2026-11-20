/**
 * ArenaFlow Fixtures & Seed Data
 * Baseline date: 2026-11-18
 * Supports all 8 test characters, realistic relations, and full journey state machines.
 */

export const INITIAL_FIXTURES = {
  meta: {
    version: '1.0.0',
    seededAt: '2026-11-18T10:00:00Z',
    mockClock: '2026-11-18T10:00:00Z',
    gpsStatus: 'granted', // 'granted' | 'denied' | 'prompt'
    activeScenario: 'default',
    simulatedError: 'none' // 'none' | '403' | '404' | '409' | 'network'
  },

  characters: {
    'user-new': {
      id: 'user-new',
      phone: '0901000001',
      displayName: '',
      preferredPosition: null,
      wardId: null,
      dateOfBirth: null,
      gender: null,
      bio: '',
      avatarUrl: null,
      reputationScore: 100,
      completionRate: null,
      matchCount: 0,
      isNewUser: true,
      isDeleted: false,
      role: 'USER',
      label: 'User mới (Chưa xong hồ sơ, chưa có đội)'
    },
    'user-player': {
      id: 'user-player',
      phone: '0902000002',
      displayName: 'Trần Văn Player',
      preferredPosition: 'MIDFIELDER',
      wardId: 'ward-ak',
      dateOfBirth: '1998-05-15',
      gender: 'MALE',
      bio: 'Chuyên đá tiền vệ giữa, thể lực tốt, thích giao lưu học hỏi.',
      avatarUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150',
      reputationScore: 95,
      completionRate: 98,
      matchCount: 14,
      isNewUser: false,
      isDeleted: false,
      role: 'USER',
      label: 'User cá nhân (Đá lẻ, có đơn pending/approved)'
    },
    'user-cap-alpha': {
      id: 'user-cap-alpha',
      phone: '0903000003',
      displayName: 'Lê Hoàng Alpha',
      preferredPosition: 'FORWARD',
      wardId: 'ward-ak',
      dateOfBirth: '1995-10-20',
      gender: 'MALE',
      bio: 'Đội trưởng Bình Minh FC. Tinh thần fair play trên hết.',
      avatarUrl: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150',
      reputationScore: 92,
      completionRate: 96,
      matchCount: 25,
      isNewUser: false,
      isDeleted: false,
      role: 'USER',
      label: 'Captain Alpha (Đội Bình Minh FC, chủ kèo A)'
    },
    'user-cap-beta': {
      id: 'user-cap-beta',
      phone: '0904000004',
      displayName: 'Phạm Văn Beta',
      preferredPosition: 'DEFENDER',
      wardId: 'ward-bt',
      dateOfBirth: '1994-03-12',
      gender: 'MALE',
      bio: 'Đội trưởng Sài Gòn United. Chuyên giao lưu sân 7 người.',
      avatarUrl: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150',
      reputationScore: 88,
      completionRate: 92,
      matchCount: 18,
      isNewUser: false,
      isDeleted: false,
      role: 'USER',
      label: 'Captain Beta (Đội Sài Gòn United, đối tác / chủ kèo B)'
    },
    'user-cap-gamma': {
      id: 'user-cap-gamma',
      phone: '0905000005',
      displayName: 'Nguyễn Văn Gamma',
      preferredPosition: 'MIDFIELDER',
      wardId: 'ward-td',
      dateOfBirth: '1996-08-25',
      gender: 'MALE',
      bio: 'Đội trưởng Thảo Điền FC. Cạnh tranh ghép kèo B.',
      avatarUrl: 'https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=150',
      reputationScore: 90,
      completionRate: 94,
      matchCount: 20,
      isNewUser: false,
      isDeleted: false,
      role: 'USER',
      label: 'Captain Gamma (Đội Thảo Điền FC, đối thủ cạnh tranh)'
    },
    'user-cap-multi': {
      id: 'user-cap-multi',
      phone: '0906000006',
      displayName: 'Đoàn Văn Multi',
      preferredPosition: 'FORWARD',
      wardId: 'ward-q1',
      dateOfBirth: '1992-12-05',
      gender: 'MALE',
      bio: 'Quản lý Rồng Vàng FC và Phượng Hoàng FC.',
      avatarUrl: 'https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=150',
      reputationScore: 89,
      completionRate: 95,
      matchCount: 22,
      isNewUser: false,
      isDeleted: false,
      role: 'USER',
      label: 'Captain nhiều đội (Rồng Vàng FC & Phượng Hoàng FC)'
    },
    'user-member': {
      id: 'user-member',
      phone: '0907000007',
      displayName: 'Vũ Văn Member',
      preferredPosition: 'GOALKEEPER',
      wardId: 'ward-ak',
      dateOfBirth: '2000-01-18',
      gender: 'MALE',
      bio: 'Thủ môn năng nổ của Bình Minh FC.',
      avatarUrl: 'https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=150',
      reputationScore: 91,
      completionRate: 95,
      matchCount: 12,
      isNewUser: false,
      isDeleted: false,
      role: 'USER',
      label: 'Member (Thành viên Bình Minh FC, không có quyền quản trị)'
    },
    'user-admin': {
      id: 'user-admin',
      phone: '0909999999',
      displayName: 'Admin Quản Trị',
      preferredPosition: 'MIDFIELDER',
      wardId: 'ward-q1',
      dateOfBirth: '1990-01-01',
      gender: 'OTHER',
      bio: 'Quản trị viên hệ thống ArenaFlow.',
      avatarUrl: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150',
      reputationScore: 100,
      completionRate: 100,
      matchCount: 50,
      isNewUser: false,
      isDeleted: false,
      role: 'ADMIN',
      label: 'Admin (Khu vực quản trị hệ thống)'
    },
    'user-del-99': {
      id: 'user-del-99',
      phone: '0900000099',
      displayName: 'Người dùng đã xóa',
      realName: 'Hoàng Văn Cũ',
      preferredPosition: 'DEFENDER',
      wardId: 'ward-ak',
      dateOfBirth: '1993-04-10',
      gender: 'MALE',
      bio: 'Tài khoản này đã bị xóa.',
      avatarUrl: null,
      reputationScore: 70,
      completionRate: 80,
      matchCount: 8,
      isNewUser: false,
      isDeleted: true,
      role: 'USER',
      label: 'Người dùng đã xóa (User-002: che tên với user, hiện cờ với admin)'
    }
  },

  wards: [
    { id: 'ward-ak', name: 'Phường An Khánh', district: 'TP. Thủ Đức', city: 'TP. Hồ Chí Minh' },
    { id: 'ward-bt', name: 'Phường Bình Trưng Tây', district: 'TP. Thủ Đức', city: 'TP. Hồ Chí Minh' },
    { id: 'ward-td', name: 'Phường Thảo Điền', district: 'TP. Thủ Đức', city: 'TP. Hồ Chí Minh' },
    { id: 'ward-q1', name: 'Phường Bến Nghé', district: 'Quận 1', city: 'TP. Hồ Chí Minh' }
  ],

  teams: [
    {
      id: 'team-alpha',
      name: 'Bình Minh FC',
      shortName: 'BMFC',
      logoUrl: null,
      description: 'Đội bóng phong trào văn phòng khu vực An Khánh, chơi đẹp, vui là chính.',
      captainId: 'user-cap-alpha',
      wardId: 'ward-ak',
      status: 'ACTIVE',
      skillLevels: ['INTERMEDIATE'],
      reputationScore: 92,
      ratingAvg: 4.8,
      completionRate: 96,
      matchCount: 25,
      createdAt: '2026-01-15T08:00:00Z',
      captainHistory: [
        { captainId: 'user-cap-alpha', assignedAt: '2026-01-15T08:00:00Z' }
      ]
    },
    {
      id: 'team-beta',
      name: 'Sài Gòn United',
      shortName: 'SGU',
      logoUrl: null,
      description: 'Đội giao lưu sân 7 người cuối tuần, ưu tiên đá nhiệt tình.',
      captainId: 'user-cap-beta',
      wardId: 'ward-bt',
      status: 'ACTIVE',
      skillLevels: ['INTERMEDIATE', 'ADVANCED'],
      reputationScore: 88,
      ratingAvg: 4.6,
      completionRate: 92,
      matchCount: 18,
      createdAt: '2026-02-10T09:00:00Z',
      captainHistory: [
        { captainId: 'user-cap-beta', assignedAt: '2026-02-10T09:00:00Z' }
      ]
    },
    {
      id: 'team-gamma',
      name: 'Thảo Điền FC',
      shortName: 'TDFC',
      logoUrl: null,
      description: 'Đội anh em Thảo Điền, gắn kết bền lâu, ưu tiên thể lực.',
      captainId: 'user-cap-gamma',
      wardId: 'ward-td',
      status: 'ACTIVE',
      skillLevels: ['BEGINNER', 'INTERMEDIATE'],
      reputationScore: 90,
      ratingAvg: 4.7,
      completionRate: 94,
      matchCount: 20,
      createdAt: '2026-03-01T10:00:00Z',
      captainHistory: [
        { captainId: 'user-cap-gamma', assignedAt: '2026-03-01T10:00:00Z' }
      ]
    },
    {
      id: 'team-multi-1',
      name: 'Rồng Vàng FC',
      shortName: 'RVFC',
      logoUrl: null,
      description: 'Đội bóng giao hữu Quận 1.',
      captainId: 'user-cap-multi',
      wardId: 'ward-q1',
      status: 'ACTIVE',
      skillLevels: ['INTERMEDIATE'],
      reputationScore: 85,
      ratingAvg: 4.5,
      completionRate: 90,
      matchCount: 10,
      createdAt: '2026-04-12T08:00:00Z',
      captainHistory: [
        { captainId: 'user-cap-multi', assignedAt: '2026-04-12T08:00:00Z' }
      ]
    },
    {
      id: 'team-multi-2',
      name: 'Phượng Hoàng FC',
      shortName: 'PHFC',
      logoUrl: null,
      description: 'Đội bóng trẻ nhiệt huyết.',
      captainId: 'user-cap-multi',
      wardId: 'ward-q1',
      status: 'ACTIVE',
      skillLevels: ['ADVANCED'],
      reputationScore: 89,
      ratingAvg: 4.7,
      completionRate: 95,
      matchCount: 12,
      createdAt: '2026-05-18T08:00:00Z',
      captainHistory: [
        { captainId: 'user-cap-multi', assignedAt: '2026-05-18T08:00:00Z' }
      ]
    },
    {
      id: 'team-disbanded-1',
      name: 'Chấn Thương FC',
      shortName: 'CTFC',
      logoUrl: null,
      description: 'Đội bóng đã dừng hoạt động.',
      captainId: 'user-cap-alpha',
      wardId: 'ward-ak',
      status: 'DISBANDED',
      disbandReason: 'Đội không còn đủ thành viên tham gia hoạt động thi đấu.',
      skillLevels: ['BEGINNER'],
      reputationScore: 75,
      ratingAvg: 4.0,
      completionRate: 80,
      matchCount: 6,
      createdAt: '2026-01-01T08:00:00Z',
      disbandedAt: '2026-06-01T12:00:00Z'
    },
    {
      id: 'team-inactive-1',
      name: 'Tạm Nghỉ FC',
      shortName: 'TNFC',
      logoUrl: null,
      description: 'Đội tạm ngừng hoạt động do mùa mưa.',
      captainId: 'user-cap-beta',
      wardId: 'ward-bt',
      status: 'INACTIVE',
      inactiveReason: 'Tạm nghỉ thi đấu đến hết tháng 11/2026.',
      skillLevels: ['INTERMEDIATE'],
      reputationScore: 82,
      ratingAvg: 4.2,
      completionRate: 88,
      matchCount: 7,
      createdAt: '2026-02-01T08:00:00Z'
    },
    {
      id: 'team-low-matches',
      name: 'Tân Binh FC',
      shortName: 'TBFC',
      logoUrl: null,
      description: 'Đội bóng mới thành lập dưới 5 trận (chưa hiển thị tỷ lệ hoàn thành).',
      captainId: 'user-member',
      wardId: 'ward-ak',
      status: 'ACTIVE',
      skillLevels: ['BEGINNER'],
      reputationScore: 100,
      ratingAvg: 5.0,
      completionRate: null, // Under 5 matches
      matchCount: 2,
      createdAt: '2026-11-01T08:00:00Z'
    }
  ],

  teamMembers: [
    { teamId: 'team-alpha', userId: 'user-cap-alpha', role: 'CAPTAIN', joinedAt: '2026-01-15T08:00:00Z', status: 'ACTIVE' },
    { teamId: 'team-alpha', userId: 'user-member', role: 'MEMBER', joinedAt: '2026-01-20T09:00:00Z', status: 'ACTIVE' },
    { teamId: 'team-alpha', userId: 'user-del-99', role: 'MEMBER', joinedAt: '2026-02-01T09:00:00Z', status: 'ACTIVE' },
    { teamId: 'team-beta', userId: 'user-cap-beta', role: 'CAPTAIN', joinedAt: '2026-02-10T09:00:00Z', status: 'ACTIVE' },
    { teamId: 'team-beta', userId: 'user-player', role: 'MEMBER', joinedAt: '2026-03-01T10:00:00Z', status: 'ACTIVE' },
    { teamId: 'team-gamma', userId: 'user-cap-gamma', role: 'CAPTAIN', joinedAt: '2026-03-01T10:00:00Z', status: 'ACTIVE' },
    { teamId: 'team-multi-1', userId: 'user-cap-multi', role: 'CAPTAIN', joinedAt: '2026-04-12T08:00:00Z', status: 'ACTIVE' },
    { teamId: 'team-multi-2', userId: 'user-cap-multi', role: 'CAPTAIN', joinedAt: '2026-05-18T08:00:00Z', status: 'ACTIVE' },
    { teamId: 'team-low-matches', userId: 'user-member', role: 'CAPTAIN', joinedAt: '2026-11-01T08:00:00Z', status: 'ACTIVE' }
  ],

  venues: [
    {
      id: 'ven-anphu',
      name: 'Sân bóng An Phú',
      address: 'Số 8 Đường số 2, Phường An Khánh, TP. Thủ Đức',
      wardId: 'ward-ak',
      coordinates: { lat: 10.793, lng: 106.745 },
      type: 'SEVEN_A_SIDE',
      contactPhone: '0908123456',
      status: 'ACTIVE',
      createdBy: 'user-cap-alpha',
      images: [
        { id: 'img-1', url: 'https://images.unsplash.com/photo-1575361204480-aadea25e6e68?w=500&q=80', isPrimary: true },
        { id: 'img-2', url: 'https://images.unsplash.com/photo-1529900748604-07564a03e7a6?w=500&q=80', isPrimary: false }
      ],
      reportCount: 0
    },
    {
      id: 'ven-binhtrung',
      name: 'Sân bóng Bình Trưng',
      address: '12 Đỗ Xuân Hợp, Phường Bình Trưng Tây, TP. Thủ Đức',
      wardId: 'ward-bt',
      coordinates: { lat: 10.785, lng: 106.762 },
      type: 'SEVEN_A_SIDE',
      contactPhone: '0908654321',
      status: 'ACTIVE',
      createdBy: 'user-cap-beta',
      images: [
        { id: 'img-3', url: 'https://images.unsplash.com/photo-1529900748604-07564a03e7a6?w=500&q=80', isPrimary: true }
      ],
      reportCount: 0
    },
    {
      id: 'ven-thaodien',
      name: 'Sân mini Thảo Điền',
      address: '28 Thảo Điền, Phường Thảo Điền, TP. Thủ Đức',
      wardId: 'ward-td',
      coordinates: { lat: 10.806, lng: 106.738 },
      type: 'FIVE_A_SIDE',
      contactPhone: '0908777888',
      status: 'ACTIVE',
      createdBy: 'user-cap-gamma',
      images: [],
      reportCount: 0
    },
    {
      id: 'ven-inactive-1',
      name: 'Sân Cũ Đóng Cửa',
      address: '99 Đường Đã Giải Tỏa, Quận 1',
      wardId: 'ward-q1',
      coordinates: { lat: 10.776, lng: 106.700 },
      type: 'SEVEN_A_SIDE',
      contactPhone: '0900000000',
      status: 'INACTIVE',
      createdBy: 'user-admin',
      warning: 'Sân đã ngừng hoạt động kinh doanh, không thể chọn cho kèo mới.',
      images: [],
      reportCount: 3
    }
  ],

  matches: [
    {
      id: 'match-a-open',
      kind: 'A',
      hostTeamId: 'team-alpha',
      sportId: 'sport-football',
      format: 'SEVEN_A_SIDE',
      skillLevel: 'INTERMEDIATE',
      venueId: 'ven-anphu',
      matchDate: '2026-11-21',
      startTime: '19:00',
      endTime: '20:30',
      expiresAt: '2026-11-21T18:00:00Z',
      status: 'OPEN',
      totalCost: 600000,
      costSharing: 'SPLIT_50_50',
      hostShare: 300000,
      guestShare: 300000,
      notes: 'Đá giao lưu văn phòng, ưu tiên fair play, trọng tài chia đôi.',
      confirmedGuestTeamId: null,
      createdAt: '2026-11-18T08:00:00Z'
    },
    {
      id: 'match-b-open',
      kind: 'B',
      hostTeamId: 'team-beta',
      sportId: 'sport-football',
      format: 'SEVEN_A_SIDE',
      skillLevel: 'INTERMEDIATE',
      proposedVenues: ['ven-anphu', 'ven-binhtrung'],
      proposedTimeSlots: ['18:00 - 19:30', '19:30 - 21:00'],
      matchDate: '2026-11-21',
      expiresAt: '2026-11-21T17:00:00Z',
      status: 'OPEN',
      totalCost: 600000,
      costSharing: 'SPLIT_50_50',
      depositAmount: 200000,
      depositHolder: 'HOST',
      notes: 'Chọn 1 trong 2 sân và giờ trên, đội nào hợp thì chốt.',
      confirmedGuestTeamId: null,
      createdAt: '2026-11-18T09:00:00Z'
    },
    {
      id: 'match-confirmed-1',
      kind: 'A',
      hostTeamId: 'team-alpha',
      guestTeamId: 'team-beta',
      sportId: 'sport-football',
      format: 'SEVEN_A_SIDE',
      skillLevel: 'INTERMEDIATE',
      venueId: 'ven-anphu',
      matchDate: '2026-11-21',
      startTime: '19:00',
      endTime: '20:30',
      status: 'CONFIRMED',
      totalCost: 600000,
      costSharing: 'SPLIT_50_50',
      createdAt: '2026-11-17T10:00:00Z'
    },
    {
      id: 'match-completed-1',
      kind: 'A',
      hostTeamId: 'team-alpha',
      guestTeamId: 'team-gamma',
      sportId: 'sport-football',
      format: 'SEVEN_A_SIDE',
      skillLevel: 'INTERMEDIATE',
      venueId: 'ven-anphu',
      matchDate: '2026-11-16',
      startTime: '19:00',
      endTime: '20:30',
      status: 'COMPLETED',
      totalCost: 600000,
      costSharing: 'SPLIT_50_50',
      createdAt: '2026-11-15T10:00:00Z'
    }
  ],

  matchRequests: [
    {
      id: 'req-beta-for-a',
      matchId: 'match-a-open',
      requesterTeamId: 'team-beta',
      status: 'PENDING', // PENDING, ACCEPTED, REJECTED, WITHDRAWN, SUPERSEDED, EXPIRED
      message: 'Sài Gòn United muốn giao lưu với Bình Minh FC!',
      createdAt: '2026-11-18T09:30:00Z'
    },
    {
      id: 'req-gamma-for-a',
      matchId: 'match-a-open',
      requesterTeamId: 'team-gamma',
      status: 'PENDING',
      message: 'Thảo Điền FC xin đăng ký đá giao hữu.',
      createdAt: '2026-11-18T09:45:00Z'
    }
  ],

  pairings: [
    {
      id: 'pairing-beta-alpha',
      matchId: 'match-b-open',
      hostTeamId: 'team-beta',
      guestTeamId: 'team-alpha',
      status: 'NEGOTIATING', // NEGOTIATING, CONFIRMED, SUPERSEDED, EXPIRED
      package: {
        venueId: 'ven-anphu',
        timeSlot: '19:30 - 21:00',
        totalCost: 600000,
        costSharing: 'SPLIT_50_50',
        depositAmount: 200000,
        depositHolder: 'team-beta',
        revision: 1,
        confirms: {
          'team-alpha': true,
          'team-beta': false
        }
      },
      createdAt: '2026-11-18T09:15:00Z'
    },
    {
      id: 'pairing-beta-gamma',
      matchId: 'match-b-open',
      hostTeamId: 'team-beta',
      guestTeamId: 'team-gamma',
      status: 'NEGOTIATING',
      package: {
        venueId: 'ven-binhtrung',
        timeSlot: '18:00 - 19:30',
        totalCost: 600000,
        costSharing: 'SPLIT_50_50',
        depositAmount: 0,
        depositHolder: 'team-beta',
        revision: 1,
        confirms: {
          'team-gamma': false,
          'team-beta': false
        }
      },
      createdAt: '2026-11-18T09:20:00Z'
    }
  ],

  recruitments: [
    {
      id: 'rec-1',
      teamId: 'team-alpha',
      matchId: 'match-confirmed-1',
      position: 'GOALKEEPER',
      quantity: 1,
      approvedCount: 0,
      status: 'OPEN', // OPEN, FILLED, CLOSED, EXPIRED
      area: 'Phường An Khánh, TP. Thủ Đức',
      matchDate: '2026-11-21',
      timeSlot: '19:00 - 20:30',
      venueId: 'ven-anphu', // Hidden before approval per AC-P05!
      skillLevel: 'INTERMEDIATE',
      note: 'Cần 1 thủ môn đá sân 7, hỗ trợ nước uống và áo tập.',
      createdAt: '2026-11-18T08:30:00Z'
    },
    {
      id: 'rec-2',
      teamId: 'team-beta',
      externalMatchId: 'ext-match-1',
      position: 'MIDFIELDER',
      quantity: 2,
      approvedCount: 1,
      status: 'OPEN',
      area: 'Phường Bình Trưng Tây, TP. Thủ Đức',
      matchDate: '2026-11-22',
      timeSlot: '18:00 - 19:30',
      venueId: 'ven-binhtrung',
      skillLevel: 'INTERMEDIATE',
      note: 'Tuyển 2 tiền vệ đá giao lưu giải nội bộ.',
      createdAt: '2026-11-18T08:45:00Z'
    }
  ],

  applications: [
    {
      id: 'app-player-rec1',
      recruitmentId: 'rec-1',
      applicantId: 'user-player',
      status: 'PENDING', // PENDING, APPROVED, REJECTED, WITHDRAWN
      message: 'Tôi bắt gôn được 4 năm, phản xạ tốt, muốn tham gia.',
      createdAt: '2026-11-18T09:00:00Z'
    },
    {
      id: 'app-player-rec2',
      recruitmentId: 'rec-2',
      applicantId: 'user-player',
      status: 'APPROVED',
      message: 'Em đá tiền vệ trung tâm, đã được đội trưởng duyệt.',
      createdAt: '2026-11-18T09:10:00Z',
      approvedAt: '2026-11-18T09:25:00Z'
    }
  ],

  externalMatches: [
    {
      id: 'ext-match-1',
      teamId: 'team-beta',
      title: 'Đá giao hữu cúp Tứ Hùng',
      opponentName: 'Bình Trưng FC (Tự do)',
      venueId: 'ven-binhtrung',
      matchDate: '2026-11-22',
      startTime: '18:00',
      endTime: '19:30',
      status: 'SCHEDULED', // SCHEDULED, CANCELLED
      skillLevel: 'INTERMEDIATE',
      createdAt: '2026-11-18T08:40:00Z'
    }
  ],

  conversations: [
    {
      id: 'conv-req-beta',
      contextType: 'MATCH_REQUEST',
      contextId: 'req-beta-for-a',
      participantIds: ['user-cap-alpha', 'user-cap-beta'],
      canSend: true,
      messages: [
        {
          id: 'msg-1',
          senderId: 'user-cap-beta',
          text: 'Chào đội trưởng Bình Minh FC! Đội mình nhận kèo này nhé.',
          timestamp: '2026-11-18T09:31:00Z',
          readBy: ['user-cap-beta', 'user-cap-alpha']
        },
        {
          id: 'msg-2',
          senderId: 'user-cap-alpha',
          text: 'Ok bạn, đội bạn trình độ thế nào? Bên mình vừa sức thôi.',
          timestamp: '2026-11-18T09:33:00Z',
          readBy: ['user-cap-alpha']
        }
      ]
    },
    {
      id: 'conv-pairing-alpha',
      contextType: 'PAIRING',
      contextId: 'pairing-beta-alpha',
      participantIds: ['user-cap-beta', 'user-cap-alpha'],
      canSend: true,
      messages: [
        {
          id: 'msg-p1',
          senderId: 'user-cap-alpha',
          text: 'Mình chọn Sân An Phú lúc 19:30 nhé.',
          timestamp: '2026-11-18T09:16:00Z',
          readBy: ['user-cap-alpha', 'user-cap-beta']
        }
      ]
    },
    {
      id: 'conv-merc-rec1',
      contextType: 'MERCENARY',
      contextId: 'app-player-rec1',
      participantIds: ['user-cap-alpha', 'user-player'],
      canSend: true,
      messages: [
        {
          id: 'msg-m1',
          senderId: 'user-player',
          text: 'Chào anh, em có găng tay và giày sân 7 đầy đủ rồi ạ.',
          timestamp: '2026-11-18T09:05:00Z',
          readBy: ['user-player', 'user-cap-alpha']
        }
      ]
    }
  ],

  notifications: [
    {
      id: 'notif-1',
      userId: 'user-cap-alpha',
      type: 'MATCH_REQUEST_RECEIVED',
      title: 'Yêu cầu ghép kèo mới',
      body: 'Sài Gòn United đã gửi yêu cầu cho kèo ngày 21/11 tại Sân An Phú.',
      referenceType: 'MATCH_REQUEST',
      referenceId: 'req-beta-for-a',
      read: false,
      createdAt: '2026-11-18T09:30:00Z'
    },
    {
      id: 'notif-2',
      userId: 'user-player',
      type: 'MERCENARY_APPLICATION_APPROVED',
      title: 'Đơn ứng tuyển đã được duyệt!',
      body: 'Đội Sài Gòn United đã duyệt đơn ứng tuyển tiền vệ của bạn.',
      referenceType: 'MERCENARY_APPLICATION',
      referenceId: 'app-player-rec2',
      read: false,
      createdAt: '2026-11-18T09:25:00Z'
    },
    {
      id: 'notif-3',
      userId: 'user-cap-alpha',
      type: 'TEAM_INVITATION_RECEIVED',
      title: 'Lời mời gia nhập đội',
      body: 'Bạn nhận được lời mời tham gia thành viên danh dự từ Thảo Điền FC.',
      referenceType: 'TEAM_INVITATION',
      referenceId: 'inv-td-1',
      read: true,
      createdAt: '2026-11-17T14:00:00Z'
    },
    {
      id: 'notif-invalid-target',
      userId: 'user-cap-alpha',
      type: 'MATCH_CANCELLED',
      title: 'Kèo đã bị hủy',
      body: 'Một kèo cũ đã bị giải phóng và không còn khả dụng.',
      referenceType: 'MATCH',
      referenceId: 'match-non-existent-99', // AC-P09: Inaccessible target fallback test
      read: false,
      createdAt: '2026-11-16T10:00:00Z'
    }
  ],

  reports: [
    {
      id: 'rep-noshow-active',
      reporterType: 'TEAM',
      reporterId: 'team-alpha',
      reportedId: 'team-beta',
      matchId: 'match-completed-1',
      type: 'NOSHOW',
      reason: 'Đội bạn không đến sân và không báo trước.',
      status: 'AUTO_ACCEPTED', // AUTO_ACCEPTED, DISPUTED, RESOLVED, REJECTED
      createdAt: '2026-11-18T04:00:00Z', // 6 hours ago (< 24h: DISPUTE ALLOWED!)
      canDisputeUntil: '2026-11-19T04:00:00Z',
      dispute: null
    },
    {
      id: 'rep-noshow-expired',
      reporterType: 'TEAM',
      reporterId: 'team-alpha',
      reportedId: 'team-gamma',
      matchId: 'match-completed-1',
      type: 'NOSHOW',
      reason: 'Đội không đến đúng giờ thi đấu.',
      status: 'RESOLVED',
      createdAt: '2026-11-16T08:00:00Z', // > 24h ago: DISPUTE BLOCKED!
      canDisputeUntil: '2026-11-17T08:00:00Z',
      dispute: null
    },
    {
      id: 'rep-badcomm',
      reporterType: 'INDIVIDUAL',
      reporterId: 'user-cap-alpha',
      reportedId: 'user-player',
      matchId: 'match-confirmed-1',
      type: 'BAD_COMMUNICATION',
      reason: 'Giao tiếp thiếu lịch sự trong phòng chat.',
      status: 'PENDING',
      createdAt: '2026-11-18T08:00:00Z',
      canDisputeUntil: null // AC-P08: Dispute blocked for non-NOSHOW!
    },
    {
      id: 'rep-venue-1',
      reporterType: 'VENUE',
      reporterId: 'user-cap-beta',
      reportedId: 'ven-inactive-1',
      type: 'FAKE_INFO',
      reason: 'Sân bóng đã đóng cửa và đổi thành bãi đỗ xe.',
      status: 'PENDING',
      createdAt: '2026-11-17T11:00:00Z'
    }
  ],

  reviews: [
    {
      id: 'rev-1',
      matchId: 'match-completed-1',
      reviewerId: 'team-alpha',
      revieweeId: 'team-gamma',
      reviewerType: 'TEAM',
      stars: 5,
      score: 10,
      comment: 'Trận đấu rất hay, anh em Thảo Điền đá nhiệt tình và fair play.',
      noShow: false,
      scoreGiven: '3-2',
      createdAt: '2026-11-16T21:00:00Z'
    }
  ],

  sports: [
    { id: 'sport-football', name: 'Bóng đá phong trào (Sân 5 / 7 / 11)', active: true },
    { id: 'sport-futsal', name: 'Futsal (Bóng đá trong nhà)', active: true },
    { id: 'sport-basketball', name: 'Bóng rổ 3x3 & 5x5', active: false }
  ]
};
