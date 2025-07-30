// src/app/layout.tsx
import type { Metadata } from "next";
import { Geist } from "next/font/google";
import "../styles/globals.css";
// Header와 Footer는 이제 ClientProviders 내부에서 임포트/사용됩니다.
// import Header from "../components/Header";
// import Footer from "../components/Footer";

// 새로 만든 클라이언트 래퍼 컴포넌트 임포트
import ClientProviders from "@/components/providers/ClientProviders";

const geistSans = Geist({ variable: "--font-geist-sans", subsets: ["latin"] });

// 애플리케이션의 메타데이터 정의 (SEO 및 앱 정보)
export const metadata: Metadata = {
  title: "이웃 마켓",
  description: "이웃과 중고 거래를 통해 마음을 나눠보아요.",
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="ko" className={geistSans.variable}>
      <body className="flex flex-col min-h-screen">
        {/* 모든 클라이언트 측 프로바이더 및 Redux를 사용하는 UI는 ClientProviders 안에 있어야 합니다. */}
        <ClientProviders>
          {children} {/* 여기에 실제 페이지 콘텐츠가 들어옵니다. */}
        </ClientProviders>
      </body>
    </html>
  );
}