import { RankingEntry } from '@/lib/types';

const RANK_EMOJI = ['🥇', '🥈', '🥉'];

interface Props {
  entries: RankingEntry[];
  myUserId: number;
}

export default function RankingTable({ entries, myUserId }: Props) {
  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      <div className="px-6 py-4 border-b border-gray-100">
        <h2 className="font-semibold text-gray-700">TOP {entries.length}</h2>
      </div>
      <div className="divide-y divide-gray-50">
        {entries.map((entry) => {
          const isMe = entry.userId === myUserId;
          const rankEmoji = RANK_EMOJI[entry.rank - 1] ?? `${entry.rank}위`;

          return (
            <div
              key={entry.userId}
              className={`flex items-center px-6 py-3 transition ${
                isMe ? 'bg-indigo-50 border-l-4 border-indigo-500' : 'hover:bg-gray-50'
              }`}
            >
              <span className="w-10 text-lg">{rankEmoji}</span>
              <span className={`flex-1 font-medium ${isMe ? 'text-indigo-700' : 'text-gray-800'}`}>
                {entry.nickname ?? `User #${entry.userId}`}
                {isMe && (
                  <span className="ml-2 text-xs bg-indigo-500 text-white px-1 py-0.5 rounded">
                    나
                  </span>
                )}
              </span>
              <span className="font-bold text-gray-700">{entry.score.toLocaleString()}점</span>
            </div>
          );
        })}
        {entries.length === 0 && (
          <div className="text-center py-8 text-gray-400">아직 참여자가 없어요</div>
        )}
      </div>
    </div>
  );
}
