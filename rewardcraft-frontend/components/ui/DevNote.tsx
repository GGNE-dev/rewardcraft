/**
 * 개발 환경에서만 렌더링되는 설명 블록.
 * production 빌드(next build)에서는 null 반환 — 번들에도 포함되지 않음.
 */
export default function DevNote({ children }: { children: React.ReactNode }) {
  if (process.env.NODE_ENV === 'production') return null;
  return (
    <div className="bg-gray-900 text-gray-300 rounded-lg px-4 py-3 text-sm border border-gray-700 space-y-1">
      <span className="inline-block text-xs bg-gray-600 text-gray-200 px-1.5 py-0.5 rounded font-mono mr-2">DEV</span>
      {children}
    </div>
  );
}
