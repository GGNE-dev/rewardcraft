'use client';

import { useState, useEffect } from 'react';
import toast from 'react-hot-toast';
import api from '@/lib/api';
import { Reward } from '@/lib/types';
import DevNote from '@/components/ui/DevNote';
import CouponModal from '@/components/ui/CouponModal';

export default function RewardsPage() {
  const [rewards, setRewards] = useState<Reward[]>([]);
  const [myPoints, setMyPoints] = useState(0);
  const [loading, setLoading] = useState(true);
  const [exchangingId, setExchangingId] = useState<number | null>(null);
  const [coupon, setCoupon] = useState<{ exchangeId: number; rewardName: string; exchangedAt: string } | null>(null);
  const isLoggedIn =
    typeof window !== 'undefined' ? !!sessionStorage.getItem('accessToken') : false;

  const fetchData = async () => {
    try {
      // 리워드 목록은 비로그인도 조회 가능 (SecurityConfig permitAll)
      const rewardRes = await api.get('/api/rewards');
      setRewards(rewardRes.data.data ?? rewardRes.data.rewards ?? rewardRes.data);
    } catch {
    } finally {
      setLoading(false);
    }

    // 포인트는 로그인 상태에서만 조회 — 비로그인 시 불필요한 401 방지
    if (isLoggedIn) {
      try {
        const pointRes = await api.get('/api/users/me/points');
        setMyPoints(pointRes.data.data?.totalPoints ?? 0);
      } catch {
      }
    }
  };

  useEffect(() => {
    fetchData();
    // 30초마다 재고 갱신 — 다른 사람의 교환으로 수량 감소 반영
    // 5초마다 재고 갱신 — 다른 사람의 교환으로 수량 감소 반영
    const interval = setInterval(fetchData, 5000);
    return () => clearInterval(interval);
  }, []);

  const handleExchange = async (rewardId: number, requiredPoints: number) => {
    if (myPoints < requiredPoints) {
      toast.error('포인트가 부족합니다.');
      return;
    }
    if (!window.confirm(`${requiredPoints.toLocaleString()}포인트를 사용해서 교환하시겠습니까?`)) {
      return;
    }

    setExchangingId(rewardId);
    try {
      const { data } = await api.post(`/api/rewards/${rewardId}/exchange`);
      const reward = rewards.find(r => r.id === rewardId);
      setRewards((prev) =>
        prev.map((r) =>
          r.id === rewardId ? { ...r, remainingStock: data.data?.remainingStock ?? data.remainingStock } : r
        )
      );
      setMyPoints((prev) => prev - requiredPoints);
      // 교환 직후 쿠폰 모달 표시
      if (reward) {
        setCoupon({ exchangeId: data.data?.exchangeId ?? Date.now(), rewardName: reward.name, exchangedAt: new Date().toISOString() });
      }
      toast.success('교환 완료! 🎉');
    } catch (err: unknown) {
      const status = (err as { response?: { status?: number; data?: { message?: string } } })?.response?.status;
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message;
      if (status === 409) {
        toast.error('품절됐습니다. 다음 기회에!');
        setRewards((prev) =>
          prev.map((r) => (r.id === rewardId ? { ...r, remainingStock: 0 } : r))
        );
      } else if (status === 429) {
        toast.error('잠시 후 다시 시도해주세요. (동시 요청 제한)');
      } else {
        toast.error(msg ?? '교환 중 오류가 발생했습니다.');
      }
    } finally {
      setExchangingId(null);
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
      {coupon && (
        <CouponModal
          exchangeId={coupon.exchangeId}
          rewardName={coupon.rewardName}
          exchangedAt={coupon.exchangedAt}
          onClose={() => setCoupon(null)}
        />
      )}
      {!isLoggedIn && (
        <div className="bg-yellow-50 border border-yellow-300 rounded-lg px-4 py-3 flex items-center justify-between">
          <span className="text-sm text-yellow-800">로그인 후 포인트로 리워드를 교환할 수 있어요.</span>
          <a href="/" className="text-sm font-medium text-yellow-700 underline">로그인하기</a>
        </div>
      )}
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold">🎁 리워드 교환소</h1>
        <div className="bg-indigo-50 border border-indigo-200 px-4 py-2 rounded-lg">
          <span className="text-sm text-indigo-600 font-medium">
            보유 포인트: <strong>{myPoints.toLocaleString()}점</strong>
          </span>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {rewards.map((reward) => (
          <RewardCard
            key={reward.id}
            reward={reward}
            myPoints={myPoints}
            isExchanging={exchangingId === reward.id}
            onExchange={() => handleExchange(reward.id, reward.requiredPoints)}
          />
        ))}
      </div>

      <DevNote>
        교환 요청은 <strong>Redis Redisson 분산 락</strong>으로 직렬화됩니다.
        재고 0 → 409, 락 경합(동시 요청) → 429
      </DevNote>
    </div>
  );
}

function RewardCard({
  reward,
  myPoints,
  isExchanging,
  onExchange,
}: {
  reward: Reward;
  myPoints: number;
  isExchanging: boolean;
  onExchange: () => void;
}) {
  const isSoldOut = reward.remainingStock <= 0;
  const canAfford = myPoints >= reward.requiredPoints;
  const stockPercent = (reward.remainingStock / reward.totalStock) * 100;

  return (
    <div
      className={`bg-white rounded-xl shadow-sm border p-5 flex flex-col gap-3 ${
        isSoldOut ? 'opacity-60' : ''
      }`}
    >
      <div className="flex justify-between items-start">
        <h3 className="font-semibold text-gray-800">{reward.name}</h3>
        {isSoldOut && (
          <span className="text-xs bg-red-100 text-red-600 px-2 py-0.5 rounded-full">품절</span>
        )}
      </div>

      <div className="text-sm text-gray-600">
        필요 포인트:{' '}
        <span className={`font-bold ${canAfford ? 'text-indigo-600' : 'text-red-500'}`}>
          {reward.requiredPoints.toLocaleString()}점
        </span>
      </div>

      <div>
        <div className="flex justify-between text-xs text-gray-400 mb-1">
          <span>남은 수량</span>
          <span>
            {reward.remainingStock} / {reward.totalStock}
          </span>
        </div>
        <div className="w-full bg-gray-100 rounded-full h-2">
          <div
            className={`h-2 rounded-full transition-all duration-300 ${
              stockPercent > 30
                ? 'bg-green-400'
                : stockPercent > 10
                ? 'bg-yellow-400'
                : 'bg-red-400'
            }`}
            style={{ width: `${stockPercent}%` }}
          />
        </div>
        {!isSoldOut && stockPercent <= 10 && (
          <p className="text-xs text-red-500 mt-1">⚠️ 곧 품절됩니다!</p>
        )}
      </div>

      <button
        onClick={onExchange}
        disabled={isSoldOut || !canAfford || isExchanging}
        className={`mt-auto py-2 rounded-lg font-medium text-sm transition ${
          isSoldOut
            ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
            : !canAfford
            ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
            : isExchanging
            ? 'bg-indigo-300 text-white cursor-wait'
            : 'bg-indigo-600 text-white hover:bg-indigo-700'
        }`}
      >
        {isExchanging
          ? '처리 중...'
          : isSoldOut
          ? '품절'
          : !canAfford
          ? '포인트 부족'
          : '교환하기'}
      </button>
    </div>
  );
}
