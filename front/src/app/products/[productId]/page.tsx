"use client";

import React, { useState } from "react";
import Image from "next/image";
import { useQuery } from "@tanstack/react-query";
import { ProductDetailResponseDto } from "@/types/product";
import { useParams } from "next/navigation";

/**
 * @brief 주어진 ISO 8601 형식의 날짜 문자열을 "몇 시간 전", "몇 분 전", "방금 전"과 같은
 * 상대적인 시간 형식으로 변환합니다. 년, 월, 일 단위는 달력 기반으로 정확하게 계산하며,
 * 시간, 분, 초 단위는 경과된 시간을 기준으로 계산합니다.
 * @param dateString ISO 8601 형식의 날짜 문자열 (예: "2023-10-27T10:00:00.000Z")
 * @returns string 상대적인 시간 문자열
 */
function formatTimeAgo(dateString: string): string {
  const now = new Date();
  const updatedDate = new Date(dateString);

  const startOfNow = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const startOfUpdatedDate = new Date(updatedDate.getFullYear(), updatedDate.getMonth(), updatedDate.getDate());
  const diffInDaysCalendar = Math.floor((startOfNow.getTime() - startOfUpdatedDate.getTime()) / (1000 * 60 * 60 * 24));

  const diffInMilliseconds = now.getTime() - updatedDate.getTime();
  const diffInSeconds = Math.floor(diffInMilliseconds / 1000);

  let years = now.getFullYear() - updatedDate.getFullYear();
  let months = now.getMonth() - updatedDate.getMonth();
  
  if (now.getDate() < updatedDate.getDate()) {
    months--;
  }
  if (months < 0) {
    years--;
    months += 12;
  }

  if (years > 0) {
    return `${years}년 전`;
  } else if (months > 0) {
    return `${months}개월 전`;
  } else if (diffInDaysCalendar > 0) {
    return `${diffInDaysCalendar}일 전`;
  } else {
    const hours = Math.floor(diffInSeconds / 3600);
    const minutes = Math.floor((diffInSeconds % 3600) / 60);

    if (hours > 0) {
      return `${hours}시간 전`;
    } else if (minutes > 0) {
      return `${minutes}분 전`;
    } else if (diffInSeconds >= 10) {
      return `${diffInSeconds}초 전`;
    } else {
      return "방금 전";
    }
  }
}

/**
 * @brief 특정 상품의 상세 정보를 백엔드에서 가져오는 비동기 함수입니다.
 * @param productId 조회할 상품의 ID
 * @returns Promise<ProductDetailResponseDto> 상품 상세 정보를 반환합니다.
 * @throws Error API 호출 실패 시 에러를 발생시킵니다.
 */
async function fetchProductDetail(productId: string): Promise<ProductDetailResponseDto> {
  const res = await fetch(`/api/products/${productId}`);
  
  if (!res.ok) {
    // 404 Not Found도 에러로 처리하여 useQuery의 isError 상태를 활성화
    if (res.status === 404) {
      throw new Error('상품을 찾을 수 없습니다.');
    }
    throw new Error('상품 상세 정보를 불러오는데 실패했습니다.');
  }
  return res.json();
}

