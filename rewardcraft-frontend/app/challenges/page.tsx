'use client';

import { useState, useEffect, useCallback } from 'react';
import api from '@/lib/api';
import toast from 'react-hot-toast';
import { Challenge, Participation, MissionLog } from '@/lib/types';

// 미션 완료 신청 시 선택 가능한 포인트 옵션
const POINT_OPTIONS = [10, 30, 50, 100];

export default function ChallengesPage() {
  const [challenges, setChallenges] = useState<Challenge[]>([]);
  const [participations, setParticipations] = useState<Participation[]>([]);
  const [loading, setLoading] = useState(true);
  const [joiningId, setJoiningId] = useState<number | null>(null);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [userId, setUserId] = useState(0);

  useEffect(() => {
    setIsLoggedIn(!!sessionStorage.getItem('accessToken'));
    setUserId(Number(sessionStorage.getItem('userId')));
  }, []);

  const fetchData = useCallback(async () => {
    try {
      const challengeRes = await api.get('/api/challenges');
      setChallenges(challengeRes.data.data ?? []);

      if (userId) {
        const partRes = await api.get(`/api/participations?userId=${userId}`);
        setParticipations(partRes.data.data ?? []);
      }
    } catch {
      // challenge-service 미기동 시 조용히 실패
    } finally {
      setLoading(false);
    }
  }, [userId]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const handleJoin = async (challengeId: number) => {
    if (!isLoggedIn || !userId) {
      toast.error('로그인이 필요합니다.');
      return;
    }
    setJoiningId(challengeId);
    try {
      const res = await api.post('/api/participations', { userId, challengeId });
      const newPart: Participation = res.data.data;
      setParticipations((prev) => [...prev, newPart]);
      toast.success('챌린지에 참여했습니다! 🎉');
    } catch (err: unknown) {
      const status = (err as { response?: { status?: number } })?.response?.status;
      if (status === 409) {
        toast.error('이미 참여 중인 챌린지입니다.');
      } else {
        toast.error('참여 중 오류가 발생했습니다.');
      }
    } finally {
      setJoiningId(null);
    }
  };

  const getParticipation = (challengeId: number) =>
    participations.find((p) => p.challengeId === challengeId) ?? null;

  if (loading) {
    return (
      <div className="flex justify-center py-12">
        <div className="animate-spin text-3xl">⏳</div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {!isLoggedIn && (
        <div className="bg-yellow-50 border border-yellow-300 rounded-lg px-4 py-3 flex items-center justify-between">
          <span className="text-sm text-yellow-800">로그인 후 챌린지에 참여할 수 있어요.</span>
          <a href="/" className="text-sm font-medium text-yellow-700 underline">로그인하기</a>
        </div>
      )}

      <h1 className="text-2xl font-bold">🏅 챌린지 목록</h1>

      {challenges.length === 0 ? (
        <p className="text-gray-400 text-center py-12">현재 진행 중인 챌린지가 없습니다.</p>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {challenges.map((challenge) => {
            const participation = getParticipation(challenge.id);
            return (
              <ChallengeCard
                key={challenge.id}
                challenge={challenge}
                participation={participation}
                isJoining={joiningId === challenge.id}
                isLoggedIn={isLoggedIn}
                onJoin={() => handleJoin(challenge.id)}
                onMissionSubmitted={(pts) => {
                  if (!participation) return;
                  // PENDING 상태이므로 totalPoints는 아직 변경하지 않음
                  // 승인 후 실제 포인트가 반영됨
                  void pts;
                }}
              />
            );
          })}
        </div>
      )}
    </div>
  );
}

function ChallengeCard({
  challenge,
  participation,
  isJoining,
  isLoggedIn,
  onJoin,
  onMissionSubmitted,
}: {
  challenge: Challenge;
  participation: Participation | null;
  isJoining: boolean;
  isLoggedIn: boolean;
  onJoin: () => void;
  onMissionSubmitted: (pts: number) => void;
}) {
  const [showMissionForm, setShowMissionForm] = useState(false);
  const [selectedPoints, setSelectedPoints] = useState<number | null>(null);
  const [missionMemo, setMissionMemo] = useState('');
  const [completing, setCompleting] = useState(false);
  const [missions, setMissions] = useState<MissionLog[]>([]);
  const [showHistory, setShowHistory] = useState(false);
  const [loadingHistory, setLoadingHistory] = useState(false);

  const handleSubmit = async () => {
    if (!selectedPoints) {
      toast.error('포인트를 선택해주세요.');
      return;
    }
    if (!participation) return;

    setCompleting(true);
    try {
      const res = await api.post(
        `/api/participations/${participation.id}/missions`,
        { points: selectedPoints, memo: missionMemo || null }
      );
      const log: MissionLog = res.data.data;
      toast.success('미션 완료 신청이 접수됐습니다! 운영자 승인 후 포인트가 지급됩니다.');
      setMissions((prev) => [log, ...prev]);
      onMissionSubmitted(selectedPoints);
      setSelectedPoints(null);
      setMissionMemo('');
      setShowMissionForm(false);
    } catch {
      toast.error('미션 완료 처리 중 오류가 발생했습니다.');
    } finally {
      setCompleting(false);
    }
  };

  const loadHistory = async () => {
    if (!participation) return;
    setLoadingHistory(true);
    try {
      const res = await api.get(
        `/api/participations/${participation.id}/missions`
      );
      setMissions(res.data.data ?? []);
      setShowHistory(true);
    } catch {
      toast.error('미션 내역을 불러오지 못했습니다.');
    } finally {
      setLoadingHistory(false);
    }
  };

  const statusLabel: Record<string, string> = {
    ACTIVE: '진행 중',
    UPCOMING: '예정',
    ENDED: '종료',
  };
  const statusColor: Record<string, string> = {
    ACTIVE: 'bg-green-100 text-green-700',
    UPCOMING: 'bg-blue-100 text-blue-700',
    ENDED: 'bg-gray-100 text-gray-500',
  };

  const missionStatusBadge: Record<string, { label: string; color: string }> = {
    PENDING:  { label: '승인 대기', color: 'text-yellow-600 bg-yellow-50' },
    APPROVED: { label: '승인됨',   color: 'text-green-600 bg-green-50'  },
    REJECTED: { label: '거절됨',   color: 'text-red-500 bg-red-50'      },
  };

  return (
    <div className="bg-white rounded-xl shadow-sm border p-5 flex flex-col gap-3">
      <div className="flex justify-between items-start">
        <h3 className="font-semibold text-gray-800">{challenge.title}</h3>
        <span className={`text-xs px-2 py-0.5 rounded-full ${statusColor[challenge.status] ?? 'bg-gray-100 text-gray-500'}`}>
          {statusLabel[challenge.status] ?? challenge.status}
        </span>
      </div>

      {participation && (
        <div className="text-sm text-indigo-600 font-medium bg-indigo-50 rounded-lg px-3 py-1.5">
          참여 중 · 누적 포인트: <strong>{participation.totalPoints.toLocaleString()}점</strong>
        </div>
      )}

      <div className="flex gap-2 mt-auto flex-wrap">
        {!participation && isLoggedIn && challenge.status === 'ACTIVE' && (
          <button
            onClick={onJoin}
            disabled={isJoining}
            className="flex-1 py-2 rounded-lg bg-indigo-600 text-white text-sm font-medium hover:bg-indigo-700 disabled:bg-indigo-300 transition"
          >
            {isJoining ? '참여 중...' : '참여하기'}
          </button>
        )}

        {participation && challenge.status === 'ACTIVE' && (
          <button
            onClick={() => setShowMissionForm((v) => !v)}
            className="flex-1 py-2 rounded-lg bg-green-600 text-white text-sm font-medium hover:bg-green-700 transition"
          >
            🎯 미션 완료 신청
          </button>
        )}

        {participation && (
          <button
            onClick={showHistory ? () => setShowHistory(false) : loadHistory}
            disabled={loadingHistory}
            className="py-2 px-3 rounded-lg border border-gray-200 text-gray-600 text-sm hover:bg-gray-50 transition"
          >
            {showHistory ? '내역 닫기' : loadingHistory ? '...' : '내역 보기'}
          </button>
        )}
      </div>

      {showMissionForm && (
        <div className="border border-green-200 rounded-lg p-3 space-y-3 bg-green-50">
          <p className="text-xs font-semibold text-green-700">미션 완료 신청</p>
          <p className="text-xs text-green-600">운영자 승인 후 포인트가 지급됩니다.</p>

          {/* 포인트 프리셋 선택 */}
          <div className="flex gap-2 flex-wrap">
            {POINT_OPTIONS.map((pt) => (
              <button
                key={pt}
                onClick={() => setSelectedPoints(pt)}
                className={`px-3 py-1.5 rounded-lg text-sm font-medium border transition ${
                  selectedPoints === pt
                    ? 'bg-green-600 text-white border-green-600'
                    : 'bg-white text-gray-700 border-gray-300 hover:border-green-400'
                }`}
              >
                +{pt}점
              </button>
            ))}
          </div>

          <input
            type="text"
            placeholder="메모 (선택) — 오늘 운동 인증 등"
            value={missionMemo}
            onChange={(e) => setMissionMemo(e.target.value)}
            className="w-full border rounded px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-green-400"
          />
          <div className="flex gap-2">
            <button
              onClick={handleSubmit}
              disabled={completing || !selectedPoints}
              className="flex-1 py-1.5 rounded bg-green-600 text-white text-sm font-medium hover:bg-green-700 disabled:bg-green-300 transition"
            >
              {completing ? '처리 중...' : '신청하기'}
            </button>
            <button
              onClick={() => { setShowMissionForm(false); setSelectedPoints(null); }}
              className="py-1.5 px-3 rounded border text-sm text-gray-500 hover:bg-gray-50 transition"
            >
              취소
            </button>
          </div>
        </div>
      )}

      {showHistory && (
        <div className="border rounded-lg p-3 space-y-1.5 max-h-48 overflow-y-auto">
          <p className="text-xs font-semibold text-gray-500">미션 신청 내역</p>
          {missions.length === 0 ? (
            <p className="text-xs text-gray-400">아직 신청한 미션이 없습니다.</p>
          ) : (
            missions.map((m) => {
              const badge = missionStatusBadge[m.status] ?? { label: m.status, color: 'text-gray-500 bg-gray-50' };
              return (
                <div key={m.id} className="flex justify-between items-center text-xs text-gray-600 py-1">
                  <div>
                    <span>{new Date(m.completedAt).toLocaleDateString('ko-KR')}</span>
                    {m.memo && <span className="text-gray-400 ml-1">· {m.memo}</span>}
                  </div>
                  <div className="flex items-center gap-1.5">
                    <span className={`px-1.5 py-0.5 rounded text-xs ${badge.color}`}>{badge.label}</span>
                    <span className="font-medium text-indigo-600">+{m.pointsEarned}점</span>
                  </div>
                </div>
              );
            })
          )}
        </div>
      )}
    </div>
  );
}
