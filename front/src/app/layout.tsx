// # 전역 레이아웃
import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "../styles/globals.css";
import Header from "../components/Header";
import Footer from "../components/Footer";
import Script from "next/script";

// TanStack Query 관련 임포트 추가
import {
  QueryClient, // QueryClient 인스턴스를 생성하는 클래스
  QueryClientProvider, // React 컴포넌트 트리에 QueryClient를 제공하는 Provider
} from "@tanstack/react-query";
import { ReactQueryDevtools } from "@tanstack/react-query-devtools";

// eslint-disable-next-line @typescript-eslint/no-unused-vars
const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

// eslint-disable-next-line @typescript-eslint/no-unused-vars
const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "이웃 마켓",
  description: "이웃과 중고 거래를 통해 마음을 나눠보아요.",
};

// --- QueryClient 인스턴스 생성 ---
// 이 인스턴스는 애플리케이션 전역에서 단 한 번만 생성되어야 합니다.
// 클라이언트 컴포넌트에서 useQueryClient 훅을 통해 이 인스턴스에 접근할 수 있습니다.
const queryClient = new QueryClient({
  // 전역 쿼리 옵션을 설정하여 애플리케이션 전체의 데이터 패칭 동작을 정의합니다.
  defaultOptions: {
    queries: {
      // 데이터가 'stale'(오래된) 상태로 간주되기 전까지의 시간 (기본값: 0)
      // 이 시간 동안은 캐시된 데이터를 'fresh'한 상태로 간주하여 불필요한 네트워크 사용을 줄입니다.
      // 5분 (5 * 60 * 1000ms)으로 설정하여, 캐시된 데이터가 5분 내라면 즉시 사용하고 백그라운드 리페치를 하지 않습니다.
      staleTime: 5 * 60 * 1000,

      // 창이 다시 포커스 될 때 자동으로 데이터를 리페치할지 여부.
      // 대부분의 경우 `false`로 설정하고, 필요할 때만 수동으로 `invalidateQueries`를 사용하는 것이 좋습니다.
      refetchOnWindowFocus: false,

      // 쿼리 실패 시 자동으로 재시도할 횟수.
      // 일시적인 네트워크 문제에 강건하게 대응할 수 있도록 3회로 설정
      retry: 3,

      // 쿼리가 비활성화 상태가 된 후 캐시에서 제거되기 전까지의 시간 (기본값: 5분).
      // 이 시간 동안 캐시가 유지되므로, 사용자가 페이지를 나갔다가 돌아와도 데이터가 남아있을 수 있습니다.
      gcTime: 10 * 60 * 1000,
    },
    mutations: {
      // 뮤테이션(POST, PUT, DELETE 등)에 대한 기본 옵션을 설정할 수 있습니다.
      // 예를 들어, 뮤테이션 실패 시 재시도 횟수 등을 설정할 수 있습니다.
      retry: 0, // 뮤테이션은 일반적으로 재시도하지 않는 것이 일반적입니다. (멱등성 문제)
    }
  },
});

/**
 * @function RootLayout
 * @description Next.js 애플리케이션의 최상위 레이아웃 컴포넌트입니다.
 * 모든 페이지에 공통적으로 적용될 요소들을 정의하고, TanStack Query를 포함한
 * 전역 Provider들을 래핑하여 애플리케이션 전체에서 해당 기능들을 사용할 수 있게 합니다.
 * @param {object} props - React 자식 컴포넌트들을 포함하는 props
 * @param {React.ReactNode} props.children - 렌더링될 페이지 또는 하위 레이아웃 컴포넌트들
 * @returns {JSX.Element} 렌더링된 RootLayout 컴포넌트
 */
export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="ko">
      <body>
        {/* --- TanStack Query Provider로 애플리케이션을 감싸줍니다. --- */}
        {/* QueryClientProvider는 React Context를 사용하여 QueryClient 인스턴스를
            하위 컴포넌트들에게 제공합니다. */}
        <QueryClientProvider client={queryClient}>
          <Header />
          <main className="p-8 pb-20 sm:p-20 bg-yellow-100">{children}</main>
          <Footer />

          {/* Daum Postcode API 스크립트 (기존 코드 유지) */}
          <Script
            src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"
            strategy="lazyOnload" // 필요할 때만 로드하도록 설정 (페이지 성능에 도움)
            id="daum-postcode-script" // 스크립트 고유 ID (선택 사항이지만 권장)
          />

          {/* --- 개발 환경에서만 TanStack Query Devtools를 활성화합니다. --- */}
          {/* ReactQueryDevtools는 캐시 상태, 쿼리 진행 상황 등을 시각적으로 보여주는 도구입니다.
              개발 시 디버깅에 매우 유용하며, 프로덕션 빌드에는 포함되지 않도록 조건부 렌더링합니다. */}
          {process.env.NODE_ENV === 'development' && <ReactQueryDevtools initialIsOpen={false} />}
        </QueryClientProvider>
      </body>
    </html>
  );
}