"use client";

import React from "react";
import Image from "next/image";
import { useQuery } from '@tanstack/react-query';
import { Page, ProductListResponseDto } from '@/types/product';
import Link from "next/link";
import { customFetch } from "@/utils/customFetch";

/**
 * @brief 주어진 ISO 8601 형식의 날짜 문자열을 "몇 시간 전", "몇 분 전", "방금 전"과 같은
 * 상대적인 시간 형식으로 변환합니다. 년, 월, 일 단위는 달력 기반으로 정확하게 계산하며,
 * 시간, 분, 초 단위는 경과된 시간을 기준으로 계산합니다.
 * @param dateString ISO 8601 형식의 날짜 문자열 (예: "2023-10-27T10:00:00.000Z")
 * @returns string 상대적인 시간 문자열
 */
function formatTimeAgo(dateString: string): string {
  const now = new Date(); // 현재 시간
  const updatedDate = new Date(dateString); // 상품 업데이트 시간

  // '일 전'을 계산하기 위해 현재 날짜와 업데이트 날짜의 자정(00:00:00)을 기준으로 차이를 계산합니다.
  // 이 방식은 달력상의 날짜 경계에 따라 '일'이 증가하도록 합니다.
  const startOfNow = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const startOfUpdatedDate = new Date(updatedDate.getFullYear(), updatedDate.getMonth(), updatedDate.getDate());
  const diffInDaysCalendar = Math.floor((startOfNow.getTime() - startOfUpdatedDate.getTime()) / (1000 * 60 * 60 * 24));

  // '시간 전', '분 전', '초 전'을 계산하기 위해 총 경과된 시간을 사용합니다.
  const diffInMilliseconds = now.getTime() - updatedDate.getTime();
  const diffInSeconds = Math.floor(diffInMilliseconds / 1000);

  // 1. 년, 월 (달력 기준) 계산
  // 현재 날짜의 '일'이 업데이트 날짜의 '일'보다 작으면 월을 조정하여 정확한 월 차이를 계산합니다.
  let years = now.getFullYear() - updatedDate.getFullYear();
  let months = now.getMonth() - updatedDate.getMonth();
  
  if (now.getDate() < updatedDate.getDate()) {
    months--;
  }
  // 월이 음수일 경우 년을 조정합니다.
  if (months < 0) {
    years--;
    months += 12;
  }

  // 2. 가장 큰 단위부터 출력 (년, 월, 일 우선)
  if (years > 0) {
    return `${years}년 전`;
  } else if (months > 0) {
    return `${months}개월 전`;
  } else if (diffInDaysCalendar > 0) { // 달력상의 날짜 차이를 기준으로 '일 전'을 표시
    return `${diffInDaysCalendar}일 전`;
  } else { // 같은 날짜 내에서는 경과된 시간을 기준으로 시간, 분, 초를 표시
    const hours = Math.floor(diffInSeconds / 3600);
    const minutes = Math.floor((diffInSeconds % 3600) / 60);

    if (hours > 0) {
      return `${hours}시간 전`;
    } else if (minutes > 0) {
      return `${minutes}분 전`;
    } else if (diffInSeconds >= 10) { // 10초 이상
      return `${diffInSeconds}초 전`;
    } else {
      return "방금 전"; // 10초 미만
    }
  }
}

/**
 * @brief 메인 피드에 표시될 상품 목록을 백엔드에서 가져오는 비동기 함수입니다.
 * 백엔드에서 Page<ProductListResponseDto> 형태로 데이터를 반환하므로, 이를 처리합니다.
 * @returns Promise<Page<ProductListResponseDto>> 상품 객체 배열을 포함하는 Page 객체를 반환합니다.
 * @throws Error API 호출 실패 시 에러를 발생시킵니다.
 */
async function fetchMainFeedProducts(): Promise<Page<ProductListResponseDto>> {
  // 핵심 수정: customFetch를 사용하여 백엔드로 직접 요청하도록 변경
  const data = await customFetch<Page<ProductListResponseDto>>(`/api/products/main-feed`, { method: 'GET' });
  return data;
}

// 기본 이미지 URL (프로젝트 내에 실제 파일이 있어야 합니다. 예: public/placeholder-image.png)
const PLACEHOLDER_IMAGE_URL = "/images/placeholder-image.png"; // 적절한 경로로 수정하세요.

export default function Main() {
  const { data: productPage, isLoading, isError, error } = useQuery<Page<ProductListResponseDto>, Error>({
    queryKey: ['mainFeedProducts'], // 쿼리 키 : 메인 피드 상품 목록을 나타냄.
    queryFn: fetchMainFeedProducts, // 데이터 페칭 함수
  });

  if (isLoading) {
    return (
      <main className="container mx-auto py-8 px-4 text-center">
        <h2 className="text-3xl font-bold mb-6">최신 상품</h2>
        <div className="text-gray-600">상품을 불러오는 중...</div>
      </main>
    );
  }

  if (isError) {
    return(
      <main className="container mx-auto py-8 px-4 text-center">
        <h2 className="text-3xl font-bold mb-6">최신 상품</h2>
        <div className="text-red-600">상품을 불러오는데 실패했습니다: {error?.message}</div>
      </main>
    );
  }

  // products가 undefined일 경우 빈 배열로 처리하여 map 오류 방지
  const productList = productPage?.content || [];

  return (
    <main className="container mx-auto py-8 px-4">
      <h2 className="text-3xl font-bold mb-6 text-center">최근 등록된 상품</h2>
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
        {productList.length > 0 ? (
          productList.map((product: ProductListResponseDto) => (
            <Link key={product.productId} href={`/products/${product.productId}`} passHref>
              <div key={product.productId} className="bg-white rounded-lg shadow-md overflow-hidden">
                <Image 
                  src={product.mainImageUrl && product.mainImageUrl.trim() !== "" ? product.mainImageUrl : PLACEHOLDER_IMAGE_URL}
                  alt={product.title || "상품 이미지"}
                  width={300}
                  height={200}
                  className="w-full h-48 object-cover"
                  unoptimized={true} // 외부 이미지 URL이므로 최적화 비활성화 (next.config.js 설정 필요 시 제거)
                />
                <div className="p-4">
                  <h3 className="font-semibold text-lg mb-1">{product.title}</h3>
                  <p className="text-gray-600 text-sm mb-2">{product.category}</p>
                  <p className="text-gray-500 text-xs mb-2">{formatTimeAgo(product.updatedAt)}</p>
                  <p className="text-xl font-bold text-orange-600">{product.price.toLocaleString()}원</p>
                </div>
              </div>
            </Link>
          ))
        ) : (
          <div className="col-span-full text-center text-gray-500 text-lg">
            등록된 상품이 없습니다.
          </div>
        )}
      </div>
      {productPage && productPage.totalPages > 1 && (
        <div className="flex justify-center mt-8">
          {/* 실제 페이지네이션 로직 (버튼 클릭 시 page 파라미터 변경하여 useQuery 재호출 등) 필요 */}
          <p className="text-gray-600">
            페이지 {productPage.number + 1} / {productPage.totalPages}
          </p>
        </div>
      )}
    </main>
  );
}