"use client";

import { useEffect } from "react";

interface FooterProps {
    className?: string;
}

export default function Footer({ className }: FooterProps) {
  useEffect(() => {

  }, []);

  return (
    <footer className={`bg-pink-100 text-center py-4 ${className || ""}`.trim()}>
      <p>React 19 + Next 15 Project - Made by 서원진</p>
    </footer>
  );
}
