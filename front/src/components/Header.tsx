"use client";

import React, { useState, useCallback } from "react";
import Image from "next/image";
import Link from "next/link";
import SearchBar from "./ui/SearchBar";
import Navbar from "./ui/Navbar";
import styles from "../styles/Header.module.css";
import LoginModal from "./ui/LoginModal";
import { useAuth } from "@/context/AuthContext";
import LoggedInUserInfoBox from "./ui/LoggedInUserInfoBox";

interface HeaderProps {
  className?: string;
}

export default function Header({ className }: HeaderProps) {
  const [isModalOpen, setIsModalOpen] = useState(false);  // 모달 상태 관리
  const [productSearchTerm, setProductSearchTerm] = useState(''); // 검색어 상태 추가

  // AuthContext에서 로그인 상태와 사용자 정보, 로그아웃 함수를 가져옴
  const { isLoggedIn } = useAuth();

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
      console.log('상품 검색 실행: ', productSearchTerm);
      // 실제 검색 로직을 구현해야 함
      console.log(`상품 "${productSearchTerm}"을(를) 검색합니다!`);
    } else {
      console.warn('검색어를 입력해 주세요.');
    }
  }, [productSearchTerm]); // productSearchTerm이 변경될 때만 함수 재생성

  return (
    <header className={`p-4 shadow-md ${className || ""}`.trim()}>
      {/* 로고와 타이틀 섹션 (항상 중앙 정렬) */}
      <div className="flex justify-center items-center mb-4">
        <div className={styles.logoContainer}>
          <Link href="/" className="flex items-center gap-2">
            <Image src="/images/main-logo.png" alt="로고사진" width={75} height={75} />
            <span className={`${styles.logoText} text-[#f60] text-2xl`}>이웃마켓</span>
          </Link>
        </div>
      </div>

      {/* 메인 콘텐츠 영역: 검색창 + 사용자/인증 버튼 그룹 (Grid 레이아웃) */}
      {/* ✨ grid-cols-[1fr_minmax(300px,_max-content)_1fr]로 가운데 열의 최소 너비를 지정합니다. */}
      {/* ✨ px-44를 grid 컨테이너에 직접 적용하여 전체적인 좌우 여백을 만듭니다. */}
      <div className="grid grid-cols-[1fr_minmax(750px,_max-content)_1fr] items-center mb-4 px-44">
        {/* 첫 번째 열 (왼쪽 유연한 여백) - 비워 둡니다. */}
        <div></div> 

        {/* 두 번째 열 (SearchBar) - 이 열 안에서 SearchBar를 중앙 정렬합니다. */}
        <div className="flex justify-center">
          <SearchBar
            value={productSearchTerm}
            onChange={handleSearchTermChange}
            onSearch={hadnleProductSearch}
          />
        </div>

        {/* ✨ 세 번째 열 (사용자/인증 버튼 그룹) - 최소 높이를 지정하고 내부에서 수직 중앙 정렬합니다. */}
        {/* LoggedInUserInfoBox의 높이에 따라 min-h-[XXpx] 값을 조정하세요. */}
        <div className="flex justify-end items-center max-h-[60px]">
          {isLoggedIn ? (
            // 로그인된 상태: LoggedInUserInfoBox 컴포넌트 렌더링
            <LoggedInUserInfoBox />
          ) : (
            // 로그아웃 상태: 로그인 / 회원 가입 버튼
            <div className="flex gap-2 text-xl">
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
          )}
        </div>
      </div>

      {/* 네비게이션 바 */}
      <Navbar />

      {/* 로그인 모달 */}
      <LoginModal isOpen={isModalOpen} closeModal={closeModal} />
    </header>
  );
}