'use client';

import { useState, useEffect } from 'react';
import toast from 'react-hot-toast';
import api from '@/lib/api';

interface AuditLog {
  id: number;
  userId: number;
  action: string;
  targetType: string;
  targetId: string;
  createdAt: string;
}

export default function AdminAuditPage() {
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get('/api/admin/audit-logs')
      .then(res => setLogs(res.data.data ?? res.data ?? []))
      .catch(() => toast.error('감사 로그 조회 실패 (ADMIN 권한 필요)'))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold">🔍 감사 로그</h1>
      <p className="text-sm text-gray-500">관리자/운영자의 주요 액션이 자동 기록됩니다 (AOP @Audited).</p>

      {loading ? (
        <div className="text-center py-8 text-gray-400">로딩 중...</div>
      ) : (
        <div className="bg-white rounded-xl border overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50">
              <tr>
                <th className="text-left px-4 py-3 text-gray-500">ID</th>
                <th className="text-left px-4 py-3 text-gray-500">유저 ID</th>
                <th className="text-left px-4 py-3 text-gray-500">액션</th>
                <th className="text-left px-4 py-3 text-gray-500">대상 타입</th>
                <th className="text-left px-4 py-3 text-gray-500">대상 ID</th>
                <th className="text-left px-4 py-3 text-gray-500">일시</th>
              </tr>
            </thead>
            <tbody className="divide-y">
              {logs.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-4 py-8 text-center text-gray-400">
                    아직 감사 로그가 없습니다.
                  </td>
                </tr>
              ) : (
                logs.map(log => (
                  <tr key={log.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3 text-gray-400">{log.id}</td>
                    <td className="px-4 py-3 text-gray-500">{log.userId}</td>
                    <td className="px-4 py-3 font-medium text-indigo-700">{log.action}</td>
                    <td className="px-4 py-3 text-gray-500">{log.targetType}</td>
                    <td className="px-4 py-3 text-gray-500">{log.targetId}</td>
                    <td className="px-4 py-3 text-gray-400 text-xs">
                      {new Date(log.createdAt).toLocaleString('ko-KR')}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
