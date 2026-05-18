'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const [role, setRole] = useState<string | null>(null);
  const [checking, setChecking] = useState(true);

  useEffect(() => {
    const userRole = sessionStorage.getItem('userRole');
    const token = sessionStorage.getItem('accessToken');

    // 토큰 없거나 USER 권한이면 메인으로 리다이렉트 (RBAC 프론트 가드)
    if (!token || (userRole !== 'ADMIN' && userRole !== 'OPERATOR')) {
      router.push('/');
      return;
    }
    setRole(userRole);
    setChecking(false);
  }, [router]);

  if (checking) {
    return (
      <div className="flex justify-center py-12">
        <div className="animate-spin text-3xl">⏳</div>
      </div>
    );
  }

  return (
    <div className="flex gap-6">
      <aside className="w-48 flex-shrink-0">
        <div className="bg-gray-800 text-white rounded-xl p-4 space-y-1">
          <div className="text-xs text-gray-400 uppercase font-semibold px-2 py-1 mb-2">
            {role === 'ADMIN' ? '최고 관리자' : '운영자'}
          </div>
          <SidebarLink href="/admin" label="📊 대시보드" />
          <SidebarLink href="/admin/users" label="👥 회원 관리" />
          <SidebarLink href="/admin/challenges" label="🏅 챌린지 관리" />
          <SidebarLink href="/admin/missions" label="🎯 미션 승인" />
          {/* ADMIN만 리워드 관리 + 감사 로그 접근 가능 */}
          {role === 'ADMIN' && (
            <>
              <SidebarLink href="/admin/rewards" label="🎁 리워드 관리" />
              <SidebarLink href="/admin/audit" label="🔍 감사 로그" />
            </>
          )}
        </div>
      </aside>
      <main className="flex-1">{children}</main>
    </div>
  );
}

function SidebarLink({ href, label }: { href: string; label: string }) {
  return (
    <Link
      href={href}
      className="block px-3 py-2 rounded-lg text-sm text-gray-300 hover:bg-gray-700 hover:text-white transition"
    >
      {label}
    </Link>
  );
}
