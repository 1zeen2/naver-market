"use client"

import React, { createContext, useContext, useState, useEffect, ReactNode } from "react";
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useRouter } from "next/navigation";

// 로그인 응답 데이터 타입 정의 (백엔드의 LoginResponseDto와 일치)
interface User {
  memberId: number;
  userId: string;
  userName: string;
}

interface AuthContextType {
  isLoggedIn: boolean;
  user: User | null | undefined;
  login: (userId: string, userPwd: string) => Promise<void>;
  logout: () => void;
  isLoading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(null);
  const queryClient = useQueryClient();
  const router = useRouter();

  // 초기 로드 시 localStorage에서 토큰 확인
  useEffect(() => {
    const storedToken = localStorage.getItem('authToken');
    if (storedToken) {
      setToken(storedToken);
    }
  }, []);

  // 사용자 정보를 가져오는 쿼리 (토큰이 있을 때만 실행)
  // 실제로는 토큰 유효성 검사 및 사용자 정보 페칭 API를 호출해야 함.
  // 여기서는 더미 사용자 정보를 반환하도록 구현
  const { data: user, isLoading: isUserLoading } = useQuery<User | null>({
    queryKey: ['currentUser', token],
    queryFn: async () => {
      if (!token) return null;

      // 실제 API 호출 예시
      try {
        const response = await fetch(`/api/auth/me`, {
          headers: { Authorization: `Bearer ${token}`}
        });

        if (!response.ok) {
          console.error("토큰 유효성 검사 실패 또는 사용자 정포 페칭 에러: ", response.status, response.statusText);
          localStorage.removeItem('authToken'); // 토큰 무효화 시 제거
          setToken(null);
          return null;
        }
        const userData = await response.json();
        return userData;

      } catch (e) {
        console.error("사용자 정보 페칭 중 에러 발생: ", e);
        localStorage.removeItem('authToken');
        setToken(null);
        return null;
      }
    },
    enabled: !!token, // 토큰이 있을 때만 쿼리 실행
    staleTime: 5 * 60 * 1000, // 5분 동안 fresh 상태 유지
    gcTime: 10 * 60 * 1000, // 10분 동안 캐시 유지
  });

  // 로그인 뮤테이션
  const loginMutation = useMutation({
    mutationFn: async ({ userId, userPwd }: { userId: string; userPwd: string }) => {
      const response = await fetch(`/api/auth/login`, { 
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userId, userPwd }),
      });

      if (!response.ok) {
        // 백엔드에서 에러 메시지를 JSON 형태로 반환한다고 가정
        const errorData = await response.json().catch(() => ({ message: '알 수 없는 로그인 오류' }));
        throw new Error(errorData.message || '로그인 실패');
      }
      return response.json();
    },
    onSuccess: (data: { token: string; memberId: number; userId: string; userName: string }) => {
      localStorage.setItem('authToken', data.token);
      setToken(data.token);
      // 로그인 성공 후 사용자 정보 캐시 업데이트 (선택 사항, useQuery가 자동 페칭할 것임)
      queryClient.setQueryData(['currentUser', data.token], {
        memberId: data.memberId,
        userId: data.userId,
        userName: data.userName,
      });
      router.push('/'); // 로그인 성공 후 메인 페이지로 이동
    },
    onError: (error: Error) => {
      alert(`로그인 오류: ${error.message}`);
    },
  });

  const login = async (userId: string, userPwd: string) => {
    await loginMutation.mutateAsync({ userId, userPwd });
  };

  const logout = () => {
    localStorage.removeItem('authToken');
    setToken(null);
    queryClient.removeQueries({ queryKey: ['currentUser'] }); // 사용자 정보 캐시 무효화
    router.push('/'); // 로그아웃 후 메인 페이지로 이동
  };

  const isLoggedIn = !!token && !!user; // 토큰과 사용자 정보 모두 있을 때 로그인 상태

  const isLoading = loginMutation.isPending || isUserLoading;

  const value = {
    isLoggedIn,
    user,
    login,
    logout,
    isLoading,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}