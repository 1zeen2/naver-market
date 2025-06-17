import { useState, useEffect } from "react";

interface ModalProps {
  isOpen: boolean;
  closeModal: () => void;
}

export default function LoginModal({ isOpen, closeModal }: ModalProps) {
  const [keepLoggedIn, setKeepLoggedIn] = useState(false); // 로그인 상태 유지 상태 관리
  const [username, setUsername] = useState(""); // 아이디 상태
  const [userPwd, setuserPwd] = useState(""); // 비밀번호 상태

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

  if (!isOpen) return null; // 모달이 열려야만 내용이 보이도록 처리

  // 로그인 버튼 클릭 혹은 엔터 키로 로그인인
  const handleLogin = (e: React.FormEvent) => {
    e.preventDefault(); // 폼 제출 시 페이지 리로딩 방지
    // 여기서 실제 로그인 로직을 추가하세요.
  };

  return (
    <div className="fixed inset-0 bg-gray-500 bg-opacity-50 flex justify-center items-center z-50">
      <div className="bg-white p-10 rounded-lg shadow-lg w-full max-w-xl">
        {/* 로그인 제목 */}
        <h2 className="text-xl font-bold mb-6 text-center">로그인</h2>

        <form onSubmit={handleLogin}>
          {/* 아이디 입력 */}
          <div className="mb-4">
            <input
              type="text"
              placeholder="아이디 입력"
              className="w-full p-3 border rounded-md"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
            />
          </div>

          {/* 비밀번호 입력 */}
          <div className="mb-4">
            <input
              type="password"
              placeholder="비밀번호 입력"
              className="w-full p-3 border rounded-md"
              value={userPwd}
              onChange={(e) => setuserPwd(e.target.value)}
            />
          </div>

          {/* 로그인 상태 유지 토글 */}
          <div className="flex items-center mb-4">
            <input
              type="checkbox"
              checked={keepLoggedIn}
              onChange={() => setKeepLoggedIn(!keepLoggedIn)}
              className="mr-2"
            />
            <span className="text-sm">로그인 상태 유지</span>
          </div>

          {/* 로그인 버튼 */}
          <button
            type="submit"
            className="w-full bg-blue-500 text-white p-3 rounded-md hover:bg-blue-600"
          >
            로그인
          </button>
        </form>

        {/* 아이디 찾기, 비밀번호 찾기 버튼 */}
        <div className="mt-4 text-sm text-gray-600 space-x-4 text-center">
          <a href="#" className="hover:underline">
            아이디 찾기
          </a>
          <span>|</span>
          <a href="#" className="hover:underline">
            비밀번호 찾기
          </a>
        </div>

        {/* 하단 약관 */}
        <div className="mt-8 text-xs text-gray-500 flex justify-center space-x-4">
          <a href="#" className="hover:underline">
            이용 약관
          </a>
          <span>|</span>
          <a href="#" className="hover:underline">
            개인정보 처리 방침
          </a>
          <span>|</span>
          <a href="#" className="hover:underline">
            책임의 한계와 법적 고지
          </a>
          <span>|</span>
          <a href="#" className="hover:underline">
            회원 정보 고객 센터
          </a>
        </div>

        {/* 모달 닫기 버튼 */}
        <button
          onClick={closeModal}
          className="mt-4 w-full text-gray-500 hover:text-gray-700"
        >
          닫기
        </button>
      </div>
    </div>
  );
}
