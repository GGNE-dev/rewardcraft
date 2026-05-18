'use client';

import { useState, useEffect, useCallback } from 'react';
import toast from 'react-hot-toast';
import api from '@/lib/api';
import DevNote from '@/components/ui/DevNote';

interface AdminUser {
  id: number;
  email: string;
  nickname: string;
  role: string;
  provider: string;
  banned: boolean;
  createdAt: string;
}

export default function AdminUsersPage() {
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [search, setSearch] = useState('');
  const [roleFilter, setRoleFilter] = useState('');
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const myRole = typeof window !== 'undefined' ? sessionStorage.getItem('userRole') : '';

  const fetchUsers = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await api.get('/api/admin/users', {
        params: { email: search || undefined, role: roleFilter || undefined, page, size: 10 },
      });
      const paged = data.data ?? data;
      setUsers(paged.content ?? []);
      setTotalPages(paged.totalPages ?? 0);
    } catch {
      toast.error('회원 목록 조회 실패');
    } finally {
      setLoading(false);
    }
  }, [search, roleFilter, page]);

  useEffect(() => { fetchUsers(); }, [fetchUsers]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(0);
    fetchUsers();
  };

  const handleBanToggle = async (user: AdminUser) => {
    const action = user.banned ? '정지 해제' : '계정 정지';
    if (!window.confirm(`[${user.nickname}] ${action}하시겠습니까? (데이터는 보존됩니다)`)) return;
    try {
      const res = await api.patch(`/api/admin/users/${user.id}/ban?ban=${!user.banned}`);
      const updated: AdminUser = res.data.data;
      setUsers(prev => prev.map(u => u.id === user.id ? updated : u));
      toast.success(`${updated.nickname} 계정 ${updated.banned ? '정지' : '해제'} 완료`);
    } catch (err: unknown) {
      const status = (err as { response?: { status?: number } })?.response?.status;
      toast.error(status === 403 ? '권한이 없습니다.' : '처리 실패');
    }
  };

  const colCount = myRole === 'ADMIN' ? 7 : 6;

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold">👥 회원 관리</h1>

      <form onSubmit={handleSearch} className="flex gap-3">
        <input
          type="text" placeholder="이메일 검색" value={search}
          onChange={e => setSearch(e.target.value)}
          className="border rounded-lg px-3 py-2 text-sm flex-1"
        />
        <select value={roleFilter} onChange={e => setRoleFilter(e.target.value)}
          className="border rounded-lg px-3 py-2 text-sm">
          <option value="">전체 권한</option>
          <option value="USER">USER</option>
          <option value="OPERATOR">OPERATOR</option>
          <option value="ADMIN">ADMIN</option>
        </select>
        <button type="submit" className="bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm">
          검색
        </button>
      </form>

      <div className="bg-white rounded-xl border overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-gray-50">
            <tr>
              <th className="text-left px-4 py-3 text-gray-500">ID</th>
              <th className="text-left px-4 py-3 text-gray-500">닉네임</th>
              <th className="text-left px-4 py-3 text-gray-500">이메일</th>
              <th className="text-left px-4 py-3 text-gray-500">권한</th>
              <th className="text-left px-4 py-3 text-gray-500">상태</th>
              <th className="text-left px-4 py-3 text-gray-500">가입일</th>
              {myRole === 'ADMIN' && (
                <th className="text-left px-4 py-3 text-gray-500">관리</th>
              )}
            </tr>
          </thead>
          <tbody className="divide-y">
            {loading
              ? Array.from({ length: 5 }).map((_, i) => (
                  <tr key={i}>
                    {Array.from({ length: colCount }).map((_, j) => (
                      <td key={j} className="px-4 py-3">
                        <div className="h-4 bg-gray-100 rounded animate-pulse" />
                      </td>
                    ))}
                  </tr>
                ))
              : users.map(user => (
                  <tr key={user.id} className={`hover:bg-gray-50 ${user.banned ? 'opacity-50' : ''}`}>
                    <td className="px-4 py-3 text-gray-400">{user.id}</td>
                    <td className="px-4 py-3 font-medium">{user.nickname}</td>
                    <td className="px-4 py-3 text-gray-500">{user.email}</td>
                    <td className="px-4 py-3">
                      <span className={`text-xs px-2 py-0.5 rounded-full ${
                        user.role === 'ADMIN' ? 'bg-red-100 text-red-700'
                        : user.role === 'OPERATOR' ? 'bg-yellow-100 text-yellow-700'
                        : 'bg-gray-100 text-gray-600'
                      }`}>
                        {user.role}
                      </span>
                    </td>
                    <td className="px-4 py-3">
                      {user.banned
                        ? <span className="text-xs bg-red-100 text-red-600 px-2 py-0.5 rounded-full">정지됨</span>
                        : <span className="text-xs bg-green-100 text-green-600 px-2 py-0.5 rounded-full">정상</span>
                      }
                    </td>
                    <td className="px-4 py-3 text-gray-400 text-xs">
                      {user.createdAt ? new Date(user.createdAt).toLocaleDateString('ko-KR') : '-'}
                    </td>
                    {myRole === 'ADMIN' && (
                      <td className="px-4 py-3">
                        <button
                          onClick={() => handleBanToggle(user)}
                          className={`text-xs px-2 py-1 rounded ${
                            user.banned
                              ? 'bg-green-50 text-green-700 hover:bg-green-100'
                              : 'bg-red-50 text-red-700 hover:bg-red-100'
                          }`}
                        >
                          {user.banned ? '정지 해제' : '계정 정지'}
                        </button>
                      </td>
                    )}
                  </tr>
                ))}
          </tbody>
        </table>
      </div>

      <div className="flex justify-center gap-2">
        <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0}
          className="px-3 py-1 border rounded text-sm disabled:opacity-40">이전</button>
        <span className="px-3 py-1 text-sm text-gray-500">{page + 1} / {totalPages || 1}</span>
        <button onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))} disabled={page >= totalPages - 1}
          className="px-3 py-1 border rounded text-sm disabled:opacity-40">다음</button>
      </div>

      <DevNote>
        <strong>RBAC 시연:</strong> OPERATOR 로그인 → 관리 버튼 없음 / ADMIN → 계정 정지 버튼 + 감사 로그 메뉴
      </DevNote>
    </div>
  );
}
