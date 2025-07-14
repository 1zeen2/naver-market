// context/AuthContext.tsx
// 이 파일은 애플리케이션 전반에 걸쳐 사용자 인증 상태(로그인 여부, 사용자 정보, 로그인/로그아웃 함수)를
// 제공하는 React Context를 정의합니다. JWT(JSON Web Token)를 사용하여 인증을 관리합니다.

"use client"

import React, { createContext, useContext, useState, useEffect, ReactNode } from "react";
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useRouter } from "next/navigation";
import { customFetch } from "@/utils/customFetch";

// -----------------------------------------------------------------------------
// 1. 타입 정의 (Interfaces)
// -----------------------------------------------------------------------------

/**
 * @interface User
 * @brief 로그인된 사용자의 정보를 나타내는 인터페이스입니다.
 * 백엔드의 LoginResponseDto 또는 /api/auth/me 응답과 일치해야 합니다.
 */
interface User {
  memberId: number;
  userId: string;
  userName: string;
}

/**
 * @interface AuthContextType
 * @brief AuthContext를 통해 하위 컴포넌트들에게 제공될 값들의 타입 정의입니다.
 * 이 타입을 통해 Context의 사용법과 제공되는 데이터/함수를 명확히 알 수 있습니다.
 */
interface AuthContextType {
  isLoggedIn: boolean;          // 사용자가 현재 로그인 상태인지 여부
  user: User | null | undefined; // 로그인된 사용자 정보 (로그인 전: null, 로딩 중: undefined)
  login: (userId: string, userPwd: string) => Promise<void>; // 로그인 함수
  logout: () => void;           // 로그아웃 함수
  isLoading: boolean;           // 인증 관련 작업(로그인, 사용자 정보 페칭)이 진행 중인지 여부
}

// -----------------------------------------------------------------------------
// 2. AuthContext 생성
// -----------------------------------------------------------------------------

/**
 * @const AuthContext
 * @brief React Context 객체를 생성합니다.
 * 이 Context를 통해 AuthProvider의 값들이 하위 컴포넌트에 전달됩니다.
 * 초기값은 `undefined`로 설정하여, `useAuth` 훅 사용 시 `AuthProvider` 내부에 있는지 확인합니다.
 */
const AuthContext = createContext<AuthContextType | undefined>(undefined);

// -----------------------------------------------------------------------------
// 3. AuthProvider 컴포넌트
// -----------------------------------------------------------------------------

