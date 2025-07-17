'use client';

import React, { useEffect } from "react";
import { useSelector, useDispatch } from "react-redux";
import { RootState, AppDispatch } from "@/store";
import { clearNotification } from "@/features/notification/notificationSlice";

/**
 * @file 전역 토스트 알림 컴포넌트
 * @brief Redux Store의 알림 상태를 구독하여 사용자에게 토스트 메시지를 표시합니다.
 * 메시지는 일정 시간 후 자동으로 사라집니다.
 */
const ToastNotification: React.FC = () => {
  const dispatch: AppDispatch = useDispatch();
  // Redux Store에서 notification 상태를 선택
  const { message, type, id } = useSelector((state: RootState) => state.notification);

  // 알림 메시지가 표시되면 일정 시간 후 자동으로 사라지도록 타이머 설정
  useEffect(() => {
    if (message) {
      const timer = setTimeout(() => {
        dispatch(clearNotification());
      }, 3000);

      // 컴포넌트 언마운트 또는 메시지/ID 변경 시 타이머 클린업
      return () => clearTimeout(timer);
    }
  }, [message, id, dispatch]); // message, id가 변경될 때 마다 useEffect 재실행

  // 메시지가 없으면 렌더링하지 않음
  if (!message) return null;

  // 알림 타입에 따라 배경색을 동적으로 설정
  const bgColor = type === 'success' ? 'bg-green-500' : type === 'error' ? 'bg-red-500' : 'bg-blue-500';

  return (
    // Tailwind CSS를 사용하여 토스트 알림의 스타일을 정의합니다.
    // fixed: 화면에 고정, z-50  다른 요소 위에 표시
    <div className={`fixed bottom-4 right-4 p-4 rounded-lg shadow-lg text-white ${bgColor} z-50`}>
      {message}
    </div>
  );
};

export default ToastNotification;