"use client";

import { useState } from "react";

export function FavoriteButton({ productId }: Readonly<{ productId: string }>) {
  const [favorite, setFavorite] = useState(false);

  return (
    <button
      className="favorite"
      type="button"
      aria-pressed={favorite}
      aria-label={`${favorite ? "Bỏ" : "Thêm"} ${productId} khỏi danh sách yêu thích`}
      onClick={() => setFavorite((value) => !value)}
    >
      {favorite ? "★ Đã lưu" : "☆ Lưu"}
    </button>
  );
}
