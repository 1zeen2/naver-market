import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  /* config options here */
  // 기존 설정 (예: images)이 있다면 여기에 유지하거나 추가합니다.
  images: {
    remotePatterns: [
      {
        protocol: 'https',
        hostname: 'placehold.co', // placeholder 이미지 사용 시
        port: '',
        pathname: '/**',
      },
      // 여기에 실제 상품 이미지가 호스팅될 도메인을 추가해야 합니다.
      // 예: { protocol: 'https', hostname: 'your-image-cdn.com' },
    ],
  },
  // API 프록시 설정 추가
  async rewrites() {
    return [
      // {
      //   source: '/api/:path*', // /api 로 시작하는 모든 요청을
      //   destination: 'http://localhost:80/api/:path*', // 백엔드 서버로 프록시
      // },
    ];
  },
};

export default nextConfig;
