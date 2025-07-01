"use client";

import React, { useState, useCallback } from "react";
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
  const [productSearchTerm, setProductSearchTerm] = useState(''); // 검색어 상태 추가

  // 모달 열기, 닫기 함수
  const openModal = () => setIsModalOpen(true);
  const closeModal = () => setIsModalOpen(false);

  // SearchBar의 onChange prop 전달 함수
  const handleSearchTermChange = useCallback((newValue: string) => {
    setProductSearchTerm(newValue);
  }, []);

  // SearchBar의 onSearch prop 전달 함수 (실제 검색 로직)
  const hadnleProductSearch = useCallback(() => {
    if (productSearchTerm.trim()) {
      console.log('상품 검색 실행:', productSearchTerm);
      // 실제 검색 로직을 구현해야 함
      alert(`상품 "${productSearchTerm}"을(를) 검색합니다!`); // 사용자 피드백 (임시)
    } else {
      alert('검색어를 입력해 주세요.');
    }
  }, [productSearchTerm]); // productSearchTerm이 변경될 때만 함수 재생성

  return (
    // 로고사진, 타이틀 출력
    <header className={`p-4 shadow-md ${className || ""}`.trim()}>
      <div className="flex justify-center items-center mb-4">
        <div className={styles.logoContainer}>
          <Link href="/" className="flex items-center gap-2">
            <Image src="/images/main-logo.png" alt="로고사진" width={75} height={75} />
            <span className={`${styles.logoText} text-[#f60] text-2xl`}>이웃마켓</span>
          </Link>
        </div>
      </div>

      <div className="flex justify-end mr-44 text-xl">
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

      <div className="mb-4">
        <SearchBar
          value={productSearchTerm} // Header의 상태를 SearchBar에 전달
          onChange={handleSearchTermChange} // Header의 핸들러를 SearchBar에 전달
          onSearch={hadnleProductSearch}  // Header의 검색 실행 핸들러를 SearchBar에 전달
        />
      </div>

      <Navbar />

      {/* 로그인 모달 */}
      <LoginModal isOpen={isModalOpen} closeModal={closeModal} />
    </header>
  );
}
