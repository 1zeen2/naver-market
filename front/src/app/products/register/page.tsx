"use client";

import React, { useEffect } from 'react';
import { useAuth } from '@/context/AuthContext';
import { useRouter } from 'next/navigation';

export default function ProductRegisterPage() {
  const { isLoggedIn, isLoading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    // isLoading이 false가 되고, isLoggedIn 상태가 최종 확인된 후에만 처리
    if (!isLoading) {
      if (!isLoggedIn) {
        console.warn('로그인이 필요한 페이지입니다. 로그인 페이지로 이동합니다.');
        router.push('/login'); // 로그인 페이지로 리다이렉트
      }
    }
  }, [isLoggedIn, isLoading, router]);

  // 인증 상태를 확인 중이거나, 로그인이 되지 않아 리다이렉트 대기 중일 때
  // 실제 페이지 콘텐츠를 렌더링하지 않고 로딩 메시지를 표시하거나 null을 반환합니다.
  if (isLoading || !isLoggedIn) {
    // 로그인이 안 되어 리다이렉트가 예정된 경우에도 더 이상 페이지를 렌더링할 필요 없으므로 null 반환
    // 또는 로딩 스피너 등을 표시할 수 있습니다.
    return (
      <main className="flex flex-col items-center justify-center min-h-[calc(100vh-120px)] p-4">
        <p className="text-lg text-gray-700">인증 상태 확인 중...</p>
      </main>
    );
  }

  // 로그인된 사용자에게만 보이는 콘텐츠
  return (
    <main className="container mx-auto py-8 px-4">
      <h2 className="text-3xl font-bold mb-6 text-center">상품 등록</h2>
      <div className="bg-white p-8 rounded-lg shadow-md max-w-2xl mx-auto">
        <p className="text-gray-700">여기에 상품 등록 폼이 들어갑니다.</p>
        <p className="text-sm text-gray-500 mt-4">이 페이지는 로그인된 사용자만 접근할 수 있습니다.</p>
        {/* 실제 상품 등록 폼 구현 */}
      </div>
    </main>
  );
}