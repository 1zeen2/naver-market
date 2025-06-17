"use client";

import { useState } from "react";
import Image from "next/image";
import Link from "next/link";
import SearchBar from "./ui/SearchBar";
import Navbar from "./ui/Navbar";
import styles from "../styles/Header.module.css";
import LoginModal from "./ui/LoginModal";

interface HeaderProps {
  className?: string;
}

export default function Header({ className }: HeaderProps) {
  const [isModalOpen, setIsModalOpen] = useState(false);  // 모달 상태 관리

  // 모달 열기, 닫기 함수
  const openModal = () => setIsModalOpen(true);
  const closeModal = () => setIsModalOpen(false);

  return (
    // 로고사진, 타이틀 출력
    <header className={`bg-blue-100 ${className || ""}`.trim()}>
      <div className={styles.logoContainer}>
        <Link href="/" className="flex items-center gap-2">
          <Image src="/images/main-logo.png" alt="로고사진" width={150} height={150} />
          <span className={styles.logoText}>이웃마켓</span>
        </Link>
      </div>

      <div className={styles.buttonContainer}>
        {/* 로그인 버튼 클릭 시 모달 열기 */}
        <button
          onClick={openModal}
          className={`${styles.button} ${styles.buttonLarge}`}
        >
          로그인
        </button>
        <Link href="/signup" className={`${styles.button} ${styles.buttonLarge}`}>
          회원 가입
        </Link>
      </div>

      <SearchBar />
      <Navbar />

      {/* 로그인 모달 */}
      <LoginModal isOpen={isModalOpen} closeModal={closeModal} />
    </header>
  );
}
