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

  const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:80/api';

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
      // 실제 API 호출 예시:
      //  const response = await fetch('/api/auth/me'), {headeers: { Authorization: `Bearer ${token}` }})
      //    if (!response.ok) {localStroage.removeItem('authToken'); 토큰 무효화 시 제거
      //      setTokem(null) return null
      //    }
      // const userDate = await response.json(); return userData;

      // 현재는 더미 사용자 정보 반환
      // 실제 JWT 토큰은 헤더와 페이로드가 base64로 인코딩 되어 .으로 구분됨
      // 더미 토큰이 dummy_jwt_token_for_userId 형태로 가정하였기 때문에 해당 파싱 로직은 실패할 수 있음
      // 백엔드에서 실제 JWT를 반환하도록 구현하면 이 부분도 실제 JWT 파싱 로직으로 변경해야 함

      try {
        // 더미 토큰이 'dummy_jwt_token_for_userId' 형태이므로, userId를 직접 추출합니다.
        // 실제 JWT라면 'token.split('.')[1] 후 'atob 디코딩이 필요합니다.
        // 현재 더미 토큰은 JWT 형식이 아니므로, 간단한 파싱으로 userId를 가져옵니다.

        const userIdFromToken = token?.replace('dummy_jwt_token_for_', '');
        // memberId와 userName은 실제 백엔드 응답에서 가져와야 하지만 현재는 더미 토큰에서 userId만 추출해서 사용
        // 실제 JWT를 사용하게 되면 JWT 페이로드에서 memberId와 userName을 디코딩하여 사용해야 합니다.
        return {
          memberId: 1, // 더미 값, 실제로는 JWT 페이로드에서 추출
          userId: userIdFromToken,
          userName: userIdFromToken,
        };
      } catch (e) {
        console.error("유효하지 않은 토큰 형식 파싱 에러: ", e);
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
      const response = await fetch(`${API_BASE_URL}/auth/login`, { 
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