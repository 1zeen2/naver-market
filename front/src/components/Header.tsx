"use client";

import React, { useState, useCallback, useEffect } from "react";
import Image from "next/image";
import Link from "next/link";
import SearchBar from "./ui/SearchBar";
import Navbar from "./ui/Navbar";
import styles from "../styles/Header.module.css";
import LoginModal from "./ui/LoginModal";
import LoggedInUserInfoBox from "./ui/LoggedInUserInfoBox";
import { useSearchParams, useRouter, usePathname } from "next/navigation";
import { useSelector } from "react-redux";
import { RootState } from "@/store";

interface HeaderProps {
  className?: string;
}

/**
 * @function Header
 * @brief 애플리케이션의 상단 헤더 영역을 렌더링하는 컴포넌트입니다.
 * 검색창, 네비게이션 바, 그리고 로그인 상태에 따른 사용자 인증/정보 UI를 포함합니다.
 * 미들웨어에 의해 리다이렉트될 경우 로그인 모달을 자동으로 띄우는 기능을 포함합니다.
 *
 * @param {HeaderProps} props - 컴포넌트에 전달될 속성 (className 등)
 * @returns {JSX.Element} 렌더링된 Header 컴포넌트
 */
export default function Header({ className }: HeaderProps) {
  // -------------------------------------------------------------------------
  // 1. 상태 관리 (State Management)
  // -------------------------------------------------------------------------
  /**
   * @state isModalOpen
   * @brief 로그인 모달의 열림/닫힘 상태를 관리합니다.
   */
  const [isModalOpen, setIsModalOpen] = useState(false);

  /**
   * @state productSearchTerm
   * @brief 상품 검색 입력 필드의 현재 검색어를 관리합니다.
   */
  const [productSearchTerm, setProductSearchTerm] = useState('');

  // -------------------------------------------------------------------------
  // 2. 훅 사용 (Hook Usage)
  // -------------------------------------------------------------------------
  const isLoggedIn = useSelector((state: RootState) => state.auth.isLoggedIn);

  // URL 쿼리 파라미터를 가져오기 위한 `useSearchParams` 훅 사용
  const searchParams = useSearchParams();
  // Next.js 라우터 인스턴스 (URL 변경에 사용)
  const router = useRouter();
  // 현재 URL의 pathname을 가져오기 위한 `usePathname` 훅 사용
  const pathname = usePathname();

  // -------------------------------------------------------------------------
  // 3. 이펙트 훅 (Effect Hook)
  // -------------------------------------------------------------------------

  /**
   * @hook useEffect
   * @brief 컴포넌트 마운트 시 또는 `searchParams`, `isLoggedIn` 상태 변경 시 실행됩니다.
   * 미들웨어에 의해 `loginRequired=true` 쿼리 파라미터와 함께 리다이렉트될 경우,
   * 로그인 모달을 자동으로 열고 URL에서 쿼리 파라미터를 제거합니다.
   */
  useEffect(() => {
    // URL에서 'loginRequired' 쿼리 파라미터 값을 가져옵니다.
    const loginRequired = searchParams.get('loginRequired');

    // 'loginRequired' 파라미터가 'true'이고, 현재 사용자가 로그인되어 있지 않다면
    if (loginRequired === 'true' && !isLoggedIn) {
      setIsModalOpen(true); // 로그인 모달을 엶

      // 모달을 연 후, URL에서 `loginRequired` 쿼리 파라미터를 제거합니다.
      // 이는 사용자가 페이지를 새로고침할 때마다 모달이 다시 열리는 것을 방지합니다.
      // 현재 URL의 pathname을 가져와 쿼리 파라미터 없이 URL을 대체합니다.
      router.replace(pathname);
    }
  }, [searchParams, isLoggedIn, router, pathname]) // 'searchParams', 'isLoggedIn', 'router', 'pathname'가 변경될 때 마다 이펙트 재실행

  // -------------------------------------------------------------------------
  // 4. 이벤트 핸들러 (Event Handlers)
  // -------------------------------------------------------------------------

  /**
   * @function openModal
   * @brief 로그인 모달을 여는 함수입니다.
   */
  const openLoginModal = () => setIsModalOpen(true);

  /**
   * @function closeModal
   * @brief 로그인 모달을 닫는 함수입니다.
   */
  const closeModal = () => setIsModalOpen(false);

  /**
   * @function handleSearchTermChange
   * @brief 검색어 입력 필드의 값이 변경될 때 호출되는 콜백 함수입니다.
   * @param {string} newValue - 새로 변경된 검색어 문자열
   */
  const handleSearchTermChange = useCallback((newValue: string) => {
    setProductSearchTerm(newValue);
  }, []);
  
  /**
   * @function hadnleProductSearch
   * @brief 상품 검색 버튼 클릭 또는 엔터 시 호출되는 콜백 함수입니다.
   * 실제 상품 검색 로직을 여기에 구현합니다.
   */
  const hadnleProductSearch = useCallback(() => {
    if (productSearchTerm.trim()) {
      console.log('상품 검색 실행: ', productSearchTerm);
      // 실제 검색 로직을 구현해야 함
      console.log(`상품 "${productSearchTerm}"을(를) 검색합니다!`);
    } else {
      console.warn('검색어를 입력해 주세요.');
    }
  }, [productSearchTerm]); // productSearchTerm이 변경될 때만 함수 재생성

  // -------------------------------------------------------------------------
  // 5. 컴포넌트 렌더링 (Component Rendering)
  // -------------------------------------------------------------------------
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
      {/* grid-cols-[1fr_minmax(300px,_max-content)_1fr]로 가운데 열의 최소 너비를 지정합니다. */}
      {/* px-44를 grid 컨테이너에 직접 적용하여 전체적인 좌우 여백을 만듭니다. */}
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

        {/* 세 번째 열 (사용자/인증 버튼 그룹) - 최소 높이를 지정하고 내부에서 수직 중앙 정렬합니다. */}
        {/* LoggedInUserInfoBox의 높이에 따라 min-h-[XXpx] 값을 조정하세요. */}
        <div className="flex justify-end items-center max-h-[60px]">
          {isLoggedIn ? (
            // 로그인된 상태: LoggedInUserInfoBox 컴포넌트 렌더링
            <LoggedInUserInfoBox />
          ) : (
            // 로그아웃 상태: 로그인 / 회원 가입 버튼
            <div className="flex gap-2 text-xl">
              <button
                className={styles.loginButton}
                onClick={openLoginModal} // openModal -> openLoginModal 일관성 유지
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