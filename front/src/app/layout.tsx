// # 전역 레이아웃
import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "../styles/globals.css";
import Header from "../components/Header";
import Footer from "../components/Footer";
import Script from "next/script";

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

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="ko">
      <body>
        <Header />
        <main className="p-8 pb-20 sm:p-20 bg-yellow-100">{children}</main>
        <Footer />
        {/* Daum Postcode API 스크립트를 body 태그 닫히기 직전에 추가합니다. */}
        <Script
          src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"
          strategy="lazyOnload" // 필요할 때만 로드하도록 설정 (페이지 성능에 도움)
          id="daum-postcode-script" // 스크립트 고유 ID (선택 사항이지만 권장)
        />
      </body>
    </html>
  );
}