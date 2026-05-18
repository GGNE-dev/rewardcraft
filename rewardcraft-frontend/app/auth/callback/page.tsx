'use client';

import { useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import Cookies from 'js-cookie';
import api from '@/lib/api';

export default function AuthCallbackPage() {
  const router = useRouter();
  const searchParams = useSearchParams();

  useEffect(() => {
    const accessToken = searchParams.get('access_token');
    const refreshToken = searchParams.get('refresh_token');

    if (!accessToken || !refreshToken) {
      router.push('/');
      return;
    }

    sessionStorage.setItem('accessToken', accessToken);
    Cookies.set('refreshToken', refreshToken, {
      expires: 14,
      secure: process.env.NODE_ENV === 'production',
      sameSite: 'strict',
    });

    // 토큰 저장 후 사용자 정보 조회 → role/userId/nickname을 sessionStorage에 저장
    api.get('/api/users/me')
      .then(({ data }) => {
        sessionStorage.setItem('userRole', data.role);
        sessionStorage.setItem('userId', String(data.id));
        sessionStorage.setItem('nickname', data.nickname);
        router.push('/ranking');
      })
      .catch(() => router.push('/'));
  }, [router, searchParams]);

  return (
    <div className="flex items-center justify-center min-h-[60vh]">
      <div className="text-center">
        <div className="animate-spin text-4xl mb-4">⏳</div>
        <p className="text-gray-600">로그인 처리 중...</p>
      </div>
    </div>
  );
}
