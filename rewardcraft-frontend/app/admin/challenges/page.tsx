'use client';

import { useState, useEffect } from 'react';
import axios from 'axios';
import toast from 'react-hot-toast';
import { Challenge } from '@/lib/types';
import api from '@/lib/api';

const CHALLENGE_API = process.env.NEXT_PUBLIC_CHALLENGE_API_URL || 'http://localhost:8082';

export default function AdminChallengesPage() {
  const [challenges, setChallenges] = useState<Challenge[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({
    title: '',
    description: '',
    startAt: '',
    endAt: '',
  });
  const [creating, setCreating] = useState(false);

  useEffect(() => {
    axios.get(`${CHALLENGE_API}/api/challenges`)
      .then(res => setChallenges(res.data.data ?? []))
      .catch(() => toast.error('챌린지 목록 조회 실패'))
      .finally(() => setLoading(false));
  }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setCreating(true);
    try {
      const res = await axios.post(`${CHALLENGE_API}/api/challenges`, {
        title: form.title,
        description: form.description,
        startAt: form.startAt ? `${form.startAt}:00` : null,
        endAt: form.endAt ? `${form.endAt}:00` : null,
      });
      const created: Challenge = res.data.data;
      setChallenges(prev => [created, ...prev]);
      toast.success('챌린지가 생성되었습니다.');
      setForm({ title: '', description: '', startAt: '', endAt: '' });
      setShowForm(false);
    } catch {
      toast.error('챌린지 생성 실패');
    } finally {
      setCreating(false);
    }
  };

  // 백엔드 audit log 기록 확인용 — PERM_CHALLENGE_CREATE 권한 검증
  const handleViewAccess = async () => {
    try {
      await api.get('/api/admin/challenges');
      toast.success('챌린지 관리 접근 감사 로그가 기록되었습니다.');
    } catch {
      toast.error('접근 권한이 없습니다.');
    }
  };

  const statusLabel: Record<string, string> = { ACTIVE: '진행 중', UPCOMING: '예정', ENDED: '종료' };
  const statusColor: Record<string, string> = {
    ACTIVE: 'bg-green-100 text-green-700',
    UPCOMING: 'bg-blue-100 text-blue-700',
    ENDED: 'bg-gray-100 text-gray-500',
  };

  return (
    <div className="space-y-4">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold">🏅 챌린지 관리</h1>
        <div className="flex gap-2">
          <button
            onClick={handleViewAccess}
            className="text-sm px-3 py-2 rounded-lg border border-gray-200 text-gray-600 hover:bg-gray-50"
          >
            감사 로그 기록
          </button>
          <button
            onClick={() => setShowForm(v => !v)}
            className="bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm"
          >
            + 챌린지 생성
          </button>
        </div>
      </div>

      {showForm && (
        <form onSubmit={handleCreate} className="bg-indigo-50 border border-indigo-200 rounded-xl p-5 space-y-3">
          <h2 className="font-semibold text-indigo-800">새 챌린지</h2>
          <input
            required
            type="text"
            placeholder="제목"
            value={form.title}
            onChange={e => setForm(f => ({ ...f, title: e.target.value }))}
            className="w-full border rounded px-3 py-2 text-sm"
          />
          <input
            type="text"
            placeholder="설명 (선택)"
            value={form.description}
            onChange={e => setForm(f => ({ ...f, description: e.target.value }))}
            className="w-full border rounded px-3 py-2 text-sm"
          />
          <div className="flex gap-3">
            <div className="flex-1">
              <label className="text-xs text-gray-500">시작일시</label>
              <input
                required
                type="datetime-local"
                value={form.startAt}
                onChange={e => setForm(f => ({ ...f, startAt: e.target.value }))}
                className="w-full border rounded px-3 py-2 text-sm"
              />
            </div>
            <div className="flex-1">
              <label className="text-xs text-gray-500">종료일시</label>
              <input
                required
                type="datetime-local"
                value={form.endAt}
                onChange={e => setForm(f => ({ ...f, endAt: e.target.value }))}
                className="w-full border rounded px-3 py-2 text-sm"
              />
            </div>
          </div>
          <div className="flex gap-2">
            <button
              type="submit"
              disabled={creating}
              className="flex-1 bg-indigo-600 text-white py-2 rounded-lg text-sm disabled:bg-indigo-300"
            >
              {creating ? '생성 중...' : '생성'}
            </button>
            <button
              type="button"
              onClick={() => setShowForm(false)}
              className="py-2 px-4 border rounded-lg text-sm text-gray-500"
            >
              취소
            </button>
          </div>
        </form>
      )}

      {loading ? (
        <div className="text-center py-8 text-gray-400">로딩 중...</div>
      ) : (
        <div className="bg-white rounded-xl border overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50">
              <tr>
                <th className="text-left px-4 py-3 text-gray-500">ID</th>
                <th className="text-left px-4 py-3 text-gray-500">제목</th>
                <th className="text-left px-4 py-3 text-gray-500">상태</th>
              </tr>
            </thead>
            <tbody className="divide-y">
              {challenges.map(c => (
                <tr key={c.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3 text-gray-400">{c.id}</td>
                  <td className="px-4 py-3 font-medium">{c.title}</td>
                  <td className="px-4 py-3">
                    <span className={`text-xs px-2 py-0.5 rounded-full ${statusColor[c.status] ?? 'bg-gray-100'}`}>
                      {statusLabel[c.status] ?? c.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
