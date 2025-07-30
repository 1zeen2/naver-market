"use client"

import { useState, useEffect } from "react";
import { useDispatch } from "react-redux";
import { AppDispatch } from "@/store";
import { loginSuccess } from "@/store/authSlice";
import api from "@/api/api";
import axios from "axios";

interface ModalProps {
  isOpen: boolean;
  closeModal: () => void;
}

/**
 * @file src/app/ui/LoginModal.tsx
 * @brief 사용자 로그인 모달 컴포넌트입니다.
 * Redux를 통해 로그인 상태를 관리하고, 백엔드 API와 통신합니다.
 */
export default function LoginModal({ isOpen, closeModal }: ModalProps) {
  const dispatch: AppDispatch = useDispatch();

  const [keepLoggedIn, setKeepLoggedIn] = useState(false); // 로그인 상태 유지 (현재 Redux Persist가 이 역할을 부분적으로 수행)
  const [userId, setUserId] = useState("");
  const [userPwd, setUserPwd] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);


  useEffect(() => {
    // ESC 키로 모달 닫기
    const handleEscape = (e: KeyboardEvent) => {
      if (e.key === "Escape") {
        closeModal();
      }
    };

    document.addEventListener("keydown", handleEscape);
    return () => {
      document.removeEventListener("keydown", handleEscape);
    };
  }, [closeModal]);

  // 모달이 열릴 때마다 상태 초기화
  useEffect(() => {
    if (isOpen) {
      setUserId("");
      setUserPwd("");
      setError(null);
      setIsLoading(false);
    }
  }, [isOpen]);

  if (!isOpen) return null; // 모달이 열려야만 내용 보이도록 처리

  /**
   * @function handleLogin
   * @brief 로그인 버튼 클릭 또는 엔터 키로 로그인 처리
   * 백엔드 API를 호출하고, 성공 시 Redux 스토어에 로그인 정보를 저장합니다.
   * @param e React.FormEvent - 폼 이벤트 객체
   */
  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault(); // 폼 제출 시 페이지 리로딩 방지
    setIsLoading(true);
    setError(null);

    if (!userId || !userPwd) {
      setError("아이디와 비밀번호를 입력해주세요.");
      setIsLoading(false);
      return;
    }

    try {
      const response = await api.post('/api/auth/login', { userId, userPwd });

      const {
        token,
        memberId,
        userId: resUserId, // 컴포넌트의 userId 상태와 충돌 방지를 위해 별칭 사용
        userName,
        nickname,
        profileImageUrl,
        reputationScore,
        itemsSoldCount,
        itemsBoughtCount,
        areaName,
        isVerifiedUser,
        email,
      } = response.data; // <<-- 여기서 모든 필드를 직접 가져옵니다.
      
      // 백엔드 LoginResponseDto 구조: { token:, memberId:, userId:, userName: }
      if (token && memberId && resUserId && userName) {
        dispatch(loginSuccess({
          accessToken: token,
          user: { // user 객체는 response.data에서 가져온 필드들로 직접 구성됩니다.
            memberId: memberId,
            userId: resUserId,
            userName: userName,
            nickname: nickname || null, // 백엔드에서 null로 올 수 있거나 없을 경우 대비
            profileImageUrl: profileImageUrl || null,
            reputationScore: reputationScore || 0, // 숫자 타입에 대한 기본값
            itemsSoldCount: itemsSoldCount || 0,
            itemsBoughtCount: itemsBoughtCount || 0,
            areaName: areaName || null,
            isVerifiedUser: isVerifiedUser || false, // boolean 타입에 대한 기본값
            email: email || null,
          },
        }));
        closeModal();
      } else {
        // 필수 필드가 누락된 경우의 에러 메시지
        setError('로그인 응답 형식이 올바르지 않습니다. 필수 사용자 정보가 누락되었습니다.');
        console.error('로그인 응답 형식 오류 (필드 누락):', response.data);
      }
    } catch (err) {
      if (axios.isAxiosError(err)) {
        const backendErrorMessage = err.response?.data?.message || err.response?.data?.error || '로그인 중 오류가 발생했습니다. 아이디 또는 비밀번호를 확인해주세요.';
        setError(backendErrorMessage);
        console.error('로그인 API 오류 (LoginModal):', err.response?.data || err.message);
      } else {
        setError('알 수 없는 오류가 발생했습니다.');
        console.error('알 수 없는 오류 (LoginModal):', err);
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-gray-500 bg-opacity-50 flex justify-center items-center z-50">
      <div className="bg-white p-10 rounded-lg shadow-lg w-full max-w-xl">
        <h2 className="text-xl font-bold mb-6 text-center">로그인</h2>

        {error && (
          <div className="mb-4 text-center text-red-500 text-sm">
            {error}
          </div>
        )}

        <form onSubmit={handleLogin}>
          <div className="mb-4">
            <input
              type="text"
              placeholder="아이디 입력"
              className="w-full p-3 border rounded-md"
              value={userId}
              onChange={(e) => setUserId(e.target.value)}
              disabled={isLoading}
            />
          </div>

          <div className="mb-4">
            <input
              type="password"
              placeholder="비밀번호 입력"
              className="w-full p-3 border rounded-md"
              value={userPwd}
              onChange={(e) => setUserPwd(e.target.value)}
              disabled={isLoading}
            />
          </div>

          <div className="flex items-center mb-4">
            <input
              type="checkbox"
              checked={keepLoggedIn}
              onChange={() => setKeepLoggedIn(!keepLoggedIn)}
              className="mr-2"
              disabled={isLoading}
            />
            <span className="text-sm">로그인 상태 유지</span>
          </div>

          <button
            type="submit"
            className="w-full bg-blue-500 text-white p-3 rounded-md hover:bg-blue-600 disabled:bg-blue-300 disabled:cursor-not-allowed"
            disabled={isLoading}
          >
            {isLoading ? '로그인 중...' : '로그인'}
          </button>
        </form>

        <div className="mt-4 text-sm text-gray-600 space-x-4 text-center">
          <a href="#" className="hover:underline">아이디 찾기</a>
          <span>|</span>
          <a href="#" className="hover:underline">비밀번호 찾기</a>
        </div>

        <div className="mt-8 text-xs text-gray-500 flex justify-center space-x-4">
          <a href="#" className="hover:underline">이용 약관</a>
          <span>|</span>
          <a href="#" className="hover:underline">개인정보 처리 방침</a>
          <span>|</span>
          <a href="#" className="hover:underline">책임의 한계와 법적 고지</a>
          <span>|</span>
          <a href="#" className="hover:underline">회원 정보 고객 센터</a>
        </div>

        <button
          onClick={closeModal}
          className="mt-4 w-full text-gray-500 hover:text-gray-700 disabled:cursor-not-allowed"
          disabled={isLoading}
        >
          닫기
        </button>
      </div>
    </div>
  );
}