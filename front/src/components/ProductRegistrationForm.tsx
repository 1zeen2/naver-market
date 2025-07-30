'use client';

import React, { useState, ChangeEvent, FormEvent, useEffect } from 'react';
import api from '@/api/api'; // Axios 인스턴스
import axios from 'axios'; // Axios 타입 체크를 위해 필요
import { useRouter } from 'next/navigation';
import Image from 'next/image'; // Next.js Image 컴포넌트

/**
 * @file ProductRegistrationForm.tsx
 * @brief 상품 등록 폼 UI 및 로직을 담당하는 클라이언트 컴포넌트입니다.
 * 사용자가 상품 정보를 입력하고 이미지를 업로드하며, 백엔드 API와 통신합니다.
 */
export default function ProductRegistrationForm() {
  const router = useRouter(); // Next.js 라우터 인스턴스

  // 폼 필드 상태 관리
  const [title, setTitle] = useState<string>('');
  const [category, setCategory] = useState<string>('');
  const [price, setPrice] = useState<number | null>(null); // price는 Long 타입에 맞춰 정수로 보냄
  const [description, setDescription] = useState<string>('');
  const [preferredTradeLocation, setPreferredTradeLocation] = useState<string>('');

  // 이미지 파일 상태 관리
  const [mainImage, setMainImage] = useState<File | null>(null);
  const [detailImages, setDetailImages] = useState<File[]>([]); // 상세 이미지 파일들

  // 이미지 미리보기 URL 상태 관리
  const [mainImagePreview, setMainImagePreview] = useState<string | null>(null);
  const [detailImagePreviews, setDetailImagePreviews] = useState<string[]>([]);

  // UI 상태 관리
  const [isLoading, setIsLoading] = useState<boolean>(false); // 로딩 상태
  const [error, setError] = useState<string | null>(null); // 에러 메시지
  const [success, setSuccess] = useState<string | null>(null); // 성공 메시지

  // 컴포넌트 언마운트 시 미리보기 URL 해제 (메모리 누수 방지)
  useEffect(() => {
    return () => {
      if (mainImagePreview) URL.revokeObjectURL(mainImagePreview);
      detailImagePreviews.forEach(url => URL.revokeObjectURL(url));
    };
  }, [mainImagePreview, detailImagePreviews]);

  /**
   * @brief 일반 텍스트 입력 필드의 변경을 처리합니다.
   * @param e 입력 이벤트 객체
   */
  const handleChange = (e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setError(null); // 입력 시 에러 메시지 초기화

    switch (name) {
      case 'title':
        setTitle(value);
        break;
      case 'category':
        setCategory(value);
        break;
      case 'price':
        if (value === '' || /^\d+$/.test(value)) {
          setPrice(value === '' ? null : parseInt(value, 10));
        } else {
          setError('가격은 숫자만 입력 가능합니다.');
        }
        break;
      case 'description':
        setDescription(value);
        break;
      case 'preferredTradeLocation':
        setPreferredTradeLocation(value);
        break;
      default:
        break;
    }
  };

  /**
   * @brief 메인 이미지 파일 입력의 변경을 처리하고 미리보기를 생성합니다.
   * @param e 파일 입력 이벤트 객체
   */
  const handleMainImageChange = (e: ChangeEvent<HTMLInputElement>) => {
    setError(null);
    const file = e.target.files?.[0];
    if (file) {
      setMainImage(file);
      if (mainImagePreview) URL.revokeObjectURL(mainImagePreview);
      setMainImagePreview(URL.createObjectURL(file));
    } else {
      setMainImage(null);
      if (mainImagePreview) URL.revokeObjectURL(mainImagePreview);
      setMainImagePreview(null);
    }
  };

  /**
   * @brief 상세 이미지 파일 입력의 변경을 처리하고 미리보기를 생성합니다.
   * 최대 9장까지 허용합니다.
   * @param e 파일 입력 이벤트 객체
   */
  const handleDetailImagesChange = (e: ChangeEvent<HTMLInputElement>) => {
    setError(null);
    const selectedFiles = Array.from(e.target.files || []);

    const totalImages = detailImages.length + selectedFiles.length;
    if (totalImages > 9) {
      setError(`상세 이미지는 최대 9장까지 등록 가능합니다. 현재 ${detailImages.length}장 선택됨.`);
      e.target.value = '';
      return;
    }

    setDetailImages(prevImages => [...prevImages, ...selectedFiles]);
    setDetailImagePreviews(prevPreviews => [
      ...prevPreviews,
      ...selectedFiles.map(file => URL.createObjectURL(file))
    ]);
    e.target.value = '';
  };

  /**
   * @brief 상세 이미지 미리보기를 삭제합니다.
   * @param index 삭제할 이미지의 인덱스
   */
  const removeDetailImage = (index: number) => {
    if (detailImagePreviews[index]) {
      URL.revokeObjectURL(detailImagePreviews[index]);
    }
    setDetailImages(prevImages => prevImages.filter((_, i) => i !== index));
    setDetailImagePreviews(prevPreviews => prevPreviews.filter((_, i) => i !== index));
  };

  /**
   * @brief 폼 제출을 처리하고 백엔드 API를 호출합니다.
   * @param e 폼 제출 이벤트 객체
   */
  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setIsLoading(true);
    setError(null);
    setSuccess(null);

    // --- 유효성 검사 ---
    if (!title.trim()) {
      setError('제목을 입력해주세요.');
      setIsLoading(false);
      return;
    }
    if (!category.trim()) {
      setError('카테고리를 선택해주세요.');
      setIsLoading(false);
      return;
    }
    if (price === null || isNaN(price) || price < 0 || !Number.isInteger(price)) {
      setError('가격은 0원 이상의 정수만 입력 가능합니다.');
      setIsLoading(false);
      return;
    }
    if (!description.trim()) {
      setError('설명을 입력해주세요.');
      setIsLoading(false);
      return;
    }
    if (!preferredTradeLocation.trim()) {
      setError('선호 거래 지역을 입력해주세요.');
      setIsLoading(false);
      return;
    }
    if (!mainImage || !(mainImage instanceof File)) {
      setError('대표 이미지를 등록해주세요.');
      setIsLoading(false);
      return;
    }
    if (detailImages.length > 9) {
      setError('상세 이미지는 최대 9장까지 등록 가능합니다.');
      setIsLoading(false);
      return;
    }
    // --- 유효성 검사 끝 ---

    const formData = new FormData();

    const productData = {
      title,
      category,
      price: price as number,
      description,
      preferredTradeLocation,
    };
    // Blob 생성 시 type을 명시하지 않거나,
    // 아예 Blob을 사용하지 않고 JSON.stringify 결과를 바로 append 하는 것도 가능합니다.
    // formData.append('request', JSON.stringify(productData)); // 이렇게도 가능합니다.
    // 하지만 Blob을 사용하면 Content-Type이 "application/json"으로 명시적으로 설정되어 문제를 일으킵니다.
    // Spring의 @RequestPart는 JSON Blob의 Content-Type을 특정하지 않아도 잘 처리합니다.
    // 최적의 방법은 아래와 같습니다.
    formData.append(
      "request",
      new Blob([JSON.stringify(productData)], { type: "application/json" })
    );
    
    console.log('Product DTO JSON (FormData에 추가될 내용):', JSON.stringify(productData));

    // 메인 이미지 파일 추가 (백엔드의 @RequestPart("mainImage")와 이름 일치)
    formData.append('mainImage', mainImage);

    // 상세 이미지 파일들 추가 (백엔드의 @RequestPart(value = "detailImages", required = false) List<MultipartFile> detailImages와 이름 일치)
    detailImages.forEach((file) => {
      formData.append('detailImages', file);
    });

    try {
      // Axios는 FormData를 보낼 때 Content-Type을 'multipart/form-data'로 자동으로 설정합니다.
      // 따라서 명시적으로 'Content-Type' 헤더를 설정할 필요가 없습니다.
      const response = await api.post('/api/products', formData); 

      if (response.status === 201) {
        setSuccess('상품이 성공적으로 등록되었습니다!');
        setError(null);
        // 폼 초기화 및 미리보기 URL 해제
        setTitle('');
        setCategory('');
        setPrice(null);
        setDescription('');
        setPreferredTradeLocation('');
        setMainImage(null);
        if (mainImagePreview) URL.revokeObjectURL(mainImagePreview);
        setMainImagePreview(null);
        setDetailImages([]);
        detailImagePreviews.forEach(url => URL.revokeObjectURL(url));
        setDetailImagePreviews([]);

        if (response.data && response.data.productId) {
          router.push(`/products/${response.data.productId}`);
        } else {
          router.push('/products');
        }
      } else {
        setError('상품 등록에 실패했습니다. (예상치 못한 응답)');
      }
    } catch (err) {
      if (axios.isAxiosError(err)) {
        const backendErrorMessage = err.response?.data?.message || err.response?.data?.error || '상품 등록 중 오류가 발생했습니다.';
        setError(backendErrorMessage);
        console.error('상품 등록 API 오류:', err.response?.data || err.message, err.response);
      } else {
        setError('알 수 없는 오류가 발생했습니다.');
        console.error('알 수 없는 오류:', err);
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-100 flex items-center justify-center p-4 sm:p-6 lg:p-8 font-inter">
      <div className="bg-white p-6 sm:p-8 rounded-xl shadow-lg w-full max-w-4xl border border-gray-200">
        <h2 className="text-3xl font-bold text-gray-800 mb-6 text-center">상품 등록</h2>

        {/* 에러 및 성공 메시지 표시 */}
        {error && (
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded-lg mb-4 text-center" role="alert">
            {error}
          </div>
        )}
        {success && (
          <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded-lg mb-4 text-center" role="alert">
            {success}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* 상품 제목 */}
          <div>
            <label htmlFor="title" className="block text-sm font-medium text-gray-700 mb-2">
              상품 제목 <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              id="title"
              name="title"
              value={title}
              onChange={handleChange}
              placeholder="상품 제목을 입력하세요 (예: 아이폰 15 프로 맥스)"
              className="mt-1 block w-full px-4 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              required
            />
          </div>

          {/* 카테고리 (Select Box로 변경 권장) */}
          <div>
            <label htmlFor="category" className="block text-sm font-medium text-gray-700 mb-2">
              카테고리 <span className="text-red-500">*</span>
            </label>
            <select
              id="category"
              name="category"
              value={category}
              onChange={handleChange}
              className="mt-1 block w-full px-4 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              required
            >
              <option value="">카테고리 선택</option>
              <option value="디지털/가전">디지털/가전</option>
              <option value="가구/인테리어">가구/인테리어</option>
              <option value="의류">의류</option>
              <option value="도서">도서</option>
              <option value="스포츠/레저">스포츠/레저</option>
              <option value="뷰티/미용">뷰티/미용</option>
              <option value="식품">식품</option>
              <option value="악기">악기</option>
              <option value="기타">기타</option>
            </select>
          </div>

          {/* 가격 */}
          <div>
            <label htmlFor="price" className="block text-sm font-medium text-gray-700 mb-2">
              가격 <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              id="price"
              name="price"
              value={price?.toString() || ''}
              onChange={handleChange}
              placeholder="가격을 입력하세요 (예: 1200000)"
              className="mt-1 block w-full px-4 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              required
            />
          </div>

          {/* 상품 설명 */}
          <div>
            <label htmlFor="description" className="block text-sm font-medium text-gray-700 mb-2">
              상품 설명 <span className="text-red-500">*</span>
            </label>
            <textarea
              id="description"
              name="description"
              value={description}
              onChange={handleChange}
              rows={5}
              placeholder="상품의 자세한 설명과 특징을 입력해주세요."
              className="mt-1 block w-full px-4 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              required
            ></textarea>
          </div>

          {/* 거래 희망 지역 */}
          <div>
            <label htmlFor="preferredTradeLocation" className="block text-sm font-medium text-gray-700 mb-2">
              거래 희망 지역 <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              id="preferredTradeLocation"
              name="preferredTradeLocation"
              value={preferredTradeLocation}
              onChange={handleChange}
              placeholder="거래를 희망하는 지역을 입력하세요 (예: 서울 강남구)"
              className="mt-1 block w-full px-4 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              required
            />
          </div>

          {/* 메인 이미지 업로드 */}
          <div>
            <label htmlFor="mainImage" className="block text-sm font-medium text-gray-700 mb-2">
              대표 사진 <span className="text-red-500">*</span>
            </label>
            <input
              type="file"
              id="mainImage"
              name="mainImage"
              accept="image/*"
              onChange={handleMainImageChange}
              className="mt-1 block w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-md file:border-0 file:text-sm file:font-semibold file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"
              required
            />
            {mainImagePreview && (
              <div className="mt-4 w-32 h-32 rounded-md overflow-hidden border border-gray-300 flex items-center justify-center bg-gray-50 relative">
                <Image src={mainImagePreview} alt="대표 이미지 미리보기" fill style={{ objectFit: 'cover' }} sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw" />
              </div>
            )}
          </div>

          {/* 상세 이미지 업로드 */}
          <div>
            <label htmlFor="detailImages" className="block text-sm font-medium text-gray-700 mb-2">
              상세 사진 (최대 9장)
            </label>
            <input
              type="file"
              id="detailImages"
              name="detailImages"
              accept="image/*"
              multiple
              onChange={handleDetailImagesChange}
              className="mt-1 block w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-md file:border-0 file:text-sm file:font-semibold file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"
            />
            {detailImagePreviews.length > 0 && (
              <div className="mt-4 grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
                {detailImagePreviews.map((preview, index) => (
                  <div key={index} className="relative w-24 h-24 rounded-md overflow-hidden border border-gray-300 flex items-center justify-center bg-gray-50">
                    <Image src={preview} alt={`상세 이미지 ${index + 1} 미리보기`} fill style={{ objectFit: 'cover' }} sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw" />
                    <button
                      type="button"
                      onClick={() => removeDetailImage(index)}
                      className="absolute top-1 right-1 bg-red-500 text-white rounded-full p-1 text-xs leading-none"
                      aria-label="이미지 삭제"
                    >
                      &times;
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* 폼 제출 버튼 */}
          <div className="flex justify-center">
            <button
              type="submit"
              disabled={isLoading}
              className={`w-full sm:w-auto px-6 py-3 rounded-md font-semibold text-white transition-colors duration-200 
                ${isLoading ? 'bg-blue-300 cursor-not-allowed' : 'bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2'}`}
            >
              {isLoading ? '등록 중...' : '상품 등록하기'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}