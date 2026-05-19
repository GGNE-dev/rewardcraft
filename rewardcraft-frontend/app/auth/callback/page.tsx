'use client';

import { Suspense, useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import Cookies from 'js-cookie';
import api from '@/lib/api';

function AuthCallbackContent() {
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

    api.get('/api/users/me')
      .then(({ data }) => {
        const user = data.data;
        sessionStorage.setItem('userRole', user.role);
        sessionStorage.setItem('userId', String(user.id));
        sessionStorage.setItem('nickname', user.nickname);
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

export default function AuthCallbackPage() {
  return (
    <Suspense fallback={
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="animate-spin text-4xl">⏳</div>
      </div>
    }>
      <AuthCallbackContent />
    </Suspense>
  );
}
