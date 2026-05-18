'use client';

import { useState, useEffect } from 'react';
import axios from 'axios';
import api from '@/lib/api';
import DevNote from '@/components/ui/DevNote';

const CHALLENGE_API = process.env.NEXT_PUBLIC_CHALLENGE_API_URL || 'http://localhost:8082';

interface Stats {
  totalUsers: number;
  totalExchanges: number;
}

export default function AdminDashboard() {
  const [stats, setStats] = useState<Stats | null>(null);
  const [activeChallenges, setActiveChallenges] = useState<number | null>(null);

  useEffect(() => {
    api.get('/api/admin/stats')
      .then(res => setStats(res.data.data ?? res.data))
      .catch(() => {});

    axios.get(`${CHALLENGE_API}/api/challenges`)
      .then(res => setActiveChallenges((res.data.data ?? []).length))
      .catch(() => setActiveChallenges(0));
  }, []);

  const cards = [
    { label: '전체 회원', value: stats ? stats.totalUsers.toLocaleString() : null, icon: '👥', color: 'bg-blue-50 border-blue-200' },
    { label: '진행 중 챌린지', value: activeChallenges !== null ? String(activeChallenges) : null, icon: '🏅', color: 'bg-green-50 border-green-200' },
    { label: '총 교환 건수', value: stats ? stats.totalExchanges.toLocaleString() : null, icon: '🎁', color: 'bg-purple-50 border-purple-200' },
  ];

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">📊 관리자 대시보드</h1>
      <div className="grid grid-cols-2 lg:grid-cols-3 gap-4">
        {cards.map((c) => (
          <div key={c.label} className={`rounded-xl border p-5 ${c.color}`}>
            <div className="text-2xl mb-1">{c.icon}</div>
            {c.value !== null ? (
              <div className="text-2xl font-bold text-gray-800">{c.value}</div>
            ) : (
              <div className="h-8 bg-gray-200 rounded animate-pulse mb-1" />
            )}
            <div className="text-sm text-gray-500">{c.label}</div>
          </div>
        ))}
      </div>

      <DevNote>
        <div className="font-semibold mb-1">아키텍처 레이어</div>
        <div>• <strong>rc-core-service (:8080)</strong> — 인증(OAuth2+JWT), 회원, 리워드 교환</div>
        <div>• <strong>rc-challenge-service (:8082)</strong> — 챌린지, 참여, 미션, Redis 랭킹</div>
        <div>• <strong>Kafka</strong> — mission.completed 이벤트 → 랭킹 갱신 + 알림</div>
        <div>• <strong>Redis</strong> — 분산 락(Redisson), Sorted Set 랭킹, 캐시</div>
      </DevNote>
    </div>
  );
}
