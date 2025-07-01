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
    <div className="flex justify-center py-2 relative w-1/3 mx-auto">
      <input
        type="text"
        value={value} // 외부에서 받은 value prop 사용
        onChange={(e) => onChange(e.target.value)} // 외부에서 받은 onChange prop 호출
        onKeyDown={handleKeyDown} // Enter key 감지
        placeholder="검색어를 입력하세요."
        className="flex-grow p-2 pl-5 pr-10 border border-orange-500 rounded-full focus:outline-none focus:ring-2 focus:ring-orange-500 placeholder-center text-lg" 
      />
      <div 
        className="absolute inset-y-0 right-0 pr-3 flex items-center cursor-pointer"
        onClick={onSearch} // 돋보기 아이콘 클릭 시 외부에서 받은 onSearch prop 호출
      >
        <Search className="h-5 w-5 text-gray-400" aria-hidden="true" />
      </div>
    </div>
  )
}