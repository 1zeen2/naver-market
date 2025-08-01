"use client";

import React from "react";
import { Search } from "lucide-react";

interface SearchBarProps {
  value: string;
  onChange: (newValue: string) => void; // 검색어 변경 시 호출될 함수
  onSearch: () => void; // 검색 실행 시 호출될 함수 (돋보기 클릭 / Enter)
}

// SearchBar Component: Search UI, event
export default function SearchBar({ value, onChange, onSearch }: SearchBarProps) {
  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      onSearch();
    }
  };

  return (
    <div className="flex py-2 relative w-full">
      <input
        type="text"
        value={value} // 외부에서 받은 value prop 사용
        onChange={(e) => onChange(e.target.value)} // 외부에서 받은 onChange prop 호출
        onKeyDown={handleKeyDown} // Enter key 감지
        placeholder="검색어를 입력하세요."
        // flex-grow: 남은 공간을 채우도록 함, p-2: 패딩, pl-5 pr-10: 텍스트와 아이콘 공간 확보
        // border: 테두리, rounded-full: 둥근 모서리, focus: 포커스 시 스타일
        className="flex-grow p-2 pl-5 pr-5 border border-orange-500 rounded-full focus:outline-none focus:ring-2 focus:ring-orange-500 placeholder-center text-lg" 
      />
      {/* 돋보기 아이콘 (절대 위치) */}
      <div 
        className="absolute inset-y-0 right-0 pr-5 flex items-center cursor-pointer"
        onClick={onSearch} // 돋보기 아이콘 클릭 시 외부에서 받은 onSearch prop 호출
      >
        <Search className="h-5 w-5 text-gray-400" aria-hidden="true" />
      </div>
    </div>
  )
}