'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import axios from 'axios';
import toast from 'react-hot-toast';
import api from '@/lib/api';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
const CHALLENGE_API = process.env.NEXT_PUBLIC_CHALLENGE_API_URL || 'http://localhost:8082';

interface Challenge {
  id: number;
  title: string;
  status: string;
}

interface RankingEntry {
  rank: number;
  userId: number;
  score: number;
  nickname?: string;
}

export default function HomePage() {
  const [challenges, setChallenges] = useState<Challenge[]>([]);
  const [topRanking, setTopRanking] = useState<RankingEntry[]>([]);
  const [devLoggingIn, setDevLoggingIn] = useState<number | null>(null);
  const isLoggedIn =
    typeof window !== 'undefined' ? !!sessionStorage.getItem('accessToken') : false;

  useEffect(() => {
    axios.get(`${CHALLENGE_API}/api/challenges`)
      .then(res => setChallenges(res.data.data ?? []))
      .catch(() => {});

    axios.get(`${CHALLENGE_API}/api/challenges/1/ranking/top?limit=3`)
      .then(res => setTopRanking(res.data.data ?? []))
      .catch(() => {});
  }, []);

  const handleKakaoLogin = () => {
    window.location.href = `${API_URL}/oauth2/authorization/kakao`;
  };

  const handleGoogleLogin = () => {
    window.location.href = `${API_URL}/oauth2/authorization/google`;
  };

  // DevController: OAuth 없이 시딩된 계정으로 즉시 로그인 (local 프로파일 전용)
  const handleDevLogin = async (userId: number, label: string) => {
    setDevLoggingIn(userId);
    try {
      const tokenRes = await axios.post(`${API_URL}/api/dev/token?userId=${userId}`);
      const accessToken = tokenRes.data.data?.accessToken ?? tokenRes.data.accessToken;

      sessionStorage.setItem('accessToken', accessToken);

      const meRes = await api.get('/api/users/me');
      const user = meRes.data.data ?? meRes.data;
      sessionStorage.setItem('userRole', user.role);
      sessionStorage.setItem('userId', String(user.id));
      sessionStorage.setItem('nickname', user.nickname);

      toast.success(`${label}으로 로그인됨`);
      window.location.href = '/ranking';
    } catch {
      toast.error('개발 로그인 실패 (서버가 실행 중인지 확인하세요)');
    } finally {
      setDevLoggingIn(null);
    }
  };

  const RANK_EMOJI = ['🥇', '🥈', '🥉'];

  return (
    <div className="space-y-12">

      {/* 히어로 */}
      <section className="text-center py-12 space-y-4">
        <h1 className="text-4xl font-bold text-indigo-600">🏆 RewardCraft</h1>
        <p className="text-gray-500 text-lg">매일 챌린지를 완료하고 포인트로 리워드를 교환하세요</p>
        <div className="flex justify-center gap-4 pt-2">
          <Link href="/ranking" className="bg-indigo-600 text-white px-6 py-2 rounded-lg hover:bg-indigo-700 transition text-sm font-medium">
            랭킹 보기
          </Link>
          <Link href="/rewards" className="border border-indigo-600 text-indigo-600 px-6 py-2 rounded-lg hover:bg-indigo-50 transition text-sm font-medium">
            리워드 보기
          </Link>
        </div>
      </section>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">

        {/* 진행 중인 챌린지 */}
        <section className="bg-white rounded-xl border shadow-sm p-6 space-y-4">
          <h2 className="text-lg font-semibold text-gray-800">🏅 진행 중인 챌린지</h2>
          {challenges.length === 0 ? (
            <p className="text-sm text-gray-400">현재 진행 중인 챌린지가 없습니다.</p>
          ) : (
            <ul className="space-y-2">
              {challenges.map(c => (
                <li key={c.id} className="flex items-center justify-between px-3 py-2 bg-gray-50 rounded-lg">
                  <span className="text-sm font-medium text-gray-700">{c.title}</span>
                  <span className="text-xs bg-green-100 text-green-700 px-2 py-0.5 rounded-full">
                    {c.status === 'ACTIVE' ? '진행 중' : c.status}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </section>

        {/* 실시간 TOP 3 */}
        <section className="bg-white rounded-xl border shadow-sm p-6 space-y-4">
          <div className="flex justify-between items-center">
            <h2 className="text-lg font-semibold text-gray-800">📊 실시간 TOP 3</h2>
            <Link href="/ranking" className="text-xs text-indigo-500 hover:underline">전체 보기</Link>
          </div>
          {topRanking.length === 0 ? (
            <p className="text-sm text-gray-400">아직 순위 데이터가 없습니다.</p>
          ) : (
            <ul className="space-y-2">
              {topRanking.map(entry => (
                <li key={entry.userId} className="flex items-center gap-3 px-3 py-2 bg-gray-50 rounded-lg">
                  <span className="text-xl w-8">{RANK_EMOJI[entry.rank - 1]}</span>
                  <span className="flex-1 text-sm font-medium text-gray-700">
                    {entry.nickname ?? `User #${entry.userId}`}
                  </span>
                  <span className="text-sm font-bold text-indigo-600">{entry.score.toLocaleString()}점</span>
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>

      {/* 소셜 로그인 섹션 */}
      {!isLoggedIn && (
        <section className="bg-indigo-50 border border-indigo-100 rounded-xl p-8 text-center space-y-4">
          <p className="text-gray-600 font-medium">소셜 계정으로 시작하고 챌린지에 참여하세요</p>
          <div className="flex justify-center gap-4">
            <button
              onClick={handleKakaoLogin}
              className="bg-yellow-400 text-gray-900 font-bold py-2 px-6 rounded-lg hover:bg-yellow-500 transition"
            >
              🟡 카카오로 시작하기
            </button>
            <button
              onClick={handleGoogleLogin}
              className="bg-white border border-gray-300 text-gray-700 font-bold py-2 px-6 rounded-lg hover:bg-gray-50 transition"
            >
              🔵 구글로 시작하기
            </button>
          </div>
        </section>
      )}

      {/* 개발 테스트 퀵 로그인 — production 빌드에서는 렌더링 자체를 생략 */}
      {!isLoggedIn && process.env.NODE_ENV !== 'production' && (
        <section className="bg-gray-900 text-white rounded-xl p-6 space-y-3">
          <div className="flex items-center gap-2">
            <span className="text-xs bg-red-500 px-2 py-0.5 rounded font-bold">DEV</span>
            <h2 className="text-sm font-semibold text-gray-300">개발 퀵 로그인 (OAuth 없이 시딩 계정 사용)</h2>
          </div>
          <div className="grid grid-cols-3 gap-3">
            {[
              { userId: 1, label: '일반 유저', desc: 'USER 권한 · 320점 보유', color: 'bg-gray-700 hover:bg-gray-600' },
              { userId: 2, label: '운영자', desc: 'OPERATOR · 챌린지 관리', color: 'bg-blue-800 hover:bg-blue-700' },
              { userId: 3, label: '관리자', desc: 'ADMIN · 전체 접근', color: 'bg-red-900 hover:bg-red-800' },
            ].map(({ userId, label, desc, color }) => (
              <button
                key={userId}
                onClick={() => handleDevLogin(userId, label)}
                disabled={devLoggingIn !== null}
                className={`${color} rounded-lg p-3 text-left transition disabled:opacity-50`}
              >
                <div className="text-sm font-medium">{devLoggingIn === userId ? '로그인 중...' : label}</div>
                <div className="text-xs text-gray-400 mt-0.5">{desc}</div>
              </button>
            ))}
          </div>
          <p className="text-xs text-gray-500">
            RBAC 검증: 운영자 → 관리자 메뉴 없음 / 관리자 → 감사 로그 + 리워드 관리 접근 가능
          </p>
        </section>
      )}
    </div>
  );
}
