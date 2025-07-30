import React from "react";

/**
 * @function LoginPage
 * @brief /login 경로에 접근했을 때 렌더링되는 페이지 컴포넌트입니다.
 * 이 페이지는 직접적인 로그인 UI를 제공하지 않고,
 * Next.js가 해당 경로에 대한 유효한 React 컴포넌트를 찾을 수 있도록 합니다.
 * 실제 로그인 모달은 Header와 같은 다른 전역 컴포넌트에서 관리될 수 있습니다.
 *
 * @returns {JSX.Element | null} 간단한 메시지 또는 null (실제 UI는 모달로 처리될 경우)
 */
export default function LoginPage() {
  return (
    <></>
  );
}