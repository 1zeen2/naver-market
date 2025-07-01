// # 전역 레이아웃
import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "../styles/globals.css";
import Header from "../components/Header";
import Footer from "../components/Footer";
import Script from "next/script";
import ReactQueryProvider from "@/components/providers/ReactQueryProvider";

// eslint-disable-next-line @typescript-eslint/no-unused-vars
const geistSans = Geist({ variable: "--font-geist-sans", subsets: ["latin"] });
// eslint-disable-next-line @typescript-eslint/no-unused-vars
const geistMono = Geist_Mono({ variable: "--font-geist-mono", subsets: ["latin"] });

export const metadata: Metadata = {
  title: "이웃 마켓",
  description: "이웃과 중고 거래를 통해 마음을 나눠보아요.",
};


/**
 * @function RootLayout
 * @description Next.js 애플리케이션의 최상위 레이아웃 컴포넌트입니다.
 * 모든 페이지에 공통적으로 적용될 UI 요소(헤더, 푸터 등)를 정의하고,
 * 전역적인 서비스 제공자(Provider)들을 래핑하여 하위 컴포넌트들이 사용할 수 있도록 합니다.
 * TanStack Query 기능은 ReactQueryProvider를 통해 제공됩니다.
 *
 * @param {object} props - React 자식 컴포넌트들을 포함하는 props
 * @param {React.ReactNode} props.children - 렌더링될 페이지 또는 하위 레이아웃 컴포넌트들
 * @returns {JSX.Element} 렌더링된 RootLayout 컴포넌트
 */
export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="ko">
      <body>
        {/* ReactQueryProvider로 애플리케이션의 핵심 내용을 감싸서,
            하위 컴포넌트들이 TanStack Query를 사용할 수 있게 합니다.
            QueryClient 인스턴스와 Devtools는 ReactQueryProvider 내부에서 관리됩니다. */}
        <ReactQueryProvider>
          <Header />
          <main className="p-8 pb-20 sm:p-20 bg-yellow-100">{children}</main>
          <Footer />
        </ReactQueryProvider>

        {/* Daum Postcode API 스크립트 (클라이언트 컴포넌트 외부에서 전역적으로 로드) */}
        <Script
          src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"
          strategy="lazyOnload"
          id="daum-postcode-script"
        />
      </body>
    </html>
  );
}