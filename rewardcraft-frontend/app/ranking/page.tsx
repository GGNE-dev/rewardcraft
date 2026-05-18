'use client';

import { useState, useEffect, useCallback } from 'react';
import axios from 'axios';
import { RankingEntry } from '@/lib/types';
import RankingTable from '@/components/ranking/RankingTable';
import RankingChart from '@/components/ranking/RankingChart';
import DevNote from '@/components/ui/DevNote';

// 랭킹 API는 rc-challenge-service(8082)에 있음
const CHALLENGE_API = process.env.NEXT_PUBLIC_CHALLENGE_API_URL || 'http://localhost:8082';

const CHALLENGES = [
  { id: 1, title: '30일 운동' },
  { id: 2, title: '독서 인증' },
  { id: 3, title: '코딩 챌린지' },
];

export default function RankingPage() {
  const [selectedChallenge, setSelectedChallenge] = useState(1);
  const [rankings, setRankings] = useState<RankingEntry[]>([]);
  const [myRank, setMyRank] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [lastUpdated, setLastUpdated] = useState(new Date());

  const myUserId =
    typeof window !== 'undefined' ? Number(sessionStorage.getItem('userId')) : 0;
  const isLoggedIn =
    typeof window !== 'undefined' ? !!sessionStorage.getItem('accessToken') : false;

  const fetchRanking = useCallback(async () => {
    try {
      // 실제 응답: ApiResponse<List<RankingEntry>> → data.data가 배열
      const rankRes = await axios.get(
        `${CHALLENGE_API}/api/challenges/${selectedChallenge}/ranking/top?limit=20`
      );
      setRankings(rankRes.data.data ?? []);

      // 내 순위: userId를 query param으로 전달
      if (myUserId) {
        const myRankRes = await axios.get(
          `${CHALLENGE_API}/api/challenges/${selectedChallenge}/ranking/me?userId=${myUserId}`
        );
        setMyRank(myRankRes.data.data?.rank ?? null);
      }

      setLastUpdated(new Date());
    } catch {
      // 미로그인 또는 challenge-service 미기동 시 조용히 실패
    } finally {
      setLoading(false);
    }
  }, [selectedChallenge, myUserId]);

  useEffect(() => {
    setLoading(true);
    fetchRanking();
  }, [fetchRanking]);

  // 5초마다 자동 갱신 — Redis Sorted Set 실시간성 시연
  useEffect(() => {
    const interval = setInterval(fetchRanking, 5000);
    return () => clearInterval(interval);
  }, [fetchRanking]);

  return (
    <div className="space-y-6">
      {!isLoggedIn && (
        <div className="bg-indigo-50 border border-indigo-200 rounded-lg px-4 py-3 flex items-center justify-between">
          <span className="text-sm text-indigo-700">로그인하면 내 순위를 확인할 수 있어요.</span>
          <a href="/" className="text-sm font-medium text-indigo-600 underline">로그인하기</a>
        </div>
      )}
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-gray-800">🏆 챌린지 랭킹</h1>
        <div className="flex items-center gap-4">
          {myRank && (
            <span className="bg-indigo-100 text-indigo-700 px-3 py-1 rounded-full text-sm font-medium">
              내 순위: {myRank}위
            </span>
          )}
          <span className="text-xs text-gray-400">
            갱신: {lastUpdated.toLocaleTimeString()}
          </span>
          <span className="flex items-center gap-1 text-xs text-green-600 font-medium">
            <span className="w-2 h-2 bg-green-500 rounded-full animate-pulse" />
            LIVE
          </span>
        </div>
      </div>

      <div className="flex gap-2">
        {CHALLENGES.map((c) => (
          <button
            key={c.id}
            onClick={() => setSelectedChallenge(c.id)}
            className={`px-4 py-2 rounded-lg text-sm font-medium transition ${
              selectedChallenge === c.id
                ? 'bg-indigo-600 text-white'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
          >
            {c.title}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="text-center py-12 text-gray-400">
          <div className="text-3xl mb-2 animate-spin">⏳</div>
          랭킹 로딩 중...
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <RankingTable entries={rankings} myUserId={myUserId} />
          <RankingChart entries={rankings.slice(0, 10)} />
        </div>
      )}

      <DevNote>
        5초마다 자동 갱신 — <strong>Redis Sorted Set ZREVRANGEBYSCORE</strong> 기반.
        미션 완료 → Kafka <code>mission.completed</code> → PointsConsumer → ZADD
      </DevNote>
    </div>
  );
}
