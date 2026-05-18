'use client';

import { useState, useEffect } from 'react';
import toast from 'react-hot-toast';
import api from '@/lib/api';
import { Reward } from '@/lib/types';
import DevNote from '@/components/ui/DevNote';

export default function AdminRewardsPage() {
  const [rewards, setRewards] = useState<Reward[]>([]);
  const [loading, setLoading] = useState(true);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [createForm, setCreateForm] = useState({ name: '', requiredPoints: '', stock: '' });
  const [creating, setCreating] = useState(false);

  const fetchRewards = () =>
    api.get('/api/rewards')
      .then(res => setRewards(res.data.data ?? res.data ?? []))
      .catch(() => toast.error('리워드 목록 조회 실패'))
      .finally(() => setLoading(false));

  useEffect(() => { fetchRewards(); }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setCreating(true);
    try {
      const res = await api.post('/api/rewards', {
        name: createForm.name,
        requiredPoints: Number(createForm.requiredPoints),
        stock: Number(createForm.stock),
      });
      setRewards(prev => [...prev, res.data.data]);
      toast.success('리워드가 등록되었습니다.');
      setCreateForm({ name: '', requiredPoints: '', stock: '' });
      setShowCreateForm(false);
    } catch {
      toast.error('리워드 등록 실패 (PERM_REWARD_MANAGE 필요)');
    } finally {
      setCreating(false);
    }
  };

  const handleAdjustStock = async (reward: Reward, delta: number) => {
    const label = delta > 0 ? `+${delta}` : `${delta}`;
    if (!window.confirm(`[${reward.name}] 재고 ${label} 조정하시겠습니까?`)) return;
    try {
      const res = await api.patch(`/api/rewards/${reward.id}/stock`, { delta });
      const updated: Reward = res.data.data;
      setRewards(prev => prev.map(r => r.id === reward.id ? updated : r));
      toast.success(`재고 조정 완료: ${updated.remainingStock}개 남음`);
    } catch {
      toast.error('재고 조정 실패');
    }
  };

  const stockPercent = (r: Reward) =>
    r.totalStock > 0 ? Math.round((r.remainingStock / r.totalStock) * 100) : 0;

  return (
    <div className="space-y-4">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold">🎁 리워드 관리</h1>
        <button
          onClick={() => setShowCreateForm(v => !v)}
          className="bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm"
        >
          + 리워드 등록
        </button>
      </div>

      {showCreateForm && (
        <form onSubmit={handleCreate} className="bg-indigo-50 border border-indigo-200 rounded-xl p-5 space-y-3">
          <h2 className="font-semibold text-indigo-800">새 리워드</h2>
          <input
            required type="text" placeholder="리워드 이름"
            value={createForm.name}
            onChange={e => setCreateForm(f => ({ ...f, name: e.target.value }))}
            className="w-full border rounded px-3 py-2 text-sm"
          />
          <div className="flex gap-3">
            <input
              required type="number" min="1" placeholder="필요 포인트"
              value={createForm.requiredPoints}
              onChange={e => setCreateForm(f => ({ ...f, requiredPoints: e.target.value }))}
              className="flex-1 border rounded px-3 py-2 text-sm"
            />
            <input
              required type="number" min="1" placeholder="초기 재고"
              value={createForm.stock}
              onChange={e => setCreateForm(f => ({ ...f, stock: e.target.value }))}
              className="flex-1 border rounded px-3 py-2 text-sm"
            />
          </div>
          <div className="flex gap-2">
            <button type="submit" disabled={creating}
              className="flex-1 bg-indigo-600 text-white py-2 rounded-lg text-sm disabled:bg-indigo-300">
              {creating ? '등록 중...' : '등록'}
            </button>
            <button type="button" onClick={() => setShowCreateForm(false)}
              className="py-2 px-4 border rounded-lg text-sm text-gray-500">
              취소
            </button>
          </div>
        </form>
      )}

      {loading ? (
        <div className="text-center py-8 text-gray-400">로딩 중...</div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {rewards.map(r => (
            <div key={r.id} className="bg-white rounded-xl border p-5 space-y-3">
              <div className="flex justify-between items-start">
                <h3 className="font-semibold text-gray-800">{r.name}</h3>
                {r.remainingStock === 0 && (
                  <span className="text-xs bg-red-100 text-red-600 px-2 py-0.5 rounded-full">품절</span>
                )}
              </div>
              <div className="text-sm text-gray-500">
                필요 포인트: <span className="font-medium text-indigo-600">{r.requiredPoints.toLocaleString()}P</span>
              </div>
              <div>
                <div className="flex justify-between text-xs text-gray-400 mb-1">
                  <span>재고</span>
                  <span>{r.remainingStock} / {r.totalStock} ({stockPercent(r)}%)</span>
                </div>
                <div className="w-full bg-gray-100 rounded-full h-2">
                  <div
                    className={`h-2 rounded-full transition-all ${stockPercent(r) > 30 ? 'bg-green-400' : stockPercent(r) > 10 ? 'bg-yellow-400' : 'bg-red-400'}`}
                    style={{ width: `${stockPercent(r)}%` }}
                  />
                </div>
              </div>
              {/* 재고 조정 버튼 */}
              <div className="flex gap-2 pt-1">
                <button onClick={() => handleAdjustStock(r, 10)}
                  className="flex-1 py-1.5 rounded border border-green-300 text-green-700 text-xs hover:bg-green-50">
                  +10 추가
                </button>
                <button onClick={() => handleAdjustStock(r, 1)}
                  className="flex-1 py-1.5 rounded border border-green-200 text-green-600 text-xs hover:bg-green-50">
                  +1 추가
                </button>
                <button onClick={() => handleAdjustStock(r, -1)}
                  disabled={r.remainingStock === 0}
                  className="flex-1 py-1.5 rounded border border-red-200 text-red-600 text-xs hover:bg-red-50 disabled:opacity-40">
                  -1 차감
                </button>
                <button onClick={() => handleAdjustStock(r, -10)}
                  disabled={r.remainingStock < 10}
                  className="flex-1 py-1.5 rounded border border-red-300 text-red-700 text-xs hover:bg-red-50 disabled:opacity-40">
                  -10 차감
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      <DevNote>
        <strong>분산 락 시연:</strong> 리워드 탭에서 다수 유저가 동시 교환 시도 →
        Redis Redisson 락 직렬화 → 재고 0 시 409, 락 경합 시 429
      </DevNote>
    </div>
  );
}
