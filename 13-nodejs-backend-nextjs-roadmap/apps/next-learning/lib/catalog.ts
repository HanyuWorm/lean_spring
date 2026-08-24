import "server-only";
import { cacheLife } from "next/cache";

export interface Product {
  readonly id: string;
  readonly name: string;
  readonly level: "Foundation" | "Backend" | "Advanced";
  readonly summary: string;
  readonly outcomes: readonly string[];
}

const products: readonly Product[] = [
  {
    id: "event-loop",
    name: "Node Event Loop",
    level: "Foundation",
    summary: "Hiểu callback scheduling, microtasks và nguyên nhân event-loop starvation.",
    outcomes: ["Đo event-loop delay", "Tách CPU work", "Thiết kế timeout có cancellation"],
  },
  {
    id: "reliable-api",
    name: "Reliable Backend API",
    level: "Backend",
    summary: "Validation, idempotency, transaction boundary và bounded concurrency.",
    outcomes: ["Error contract ổn định", "Idempotency durable", "Graceful shutdown"],
  },
  {
    id: "next-boundaries",
    name: "Next.js Boundaries",
    level: "Advanced",
    summary: "Server/Client Components, cache scope và authorization gần dữ liệu.",
    outcomes: ["Giảm client bundle", "Cache không lộ tenant data", "DAL trả DTO tối thiểu"],
  },
];

export async function getProducts(query = ""): Promise<readonly Product[]> {
  "use cache";
  cacheLife("minutes");
  const normalized = query.trim().toLocaleLowerCase("en");
  if (!normalized) return products;
  return products.filter((product) =>
    `${product.name} ${product.summary} ${product.level}`.toLocaleLowerCase("en").includes(normalized),
  );
}

export async function getProduct(id: string): Promise<Product | undefined> {
  "use cache";
  cacheLife("hours");
  return products.find((product) => product.id === id);
}

export async function getProductIds(): Promise<readonly string[]> {
  "use cache";
  cacheLife("max");
  return products.map((product) => product.id);
}
