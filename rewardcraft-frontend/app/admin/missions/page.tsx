'use client';

import { useState, useEffect, useCallback } from 'react';
import axios from 'axios';
import toast from 'react-hot-toast';
import { PendingMission } from '@/lib/types';
import DevNote from '@/components/ui/DevNote';

const CHALLENGE_API = process.env.NEXT_PUBLIC_CHALLENGE_API_URL || 'http://localhost:8082';

export default function AdminMissionsPage() {
  const [missions, setMissions] = useState<PendingMission[]>([]);
  const [loading, setLoading] = useState(true);
  const [processingId, setProcessingId] = useState<number | null>(null);

  const fetchPending = useCallback(async () => {
    try {
      const res = await axios.get(`${CHALLENGE_API}/api/missions/pending`);
      setMissions(res.data.data ?? []);
    } catch {
      toast.error('미션 목록을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchPending();
  }, [fetchPending]);

  const handleApprove = async (id: number) => {
    setProcessingId(id);
    try {
      await axios.patch(`${CHALLENGE_API}/api/missions/${id}/approve`);
      setMissions((prev) => prev.filter((m) => m.id !== id));
      toast.success('미션이 승인됐습니다. 포인트가 지급됩니다.');
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message;
      toast.error(msg ?? '승인 중 오류가 발생했습니다.');
    } finally {
      setProcessingId(null);
    }
  };

  const handleReject = async (id: number) => {
    if (!window.confirm('이 미션 신청을 거절하시겠습니까?')) return;
    setProcessingId(id);
    try {
      await axios.patch(`${CHALLENGE_API}/api/missions/${id}/reject`);
      setMissions((prev) => prev.filter((m) => m.id !== id));
      toast.success('미션 신청이 거절됐습니다.');
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message;
      toast.error(msg ?? '거절 중 오류가 발생했습니다.');
    } finally {
      setProcessingId(null);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center py-12">
        <div className="animate-spin text-3xl">⏳</div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold text-gray-800">🎯 미션 승인 관리</h1>
        <button
          onClick={fetchPending}
          className="text-sm text-indigo-600 hover:underline"
        >
          새로고침
        </button>
      </div>

      <DevNote>
        미션 완료 신청은 <strong>PENDING</strong> 상태로 저장됩니다.
        승인 시 포인트 지급 + <strong>Kafka MissionCompletedEvent</strong> 발행 → 랭킹 갱신
      </DevNote>

      {missions.length === 0 ? (
        <div className="bg-white rounded-xl border p-8 text-center text-gray-400">
          <p className="text-4xl mb-3">✅</p>
          <p className="text-sm">승인 대기 중인 미션이 없습니다.</p>
        </div>
      ) : (
        <div className="space-y-3">
          {missions.map((m) => (
            <div key={m.id} className="bg-white rounded-xl border p-4 flex items-start justify-between gap-4">
              <div className="space-y-1 flex-1 min-w-0">
                <div className="flex items-center gap-2 flex-wrap">
                  <span className="text-xs bg-indigo-100 text-indigo-700 px-2 py-0.5 rounded-full font-medium">
                    {m.challengeTitle}
                  </span>
                  <span className="text-xs text-gray-400">유저 #{m.userId}</span>
                  <span className="text-xs text-gray-400">
                    {new Date(m.submittedAt).toLocaleString('ko-KR')}
                  </span>
                </div>
                <div className="flex items-center gap-2 mt-1">
                  <span className="text-sm font-bold text-green-600">+{m.pointsEarned}점</span>
                  {m.memo && (
                    <span className="text-sm text-gray-600 truncate">· {m.memo}</span>
                  )}
                </div>
              </div>

              <div className="flex gap-2 shrink-0">
                <button
                  onClick={() => handleApprove(m.id)}
                  disabled={processingId === m.id}
                  className="px-3 py-1.5 rounded-lg bg-green-600 text-white text-sm font-medium hover:bg-green-700 disabled:bg-green-300 transition"
                >
                  {processingId === m.id ? '...' : '승인'}
                </button>
                <button
                  onClick={() => handleReject(m.id)}
                  disabled={processingId === m.id}
                  className="px-3 py-1.5 rounded-lg border border-red-300 text-red-500 text-sm font-medium hover:bg-red-50 disabled:opacity-50 transition"
                >
                  거절
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
