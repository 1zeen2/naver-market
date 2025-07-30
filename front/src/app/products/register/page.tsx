"use client";

import React, { useEffect } from 'react';
import { useSelector } from 'react-redux';
import { RootState } from '@/store';
import { useRouter } from 'next/navigation';
import ProductRegistrationForm from '../../../components/ProductRegistrationForm';

/**
 * @file page.tsx
 * @brief Next.js App Router를 위한 상품 등록 페이지입니다.
 * 사용자 인증 상태를 확인하고, 로그인된 경우 ProductRegistrationForm 컴포넌트를 렌더링합니다.
 */
export default function ProductRegisterPage() {
  const router = useRouter();

  // Redux 스토어에서 인증 상태와 로딩 상태를 가져옵니다.
  // authSlice의 isLoggedIn과 isLoading 상태를 가정합니다.
  const isLoggedIn = useSelector((state: RootState) => state.auth.isLoggedIn);
  const isLoading = useSelector((state: RootState) => state.auth.isLoading); // authSlice에 isLoading 상태가 있다고 가정

  useEffect(() => {
    // isLoading이 false가 되고, isLoggedIn 상태가 최종 확인된 후에만 처리
    if (!isLoading) {
      if (!isLoggedIn) {
        console.warn('로그인이 필요한 페이지입니다. 메인 페이지로 이동합니다.');
        router.push('/'); // 로그인 페이지로 리다이렉트
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
    <ProductRegistrationForm />
  );
}