export default function ProductDetailPage() {
  const params = useParams();
  const productId = params.productId as string; // productId는 string으로 넘어옴

  // 현재 활성화된 이미지의 인덱스를 관리하는 상태
  const [currentImageIndex, setCurrentImageIndex] = useState(0);

  const { data: product, isLoading, isError, error } = useQuery<ProductDetailResponseDto, Error>({
    queryKey: ['productDetail', productId], // 쿼리 키에 productId 포함
    queryFn: () => fetchProductDetail(productId), // productId를 fetch 함수에 전달
    enabled: !!productId, // productId가 있을 때만 쿼리 실행
  });

  /**
   * @brief 백엔드에서 받은 상품 상태를 사용자 친화적인 이름으로 변환합니다.
   * @param status 백엔드 상품 상태 문자열 (예: "ACTIVE", "RESERVED", "SOLD_OUT")
   * @returns string 변환된 사용자 친화적인 상태 이름
   */
  const getProductStatusDisplayName = (status: string): string => {
    switch (status) {
      case 'ACTIVE':
        return '판매 중';
      case 'RESERVED':
        return '예약 중';
      case 'SOLD_OUT':
        return '판매 완료';
      default:
        return status;
    }
  };

  if (isLoading) {
    return (
      <main className="container mx-auto py-8 px-4 text-center">
        <h2 className="text-3xl font-bold mb-6">상품 상세 정보</h2>
        <div className="text-gray-600">상품 정보를 불러오는 중...</div>
      </main>
    );
  }

  if (isError) {
    return (
      <main className="container mx-auto py-8 px-4 text-center">
        <h2 className="text-3xl font-bold mb-6">상품 상세 정보</h2>
        <div className="text-red-600">상품 정보를 불러오는데 실패했습니다: {error?.message}</div>
      </main>
    );
  }

  if (!product) {
    return (
      <main className="container mx-auto py-8 px-4 text-center">
        <h2 className="text-3xl font-bold mb-6">상품 상세 정보</h2>
        <div className="text-gray-500">상품 정보를 찾을 수 없습니다.</div>
      </main>
    );
  }

  // 이미지 슬라이더 다음 이미지로 이동
  const goToNextImage = () => {
    if (product.imageUrls && product.imageUrls.length > 0) {
      setCurrentImageIndex((prevIndex) => (prevIndex + 1) % product.imageUrls.length);
    }
  };

  // 이미지 슬라이더 이전 이미지로 이동
  const goToPrevImage = () => {
    if (product.imageUrls && product.imageUrls.length > 0) {
      setCurrentImageIndex((prevIndex) => 
        (prevIndex - 1 + product.imageUrls.length) % product.imageUrls.length
      );
    }
  };

  return (
    <main className="container mx-auto py-8 px-4 max-w-4xl">
      <h2 className="text-3xl font-bold mb-8 text-center">상품 상세 정보</h2>
      
      <div className="bg-white rounded-lg shadow-xl p-6 md:p-8">
        {/* 상품 이미지 갤러리 */}
        <div className="relative w-full h-96 mb-6 rounded-lg overflow-hidden border border-gray-200">
          {product.imageUrls && product.imageUrls.length > 0 ? (
            <>
              <Image
                src={product.imageUrls[currentImageIndex]}
                alt={product.title}
                fill // 부모 요소에 맞춰 이미지 크기 조절
                style={{ objectFit: 'contain' }} // 이미지가 잘리지 않고 전체 보이도록
                unoptimized={true}
                className="transition-opacity duration-300 ease-in-out"
              />
              {product.imageUrls.length > 1 && (
                <>
                  <button
                    onClick={goToPrevImage}
                    className="absolute left-2 top-1/2 -translate-y-1/2 bg-black bg-opacity-50 text-white p-2 rounded-full z-10 hover:bg-opacity-75 transition-colors"
                    aria-label="Previous image"
                  >
                    &#10094; {/* 왼쪽 화살표 */}
                  </button>
                  <button
                    onClick={goToNextImage}
                    className="absolute right-2 top-1/2 -translate-y-1/2 bg-black bg-opacity-50 text-white p-2 rounded-full z-10 hover:bg-opacity-75 transition-colors"
                    aria-label="Next image"
                  >
                    &#10095; {/* 오른쪽 화살표 */}
                  </button>
                  <div className="absolute bottom-2 left-1/2 -translate-x-1/2 flex space-x-2 z-10">
                    {product.imageUrls.map((_, index) => (
                      <span
                        key={index}
                        className={`block w-3 h-3 rounded-full ${
                          index === currentImageIndex ? 'bg-orange-500' : 'bg-gray-300'
                        } cursor-pointer`}
                        onClick={() => setCurrentImageIndex(index)}
                        aria-label={`View image ${index + 1}`}
                      ></span>
                    ))}
                  </div>
                </>
              )}
            </>
          ) : (
            <div className="flex items-center justify-center w-full h-full bg-gray-100 text-gray-400">
              <span className="text-lg">이미지 없음</span>
            </div>
          )}
        </div>

        {/* 상품 정보 */}
        <div className="space-y-4">
          <h1 className="text-3xl font-bold text-gray-900">{product.title}</h1>
          <p className="text-4xl font-extrabold text-orange-600">{product.price.toLocaleString()}원</p>
          
          <hr className="border-gray-200" />

          <div className="flex justify-between items-center text-gray-600 text-sm">
            <span>카테고리: <span className="font-medium">{product.category}</span></span>
            <span>상태: <span className="font-medium">{getProductStatusDisplayName(product.status)}</span></span>
          </div>

          <p className="text-gray-700 leading-relaxed whitespace-pre-wrap">{product.description}</p>
          
          <hr className="border-gray-200" />

          <div className="flex justify-between items-center text-gray-500 text-sm">
            <span>조회수: {product.views}</span>
            <span>등록일: {formatTimeAgo(product.createdAt)}</span>
            <span>최근 수정: {formatTimeAgo(product.updatedAt)}</span>
          </div>

          {/* 판매자 정보, 채팅, 구매 버튼 등 추가 가능 */}
          <div className="mt-6 flex justify-center space-x-4">
            <button className="bg-blue-500 hover:bg-blue-600 text-white font-bold py-3 px-6 rounded-lg shadow-md transition-colors duration-200">
              판매자에게 채팅하기
            </button>
            <button className="bg-orange-500 hover:bg-orange-600 text-white font-bold py-3 px-6 rounded-lg shadow-md transition-colors duration-200">
              구매하기
            </button>
          </div>
        </div>
      </div>
    </main>
  );
}