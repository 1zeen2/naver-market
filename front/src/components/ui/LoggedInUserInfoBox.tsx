"use client";

import React from "react";
import Link from "next/link";
import { useAuth } from "@/context/AuthContext";

export default function LoggedInUserInfoBox() {
  const { user, logout } = useAuth();

  // user 객체가 없을 경우 (로딩 중이거나 로그아웃 상태)를 대비한 방어 로직
  if (!user) {
    return null;
  }

  return(
    // ✨ 박스 형태의 테두리 (연한 회색, 둥근 모서리)
    <div className="p-4 border border-gray-200 rounded-lg shadow-sm bg-gray-50 text-gray-700">
      {/* 사용자 프로필 영역 (네이버 예시 참고) */}
      <div className="flex items-center mb-4">
        {/* 프로필 이미지 (임시 플레이스홀더) */}
        <div className="w-12 h-12 bg-gray-300 rounded-full flex items-center justify-center text-gray-600 text-sm font-bold mr-3">
          {user.userName ? user.userName.charAt(0) : user.userId.charAt(0)}
        </div>
        {/* 사용자 정보 */}
        <div className="flex flex-col">
          {/* 닉네임 */}
          <span className="text-lg font-bold whitespace-nowrap">
            {user.userName || user.userId}님 반갑습니다
          </span>
          {/* 이메일 (MemberEntity에 email 필드가 있다면 user 객체에도 추가되어야 합니다) */}
          {/* {user.email && <span className="text-sm text-gray-500">{user.email}</span>} */}
          {/* 현재 user 객체에 email이 없으므로 주석 처리. 백엔드에서 email을 LoginResponseDto에 포함시키면 사용 가능. */}
        </div>
      </div>

      {/* 주요 메뉴 링크들 */}
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

      {/* 로그아웃 버튼 */}
      <button
        onClick={logout}
        className="w-full py-2 px-4 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300 transition duration-200 text-sm font-medium"
      >
        로그아웃
      </button>
    </div>
  );
}