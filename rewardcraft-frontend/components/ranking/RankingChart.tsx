'use client';

import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts';
import { RankingEntry } from '@/lib/types';

interface Props {
  entries: RankingEntry[];
}

export default function RankingChart({ entries }: Props) {
  const chartData = entries.map((e) => ({
    name: e.nickname ? e.nickname.slice(0, 4) : `#${e.userId}`,
    포인트: e.score,
  }));

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
      <h2 className="font-semibold text-gray-700 mb-4">TOP 10 현황</h2>
      <ResponsiveContainer width="100%" height={300}>
        <BarChart data={chartData} layout="vertical">
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis type="number" />
          <YAxis type="category" dataKey="name" width={60} />
          <Tooltip formatter={(value) => [`${value}점`, '포인트']} />
          <Bar dataKey="포인트" fill="#6366f1" radius={[0, 4, 4, 0]} />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
