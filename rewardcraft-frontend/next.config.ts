import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  webpack: (config, { dev }) => {
    if (dev) {
      config.watchOptions = {
        poll: 1000,       // 1초마다 파일 변경 감지 (WSL2 /mnt/c/ 경로용)
        aggregateTimeout: 300,
      };
    }
    return config;
  },
};

export default nextConfig;
