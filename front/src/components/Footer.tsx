"use client";

import { useEffect } from "react";

interface FooterProps {
    className?: string;
}

export default function Footer({ className }: FooterProps) {
  useEffect(() => {

  }, []);

  return (
    <footer className={`bg-orange-100 text-center py-4 ${className || ""}`.trim()}>
      <p>React 19 + Next 15 Project - Made by 서원진</p>
      <p>본 페이지는 개인 포트폴리오 제작 용으로 상업적인 목적은 일절 없음을 명확히 밝힙니다.</p>
    </footer>
  );
}
