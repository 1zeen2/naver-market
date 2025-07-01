'use client'

import React from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools'; // Devtools도 여기서 관리

// QueryClient 인스턴스는 Provider 컴포넌트 외부에서 한 번만 생성되도록 합니다.
// 이는 매 렌더링마다 새로운 인스턴스가 생성되는 것을 방지하기 위함입니다.
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000, // 5분
      refetchOnWindowFocus: false,
      retry: 3,
      gcTime: 10 * 60 * 1000, // 10분
    },
    mutations: {
      retry: 0,
    }
  },
});

/**
 * @function ReactQueryProvider
 * @description
 * TanStack Query의 QueryClientProvider를 래핑하는 클라이언트 컴포넌트입니다.
 * 이를 통해 QueryClient 인스턴스가 클라이언트 번들에서 생성되고,
 * 애플리케이션의 모든 클라이언트 컴포넌트가 TanStack Query의 기능을 사용할 수 있게 됩니다.
 * @param {object} props - React 자식 컴포넌트들을 포함하는 props
 * @param {React.ReactNode} props.children - 렌더링될 하위 컴포넌트들
 * @returns {JSX.Element} 렌더링된 QueryClientProvider와 Devtools
 */
export default function ReactQueryProvider({ children }: { children: React.ReactNode }) {
  return (
    <QueryClientProvider client={queryClient}>
      {children}
      {/* 개발 환경에서만 TanStack Query Devtools 활성화 */}
      {process.env.NODE_ENV === 'development' && <ReactQueryDevtools initialIsOpen={false} />}
    </QueryClientProvider>
  );
}