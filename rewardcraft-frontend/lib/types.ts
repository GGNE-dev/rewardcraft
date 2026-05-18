export interface User {
  id: number;
  email: string;
  nickname: string;
  role: 'USER' | 'OPERATOR' | 'ADMIN';
  provider: 'KAKAO' | 'GOOGLE';
  banned: boolean;
  createdAt: string;
}

export interface RankingEntry {
  rank: number;
  userId: number;
  score: number;
  nickname?: string;
}

export interface RankingResponse {
  entries: RankingEntry[];
}

export interface Reward {
  id: number;
  name: string;
  requiredPoints: number;
  totalStock: number;
  remainingStock: number;
}

export interface ExchangeResult {
  exchangeId: number;
  remainingStock: number;
}

export interface Challenge {
  id: number;
  title: string;
  description: string;
  startAt: string;
  endAt: string;
  status: 'UPCOMING' | 'ACTIVE' | 'ENDED';
}

export interface Participation {
  id: number;
  userId: number;
  challengeId: number;
  totalPoints: number;
}

export interface MissionLog {
  id: number;
  participationId: number;
  completedAt: string;
  pointsEarned: number;
  memo: string | null;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
}

export interface PendingMission {
  id: number;
  participationId: number;
  userId: number;
  challengeId: number;
  challengeTitle: string;
  pointsEarned: number;
  memo: string | null;
  submittedAt: string;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
}

export interface ApiError {
  code: string;
  message: string;
  status: number;
}
