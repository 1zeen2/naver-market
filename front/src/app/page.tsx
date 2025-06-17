"use client";

import { useEffect, useState } from "react";
import Main from "@/components/Main";

export default function Home() {
  const [isClient, setIsClient] = useState(false);

  useEffect(() => {
    setIsClient(true);
  }, []);

  if (!isClient) {
    return <div>Loading...</div>;
  }

  return (
    <Main />
  );
}