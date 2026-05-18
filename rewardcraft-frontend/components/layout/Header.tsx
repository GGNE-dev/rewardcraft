'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import Cookies from 'js-cookie';

export default function Header() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [isAdmin, setIsAdmin] = useState(false);
  const pathname = usePathname();

  // pathname이 바뀔 때마다 재확인 — 로그인 후 페이지 이동 시 헤더 상태 동기화
  useEffect(() => {
    const token = sessionStorage.getItem('accessToken');
    const role = sessionStorage.getItem('userRole');
    setIsLoggedIn(!!token);
    setIsAdmin(role === 'ADMIN' || role === 'OPERATOR');
  }, [pathname]);

  const logout = () => {
    sessionStorage.clear();
    Cookies.remove('refreshToken');
    setIsLoggedIn(false);
    window.location.href = '/';
  };

  return (
    <header className="bg-indigo-600 text-white shadow-md">
      <div className="max-w-6xl mx-auto px-4 py-4 flex justify-between items-center">
        <Link href="/" className="text-xl font-bold">🏆 RewardCraft</Link>
        <nav className="flex gap-6 items-center">
          <Link href="/challenges" className="hover:text-indigo-200">챌린지</Link>
          <Link href="/ranking" className="hover:text-indigo-200">랭킹</Link>
          <Link href="/rewards" className="hover:text-indigo-200">리워드</Link>
          {isAdmin && (
            <Link href="/admin" className="hover:text-indigo-200">관리자</Link>
          )}
          {isLoggedIn && (
            <Link href="/profile" className="hover:text-indigo-200">내 프로필</Link>
          )}
          {isLoggedIn ? (
            <button
              onClick={logout}
              className="bg-white text-indigo-600 px-4 py-1 rounded font-medium"
            >
              로그아웃
            </button>
          ) : (
            <Link href="/" className="bg-white text-indigo-600 px-4 py-1 rounded font-medium">
              로그인
            </Link>
          )}
        </nav>
      </div>
    </header>
  );
}
