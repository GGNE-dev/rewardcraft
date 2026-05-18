'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import axios from 'axios';
import toast from 'react-hot-toast';
import api from '@/lib/api';
import { User, Participation, Challenge } from '@/lib/types';
import CouponModal from '@/components/ui/CouponModal';

const CHALLENGE_API = process.env.NEXT_PUBLIC_CHALLENGE_API_URL || 'http://localhost:8082';

interface ExchangeRecord {
  exchangeId: number;
  rewardName: string;
  rewardId: number;
  status: string;
  exchangedAt: string;
}

export default function ProfilePage() {
  const router = useRouter();
  const [user, setUser] = useState<User | null>(null);
  const [totalPoints, setTotalPoints] = useState<number | null>(null);
  const [participations, setParticipations] = useState<Participation[]>([]);
  const [challenges, setChallenges] = useState<Challenge[]>([]);
  const [exchanges, setExchanges] = useState<ExchangeRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [editingNickname, setEditingNickname] = useState(false);
  const [newNickname, setNewNickname] = useState('');
  const [saving, setSaving] = useState(false);
  const [coupon, setCoupon] = useState<{ exchangeId: number; rewardName: string; exchangedAt: string } | null>(null);

  const userId = typeof window !== 'undefined' ? Number(sessionStorage.getItem('userId')) : 0;

  useEffect(() => {
    if (!sessionStorage.getItem('accessToken')) {
      router.push('/');
      return;
    }

    setLoading(true);

    // 각 호출을 독립적으로 — 하나 실패해도 나머지는 표시
    api.get('/api/users/me')
      .then(res => setUser(res.data.data ?? res.data))
      .catch(() => toast.error('사용자 정보 조회 실패'))
      .finally(() => setLoading(false));

    api.get('/api/users/me/points')
      .then(res => setTotalPoints(res.data.data?.totalPoints ?? 0))
      .catch(() => setTotalPoints(0));

    api.get('/api/rewards/my-exchanges')
      .then(res => setExchanges(res.data.data ?? []))
      .catch(() => {});

    if (userId) {
      axios.get(`${CHALLENGE_API}/api/participations?userId=${userId}`)
        .then(res => setParticipations(res.data.data ?? []))
        .catch(() => {});

      axios.get(`${CHALLENGE_API}/api/challenges`)
        .then(res => setChallenges(res.data.data ?? []))
        .catch(() => {});
    }
  }, [userId, router]);

  const handleNicknameUpdate = async () => {
    if (!newNickname.trim() || !user) return;
    setSaving(true);
    try {
      const res = await api.patch(`/api/users/${user.id}/nickname`, { nickname: newNickname });
      const updated = res.data.data ?? res.data;
      setUser(updated);
      sessionStorage.setItem('nickname', updated.nickname);
      toast.success('닉네임이 변경되었습니다.');
      setEditingNickname(false);
    } catch {
      toast.error('닉네임 변경 실패');
    } finally {
      setSaving(false);
    }
  };

  const getChallengeTitle = (challengeId: number) =>
    challenges.find(c => c.id === challengeId)?.title ?? `챌린지 #${challengeId}`;

  if (loading) {
    return (
      <div className="flex justify-center py-12">
        <div className="animate-spin text-3xl">⏳</div>
      </div>
    );
  }

  if (!user) return null;

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <h1 className="text-2xl font-bold">👤 내 프로필</h1>

      {coupon && (
        <CouponModal
          exchangeId={coupon.exchangeId}
          rewardName={coupon.rewardName}
          exchangedAt={coupon.exchangedAt}
          onClose={() => setCoupon(null)}
        />
      )}

      {/* 기본 정보 */}
      <div className="bg-white rounded-xl border p-6 space-y-4">
        <div className="flex items-center justify-between">
          <div>
            {editingNickname ? (
              <div className="flex items-center gap-2">
                <input
                  type="text"
                  value={newNickname}
                  onChange={e => setNewNickname(e.target.value)}
                  placeholder={user.nickname}
                  className="border rounded px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400"
                  autoFocus
                />
                <button onClick={handleNicknameUpdate} disabled={saving}
                  className="bg-indigo-600 text-white px-3 py-1.5 rounded text-sm disabled:bg-indigo-300">
                  {saving ? '저장 중...' : '저장'}
                </button>
                <button onClick={() => setEditingNickname(false)}
                  className="px-3 py-1.5 border rounded text-sm text-gray-500">취소</button>
              </div>
            ) : (
              <div className="flex items-center gap-3">
                <span className="text-xl font-semibold">{user.nickname}</span>
                <button
                  onClick={() => { setEditingNickname(true); setNewNickname(user.nickname); }}
                  className="text-xs text-indigo-500 hover:underline">수정</button>
              </div>
            )}
            <p className="text-sm text-gray-400 mt-1">{user.email}</p>
          </div>
          <div className="text-right">
            <span className={`text-xs px-2 py-0.5 rounded-full ${
              user.role === 'ADMIN' ? 'bg-red-100 text-red-700'
              : user.role === 'OPERATOR' ? 'bg-yellow-100 text-yellow-700'
              : 'bg-gray-100 text-gray-600'}`}>
              {user.role}
            </span>
            <p className="text-xs text-gray-400 mt-1">{user.provider} 로그인</p>
          </div>
        </div>

        <div className="bg-indigo-50 rounded-lg p-4 flex items-center justify-between">
          <span className="text-sm text-indigo-700 font-medium">총 보유 포인트</span>
          <span className="text-2xl font-bold text-indigo-600">
            {totalPoints !== null ? totalPoints.toLocaleString() : '...'}점
          </span>
        </div>
      </div>

      {/* 교환 내역 */}
      <div className="bg-white rounded-xl border p-6 space-y-3">
        <h2 className="font-semibold text-gray-700">🎁 교환 내역</h2>
        {exchanges.length === 0 ? (
          <p className="text-sm text-gray-400">아직 교환한 리워드가 없습니다.</p>
        ) : (
          <ul className="space-y-2">
            {exchanges.map(e => (
              <li key={e.exchangeId}
                className="flex items-center justify-between px-4 py-3 bg-gray-50 rounded-lg cursor-pointer hover:bg-indigo-50 transition"
                onClick={() => setCoupon({ exchangeId: e.exchangeId, rewardName: e.rewardName, exchangedAt: e.exchangedAt })}
              >
                <div>
                  <span className="text-sm font-medium text-gray-700">{e.rewardName}</span>
                  <p className="text-xs text-gray-400 mt-0.5">
                    {e.exchangedAt ? new Date(e.exchangedAt).toLocaleDateString('ko-KR') : ''}
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-xs bg-green-100 text-green-700 px-2 py-0.5 rounded-full">{e.status}</span>
                  <span className="text-xs text-indigo-500 font-medium">쿠폰 보기 →</span>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>

      {/* 참여 중인 챌린지 */}
      <div className="bg-white rounded-xl border p-6 space-y-3">
        <h2 className="font-semibold text-gray-700">🏅 참여 중인 챌린지</h2>
        {participations.length === 0 ? (
          <p className="text-sm text-gray-400">아직 참여한 챌린지가 없습니다.</p>
        ) : (
          <ul className="space-y-2">
            {participations.map(p => (
              <li key={p.id} className="flex items-center justify-between px-4 py-3 bg-gray-50 rounded-lg">
                <span className="text-sm font-medium text-gray-700">{getChallengeTitle(p.challengeId)}</span>
                <span className="text-sm font-bold text-indigo-600">{p.totalPoints.toLocaleString()}점</span>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