/**
 * @function AuthProvider
 * @brief 애플리케이션의 인증 상태를 관리하고 하위 컴포넌트에 제공하는 프로바이더 컴포넌트입니다.
 * 이 컴포넌트는 React 트리의 상단에 위치하여 모든 자식 컴포넌트가 인증 정보에 접근할 수 있도록 합니다.
 *
 * @param {object} props - React 자식 컴포넌트들을 포함하는 props
 * @param {ReactNode} props.children - 렌더링될 하위 컴포넌트들
 * @returns {JSX.Element} 렌더링된 AuthContext.Provider
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  // -------------------------------------------------------------------------
  // 3.1. 상태 관리 (State Management)
  // -------------------------------------------------------------------------

  /**
   * @state token
   * @brief 현재 사용자의 JWT 토큰을 저장하는 상태입니다.
   * `null`은 토큰이 없음을 의미하며, `string`은 유효한 토큰 문자열을 의미합니다.
   */
  const [token, setToken] = useState<string | null>(null);

  /**
   * @const queryClient
   * @brief TanStack Query 클라이언트 인스턴스입니다.
   * 캐시 관리, 쿼리 무효화 등에 사용됩니다.
   */
  const queryClient = useQueryClient();

  /**
   * @const router
   * @brief Next.js 라우터 인스턴스입니다.
   * 페이지 이동(리다이렉션)에 사용됩니다.
   */
  const router = useRouter();

  // -------------------------------------------------------------------------
  // 3.2. 초기 로드 및 토큰 확인 (Initial Load & Token Check)
  // -------------------------------------------------------------------------

  /**
   * @hook useEffect
   * @brief 컴포넌트가 마운트될 때 한 번만 실행되어 localStorage에서 JWT 토큰을 확인합니다.
   * 새로고침 시에도 로그인 상태를 유지하기 위함입니다.
   */
  useEffect(() => {
    // 클라이언트 환경에서만 localStorage에 접근하도록 `typeof window !== 'undefined'` 확인
    if (typeof window !== 'undefined') {
      const storedToken = localStorage.getItem('authToken');
      if (storedToken) {
        setToken(storedToken); // 토큰이 있으면 상태에 설정
      }
    }
  }, []); // 빈 의존성 배열은 이 훅이 컴포넌트 마운트 시 한 번만 실행되도록 합니다.

  // -------------------------------------------------------------------------
  // 3.3. 사용자 정보 페칭 (User Information Fetching with TanStack Query)
  // -------------------------------------------------------------------------

  /**
   * @hook useQuery
   * @brief JWT 토큰을 사용하여 백엔드로부터 현재 로그인된 사용자 정보를 가져옵니다.
   * `token` 상태가 변경될 때마다 이 쿼리가 다시 실행되어 토큰의 유효성을 검사하고 최신 사용자 정보를 반영합니다.
   */
  const { data: user, isLoading: isUserLoading } = useQuery<User | null>({
    queryKey: ['currentUser', token], // 쿼리 키: 'currentUser'와 현재 'token'을 포함하여 토큰 변경 시 재실행
    queryFn: async () => {
      if (!token) {
        // 토큰이 없으면 사용자 정보를 가져올 필요가 없으므로 즉시 null 반환
        return null;
      }

      try {
        // `customFetch` 함수를 사용하여 `/api/auth/me` 엔드포인트로 GET 요청을 보냅니다.
        // `customFetch` 내부의 Axios 인터셉터가 `Authorization: Bearer <token>` 헤더를 자동으로 추가합니다.
        const userData = await customFetch<User>(`/api/auth/me`, {
          method: 'GET',
        });
        return userData; // 성공적으로 사용자 정보를 가져오면 반환
      } catch (e) {
        // API 호출 중 에러 발생 (예: 토큰 만료, 유효하지 않은 토큰, 네트워크 오류 등)
        console.error("사용자 정보 페칭 중 에러 발생 (토큰 만료 또는 유효하지 않음): ", e);
        // 에러 발생 시 localStorage에서 토큰을 제거하여 자동 로그아웃을 유도합니다.
        if (typeof window !== 'undefined') {
          localStorage.removeItem('authToken');
        }
        setToken(null); // 토큰 상태를 null로 설정
        return null; // 사용자 정보도 null로 설정
      }
    },
    enabled: !!token, // 이 쿼리는 `token` 상태가 존재(null이 아님)할 때만 실행되도록 활성화합니다.
    staleTime: 5 * 60 * 1000, // 5분 (300,000ms) 동안 데이터가 'fresh' 상태로 유지됩니다.
                              // 이 시간 동안은 새로운 쿼리 요청이 와도 백그라운드에서 재페칭하지 않습니다.
    gcTime: 10 * 60 * 1000, // 10분 (600,000ms) 동안 캐시된 데이터가 가비지 컬렉션되지 않고 유지됩니다.
                            // 이 시간 이후에는 캐시에서 제거될 수 있습니다.
    retry: false, // 쿼리 실패 시 자동으로 재시도하지 않도록 설정합니다.
                  // 인증 관련 쿼리는 만료된 토큰으로 재시도하지 않고 즉시 로그아웃 처리하는 것이 더 적절합니다.
  });

  // -------------------------------------------------------------------------
  // 3.4. 로그인 뮤테이션 (Login Mutation with TanStack Query)
  // -------------------------------------------------------------------------

  /**
   * @hook useMutation
   * @brief 사용자 로그인 요청을 처리하는 TanStack Mutation 훅입니다.
   * 백엔드의 `/api/auth/login` 엔드포인트로 로그인 정보를 전송하고 JWT 토큰을 받습니다.
   */
  const loginMutation = useMutation({
    mutationFn: async ({ userId, userPwd }: { userId: string; userPwd: string }) => {
      // `customFetch` 함수를 사용하여 로그인 API를 호출합니다.
      // 요청 본문에 사용자 ID와 비밀번호를 JSON 형태로 포함합니다.
      const response = await customFetch<{ token: string; memberId: number; userId: string; userName: string }>(
        `/api/auth/login`,
        {
          method: 'POST',
          body: { userId, userPwd }, // 요청 본문 (JSON.stringify는 customFetch 내부에서 처리)
        }
      );
      return response; // 백엔드로부터 받은 응답 데이터를 반환
    },
    onSuccess: (data) => {
      // 뮤테이션(로그인) 성공 시 실행되는 콜백 함수입니다.
      // 백엔드에서 받은 JWT 토큰을 `localStorage`에 저장하여 브라우저에 영구적으로 보관합니다.
      if (typeof window !== 'undefined') {
        localStorage.setItem('authToken', data.token);
      }
      setToken(data.token); // React 상태에도 토큰을 업데이트하여 즉시 UI에 반영되도록 합니다.

      // TanStack Query 캐시를 수동으로 업데이트합니다.
      // `currentUser` 쿼리의 캐시를 업데이트하여, `useQuery`가 다시 페칭하지 않고도
      // 로그인 성공 직후 최신 사용자 정보를 UI에 보여줄 수 있도록 합니다.
      queryClient.setQueryData(['currentUser', data.token], {
        memberId: data.memberId,
        userId: data.userId,
        userName: data.userName,
      });

      router.push('/'); // 로그인 성공 후 사용자를 메인 페이지로 리다이렉트합니다.
    },
    onError: (error: Error) => {
      // 뮤테이션(로그인) 실패 시 실행되는 콜백 함수입니다.
      // 사용자에게 로그인 오류 메시지를 알림 창으로 표시합니다.
      alert(`로그인 오류: ${error.message}`);
    },
  });

  // -------------------------------------------------------------------------
  // 3.5. 인증 관련 공개 함수 (Public Authentication Functions)
  // -------------------------------------------------------------------------

  /**
   * @function login
   * @brief 로그인 요청을 시작하는 비동기 함수입니다.
   * `LoginModal`과 같은 로그인 폼 컴포넌트에서 호출됩니다.
   *
   * @param {string} userId - 사용자가 입력한 아이디
   * @param {string} userPwd - 사용자가 입력한 비밀번호
   * @returns {Promise<void>} 로그인 뮤테이션의 완료를 기다립니다.
   */
  const login = async (userId: string, userPwd: string) => {
    await loginMutation.mutateAsync({ userId, userPwd });
  };

  /**
   * @function logout
   * @brief 사용자를 로그아웃 처리하는 함수입니다.
   * JWT 토큰을 제거하고, 인증 상태를 초기화하며, 사용자 정보를 무효화합니다.
   */
  const logout = () => {
    // `localStorage`에서 JWT 토큰을 제거하여 로그인 상태를 해제합니다.
    if (typeof window !== 'undefined') {
      localStorage.removeItem('authToken');
    }
    setToken(null); // React 상태의 토큰을 `null`로 초기화합니다.

    // TanStack Query 캐시에서 'currentUser' 쿼리의 모든 데이터를 제거하여
    // 사용자 정보가 더 이상 캐시되지 않도록 합니다. 이는 즉시 로그아웃 상태를 반영합니다.
    queryClient.removeQueries({ queryKey: ['currentUser'] });

    router.push('/'); // 로그아웃 후 사용자를 메인 페이지로 리다이렉트합니다.
  };

  // -------------------------------------------------------------------------
  // 3.6. 파생된 상태 (Derived States)
  // -------------------------------------------------------------------------

  /**
   * @const isLoggedIn
   * @brief 사용자가 로그인 상태인지 여부를 나타내는 불리언 값입니다.
   * `token`이 존재하고 `user` 정보도 성공적으로 로드되었을 때만 `true`가 됩니다.
   */
  const isLoggedIn = !!token && !!user;

  /**
   * @const isLoading
   * @brief 인증 관련 작업(로그인 요청 또는 사용자 정보 페칭)이 진행 중인지 여부입니다.
   * 이 값을 통해 UI에서 로딩 스피너를 표시하거나 버튼을 비활성화할 수 있습니다.
   */
  const isLoading = loginMutation.isPending || isUserLoading;

  // -------------------------------------------------------------------------
  // 3.7. Context 값 제공 (Context Value Provision)
  // -------------------------------------------------------------------------

  /**
   * @const value
   * @brief AuthContext.Provider를 통해 하위 컴포넌트들에게 제공될 객체입니다.
   */
  const value = {
    isLoggedIn, // 로그인 상태
    user,       // 사용자 정보
    login,      // 로그인 함수
    logout,     // 로그아웃 함수
    isLoading,  // 로딩 상태
  };

  // AuthContext.Provider로 자식 컴포넌트들을 감싸서, 정의된 `value`를 제공합니다.
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

// -----------------------------------------------------------------------------
// 4. useAuth 커스텀 훅
// -----------------------------------------------------------------------------

/**
 * @function useAuth
 * @brief AuthContext의 값을 쉽게 접근할 수 있도록 하는 커스텀 훅입니다.
 * 이 훅은 반드시 `AuthProvider` 컴포넌트의 하위 컴포넌트에서만 호출되어야 합니다.
 *
 * @returns {AuthContextType} AuthContext에서 제공하는 값들
 * @throws {Error} `AuthProvider` 외부에서 호출될 경우 에러 발생
 */
export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    // 개발자가 `AuthProvider`로 감싸지 않은 곳에서 `useAuth` 훅을 사용했을 때 명확한 에러 메시지를 제공합니다.
    throw new Error('useAuth는 AuthProvider 내에서 사용되어야 합니다.');
  }
  return context;
}