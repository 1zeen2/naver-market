import Main from "@/components/Main";

/**
 * @brief Next.js의 루트 경로('/')에 해당하는 페이지 컴포넌트입니다.
 * 이 컴포넌트는 클라이언트 컴포넌트인 Main을 렌더링하여
 * 메인 피드 상품 목록을 사용자에게 표시합니다.
 */
export default function Home() {
  return (
    <Main />
  );
}