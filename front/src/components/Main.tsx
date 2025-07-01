"use client";

import React from "react";
import Image from "next/image";

export default function Main() {
    return (
      // <main className="flex flex-col items-center justify-center">
      //   <h1 className="text-2xl font-bold">이웃마켓에 오신 것을 환영합니다!</h1>
      //   <p className="text-lg mt-4">이웃과 중고 거래를 시작해보세요.</p>
      // </main>
      <main className="container mx-auto py-8 px-4">
      <h2 className="text-3xl font-bold mb-6 text-center">최신 상품</h2>
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
        {/* 여기에 실제 상품 목록을 렌더링하는 로직이 들어갑니다. */}
        {/* 예시 상품 카드 */}
        {[1, 2, 3, 4, 5, 6, 7, 8].map(item => (
          <div key={item} className="bg-white rounded-lg shadow-md overflow-hidden">
            <Image
              src={`https://placehold.co/300x200/FF7F50/FFFFFF?text=Product+${item}`} 
              alt={`상품 이미지 ${item}`} 
              width={300}
              height={200}
              className="w-full h-48 object-cover"
              unoptimized={true} // 플레이스홀더 이미지이므로 최적화 비활성화
            />
            <div className="p-4">
              <h3 className="font-semibold text-lg mb-1">상품명 {item}</h3>
              <p className="text-gray-600 text-sm mb-2">카테고리</p>
              <p className="text-xl font-bold text-orange-600">{(10000 + item * 1000).toLocaleString()}원</p>
            </div>
          </div>
        ))}
      </div>
    </main>
    );
}