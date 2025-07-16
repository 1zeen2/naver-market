// components/ui/LoggedInUserInfoBox.tsx
// 이 파일은 사용자가 로그인했을 때 헤더에 표시되는 사용자 정보 박스 컴포넌트입니다.
// AuthContext에서 현재 로그인된 사용자 정보와 로그아웃 함수를 가져와 UI를 구성합니다.

"use client";

import React from "react";
import Link from "next/link";
import { useAuth } from "@/context/AuthContext";

/**
 * @function LoggedInUserInfoBox
 * @brief 로그인된 사용자의 정보를 표시하고, 마이페이지, 상품 등록 링크 및 로그아웃 버튼을 제공하는 컴포넌트입니다.
 * 이 컴포넌트는 `AuthContext`의 `user` 상태에 따라 동적으로 렌더링됩니다.
 *
 * @returns {JSX.Element | null} 렌더링된 사용자 정보 박스 또는 `user` 정보가 없을 경우 `null`
 */
export default function LoggedInUserInfoBox() {
  // `useAuth` 훅을 사용하여 AuthContext에서 현재 로그인된 사용자 정보(`user`)와 로그아웃 함수(`logout`)를 가져옵니다.
  const { user, logout } = useAuth();

  // -------------------------------------------------------------------------
  // 1. 조건부 렌더링 (Conditional Rendering)
  // -------------------------------------------------------------------------

  // `user` 객체가 `null`이거나 `undefined`일 경우 (예: 로딩 중, 로그아웃 상태, 토큰 만료 후 정보 없음),
  // 이 컴포넌트를 렌더링하지 않고 `null`을 반환하여 UI에 표시되지 않도록 합니다.
  // 이는 `Header.tsx`에서 `isLoggedIn` 상태에 따라 이 컴포넌트가 렌더링되는 방식과도 일치합니다.
  if (!user) {
    return null;
  }

  // -------------------------------------------------------------------------
  // 2. 컴포넌트 렌더링 (Component Rendering)
  // -------------------------------------------------------------------------

  return(
    // 박스 형태의 테두리 (연한 회색, 둥근 모서리)
    <div className="p-4 border border-gray-200 rounded-lg shadow-sm bg-gray-50 text-gray-700">
      {/* 사용자 프로필 영역 (프로필 이미지와 사용자 이름/이메일 정보를 포함 / 네이버 예시 참고) */}
      <div className="flex items-center mb-4">
        {/* 프로필 이미지 임시 플레이스 홀더 / 사용자의 이름 또는 아이디의 첫 글자를 표시하여 시각적 피드백 제공) */}
        <div className="w-12 h-12 bg-gray-300 rounded-full flex items-center justify-center text-gray-600 text-sm font-bold mr-3">
          {user.userName ? user.userName.charAt(0) : user.userId.charAt(0)}
        </div>
        {/* 사용자 정보 텍스트 영역 */}
        <div className="flex flex-col">
          {/* 사용자 닉네임 또는 아이디를 포함한 환영 메시지 */}
          <span className="text-lg font-bold whitespace-nowrap">
            {user.userName || user.userId}님 반갑습니다
          </span>
          {/* 해당 부분은 구현 예정 없음 */}
          {/* 이메일 정보 (주석 처리됨): 백엔드 `LoginResponseDto`에 `email` 필드가 추가되면 활성화 가능 */}
          {/* {user.email && <span className="text-sm text-gray-500">{user.email}</span>} */}
        </div>
      </div>

      {/* 주요 메뉴 링크들: 마이페이지 및 상품 등록 페이지로 이동하는 링크 제공. */}
      <div className="flex flex-col gap-2 mb-4">
        <Link href="/mypage" className="text-blue-600 hover:underline text-sm">
          마이페이지
        </Link>
        <Link href="/product/register" className="text-orange-600 hover:underline text-sm">
          상품 등록
        </Link>
        {/* 네이버 예시처럼 다른 서비스 링크를 추가할 수 있습니다. */}
        {/* <Link href="/messages" className="text-blue-600 hover:underline text-sm">쪽지</Link> */}
        {/* <Link href="/happybean" className="text-blue-600 hover:underline text-sm">해피빈</Link> */}
      </div>

      {/* 로그아웃 버튼: 클릭 시 `AuthContext`의 `logout` 함수를 호출하여 사용자를 로그아웃 처리합니다. */}
      <button
        onClick={logout} // `useAuth` 훅에서 가져온 `logout` 함수를 호출
        className="w-full py-2 px-4 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300 transition duration-200 text-sm font-medium"
      >
        로그아웃
      </button>
    </div>
  );
}