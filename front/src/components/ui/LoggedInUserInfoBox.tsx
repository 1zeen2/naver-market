"use client";

import React from "react";
import Link from "next/link";
import Image from "next/image";
import { useSelector, useDispatch } from "react-redux";
import { RootState, AppDispatch } from "@/store";
import { logout } from "@/store/authSlice";
import { useRouter } from "next/navigation";

/**
 * @file src/components/ui/LoggedInUserInfoBox.tsx
 * @brief 로그인된 사용자의 정보를 표시하고, 마이페이지, 상품 등록 링크 및 로그아웃 버튼을 제공하는 컴포넌트입니다.
 * Redux 스토어의 `auth` 상태에서 현재 로그인된 사용자 정보를 가져와 UI를 구성합니다.
 *
 * @returns {JSX.Element | null} 렌더링된 사용자 정보 박스 또는 `user` 정보가 없을 경우 `null`
 */
export default function LoggedInUserInfoBox() {
  const dispatch: AppDispatch = useDispatch();
  const router = useRouter();

  const user = useSelector((state: RootState) => state.auth.user);

  // -------------------------------------------------------------------------
  // 1. 조건부 렌더링 (Conditional Rendering)
  // -------------------------------------------------------------------------

  // `user` 객체가 `null`이거나 `undefined`일 경우 (예: 로딩 중, 로그아웃 상태, 토큰 만료 후 정보 없음),
  // 이 컴포넌트를 렌더링하지 않고 `null`을 반환하여 UI에 표시되지 않도록 합니다.
  if (!user) {
    return null;
  }

  // 표시할 이름 결정: nickname이 있으면 nickname, 없으면 userName을 폴백으로
  // 최종적으로는 nickname만 남을 예정임을 고려
   const displayUserName = user.nickname || user.userName;

  // 프로필 이미지 URL 결정: 유효한 URL이 없으면 기본 이미지 사용
  const defaultProfileImage = '/images/default-profile.png'; // 이 경로에 기본 이미지 파일이 있어야 합니다.
  const profileImageSrc = user.profileImageUrl && user.profileImageUrl.trim() !== ""
    ? user.profileImageUrl
    : defaultProfileImage;

  // -------------------------------------------------------------------------
  // 2. 이벤트 핸들러 (Event Handlers)
  // -------------------------------------------------------------------------

  const handleLogout = () => {
    dispatch(logout());
    // 로그아웃 후 메인 페이지 또는 로그인 페이지로 리다이렉트
    router.push('/');
  };

  // -------------------------------------------------------------------------
  // 3. 컴포넌트 렌더링 (Component Rendering)
  // -------------------------------------------------------------------------

  return (
    <div className="p-4 border border-gray-200 rounded-lg shadow-sm bg-gray-50 text-gray-700 w-full max-w-sm"> {/* max-w-sm 추가하여 너비 제어 */}
      {/* 사용자 프로필 영역 (프로필 이미지와 사용자 이름/이메일 정보를 포함) */}
      <div className="flex items-center mb-4">
        {/* 프로필 이미지 표시 (Image 컴포넌트 사용) */}
        <div className="w-12 h-12 relative flex-shrink-0"> {/* relative 추가 */}
          <Image
            src={profileImageSrc}
            alt="프로필 이미지"
            fill // 부모 div에 꽉 채우기
            sizes="100%" // 이미지 크기 최적화를 위한 sizes 속성
            className="rounded-full object-cover"
          />
        </div>
        
        {/* 사용자 정보 텍스트 영역 */}
        <div className="flex flex-col ml-3 overflow-hidden"> {/* ml-3 추가, overflow-hidden 추가 */}
          {/* 사용자 닉네임 또는 아이디를 포함한 환영 메시지 */}
          <span className="text-lg font-bold whitespace-nowrap overflow-hidden text-ellipsis"> {/* 닉네임 길이에 따른 줄바꿈 방지 및 말줄임표 */}
            {displayUserName}님 반갑습니다
          </span>
          {/* 사용자 매너 온도 또는 기타 정보를 여기에 추가할 수 있습니다. */}
          {user.areaName && (
            <span className="text-sm text-gray-500 whitespace-nowrap overflow-hidden text-ellipsis">
              {user.areaName}
            </span>
          )}
          {user.reputationScore !== undefined && ( // reputationScore가 정의되어 있을 때만 표시
            <span className="text-sm text-gray-500">
              매너 온도: {user.reputationScore}°C {/* °C 같은 단위 추가 */}
            </span>
          )}
        </div>
      </div>

      {/* 주요 메뉴 링크들 */}
      <div className="flex flex-col gap-2 mb-4">
        <Link href="/mypage" className="text-blue-600 hover:underline text-sm font-medium">
          마이페이지
        </Link>
        <Link href="/products/register" className="text-orange-600 hover:underline text-sm font-medium">
          상품 등록
        </Link>
        {/* 쪽지/알림 바로가기 */}
        <Link href="/messages" className="text-blue-600 hover:underline text-sm font-medium flex items-center">
          쪽지
          {user.unreadMessageCount !== undefined && user.unreadMessageCount > 0 && (
            <span className="ml-2 bg-red-500 text-white text-xs font-semibold px-2 py-0.5 rounded-full">
              {user.unreadMessageCount}
            </span>
          )}
        </Link>
        {/* 간단한 판매/구매 요약 */}
        {user.sellingInProgressCount !== undefined && user.sellingInProgressCount > 0 && (
          <Link href="/my-sales?status=in-progress" className="text-blue-600 hover:underline text-sm font-medium">
            판매 중인 거래: {user.sellingInProgressCount}개
          </Link>
        )}
        {user.buyingInProgressCount !== undefined && user.buyingInProgressCount > 0 && (
          <Link href="/my-purchases?status=in-progress" className="text-blue-600 hover:underline text-sm font-medium">
            구매 진행 중: {user.buyingInProgressCount}개
          </Link>
        )}
      </div>

      <button
        onClick={handleLogout}
        className="w-full py-2 px-4 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300 transition duration-200 text-sm font-medium"
      >
        로그아웃
      </button>
    </div>
  );
}