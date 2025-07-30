// src/components/providers/ClientProviders.tsx
'use client'; // 이 파일은 클라이언트 컴포넌트임을 명시

import React from 'react';
import dynamic from 'next/dynamic';
import ReactQueryProvider from "./ReactQueryProvider";
import ToastNotification from '@/components/common/ToastNotification';
import Script from "next/script";

// Header와 Footer 컴포넌트 임포트
import Header from "../Header"; // 올바른 상대 경로로 임포트했는지 확인해주세요.
import Footer from "../Footer"; // 올바른 상대 경로로 임포트했는지 확인해주세요.

// ReduxProvider를 next/dynamic을 사용하여 SSR 없이 클라이언트에서만 렌더링되도록 설정
const ReduxProvider = dynamic(() => import('@/components/providers/ReduxProvider'), {
  ssr: false, // 이제 클라이언트 컴포넌트 안이므로 ssr: false 사용 가능
  loading: () => null, // 로딩 중에 아무것도 보여주지 않음 (필요하다면 스피너 등)
});

export default function ClientProviders({ children }: { children: React.ReactNode }) {
  return (
    // ReduxProvider는 이 클라이언트 컴포넌트 내에서 동적으로 로드되며,
    // 그 안에 있는 모든 요소가 Redux 컨텍스트에 접근할 수 있습니다.
    <ReduxProvider>
      {/* ReactQueryProvider도 클라이언트 전용이므로 여기에 함께 래핑합니다. */}
      <ReactQueryProvider>
        {/* Header, main, Footer, 그리고 실제 페이지 콘텐츠(children)를 모두 이 안에 렌더링 */}
        <Header />
        <main className="p-8 pb-20 sm:p-20 flex-grow">
          {children} {/* 렌더링될 페이지 콘텐츠 */}
        </main>
        <Footer />
      </ReactQueryProvider>

      {/* 클라이언트 측에서만 필요한 컴포넌트들 (예: ToastNotification, 외부 스크립트) */}
      <ToastNotification />

      <Script
        src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"
        strategy="lazyOnload"
        id="daum-postcode-script"
      />
    </ReduxProvider>
  );
}