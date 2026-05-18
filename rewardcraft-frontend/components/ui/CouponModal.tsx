'use client';

import { useEffect } from 'react';

interface Props {
  exchangeId: number;
  rewardName: string;
  exchangedAt: string;
  onClose: () => void;
}

// exchangeId 기반으로 항상 동일한 바코드 패턴 생성 (LCG pseudo-random)
function buildBars(seed: number): number[] {
  const bars: number[] = [];
  let n = seed;
  for (let i = 0; i < 50; i++) {
    n = Math.imul(n, 1664525) + 1013904223;
    bars.push((Math.abs(n) % 3) + 1); // 1~3px 너비
  }
  return bars;
}

// 16자리 쿠폰 코드 (exchangeId 시드, 항상 동일)
function buildCode(seed: number): string {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
  let n = seed;
  let code = '';
  for (let i = 0; i < 16; i++) {
    n = Math.imul(n, 1664525) + 1013904223;
    code += chars[Math.abs(n) % chars.length];
    if (i === 3 || i === 7 || i === 11) code += '-';
  }
  return code;
}

export default function CouponModal({ exchangeId, rewardName, exchangedAt, onClose }: Props) {
  const bars = buildBars(exchangeId);
  const couponCode = buildCode(exchangeId);

  const exchangeDate = new Date(exchangedAt);
  const expiryDate = new Date(exchangeDate);
  expiryDate.setDate(expiryDate.getDate() + 90); // 90일 유효

  // ESC 키로 닫기
  useEffect(() => {
    const handler = (e: KeyboardEvent) => { if (e.key === 'Escape') onClose(); };
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, [onClose]);

  // 바코드 SVG 총 너비 계산
  const totalWidth = bars.reduce((s, w) => s + w, 0) + bars.length; // 막대 + 간격

  return (
    <div
      className="fixed inset-0 bg-black/60 flex items-center justify-center z-50 p-4"
      onClick={onClose}
    >
      <div
        className="bg-white rounded-2xl w-full max-w-sm overflow-hidden shadow-2xl"
        onClick={e => e.stopPropagation()}
      >
        {/* 쿠폰 헤더 */}
        <div className="bg-indigo-600 px-6 pt-6 pb-8 text-center text-white relative">
          <p className="text-xs font-semibold tracking-widest opacity-70 mb-1">REWARDCRAFT COUPON</p>
          <h2 className="text-xl font-bold">{rewardName}</h2>
          {/* 톱니 효과 */}
          <div className="absolute bottom-0 left-0 right-0 flex">
            {Array.from({ length: 18 }).map((_, i) => (
              <div key={i} className="flex-1 h-4 bg-white rounded-tl-full rounded-tr-full" />
            ))}
          </div>
        </div>

        {/* 쿠폰 본문 */}
        <div className="px-6 pt-6 pb-4 space-y-5">
          {/* 바코드 SVG */}
          <div className="flex justify-center">
            <svg
              viewBox={`0 0 ${totalWidth} 60`}
              className="w-64 h-16"
              xmlns="http://www.w3.org/2000/svg"
            >
              {(() => {
                const rects = [];
                let x = 0;
                bars.forEach((w, i) => {
                  if (i % 2 === 0) {
                    rects.push(<rect key={i} x={x} y={0} width={w} height={60} fill="#1a1a1a" />);
                  }
                  x += w + 1;
                });
                return rects;
              })()}
            </svg>
          </div>

          {/* 쿠폰 코드 */}
          <div className="text-center">
            <p className="text-xs text-gray-400 mb-1">쿠폰 번호</p>
            <p className="font-mono text-lg font-bold tracking-widest text-gray-800">{couponCode}</p>
          </div>

          {/* 유효기한 */}
          <div className="flex justify-between text-xs text-gray-400 border-t pt-4">
            <span>발급일: {exchangeDate.toLocaleDateString('ko-KR')}</span>
            <span>유효기한: {expiryDate.toLocaleDateString('ko-KR')}</span>
          </div>
        </div>

        <div className="px-6 pb-6">
          <button
            onClick={onClose}
            className="w-full py-2.5 rounded-xl bg-indigo-600 text-white font-medium text-sm hover:bg-indigo-700 transition"
          >
            확인
          </button>
        </div>
      </div>
    </div>
  );
}
