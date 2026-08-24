import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { getProduct, getProductIds } from "@/lib/catalog";

interface ProductPageProps {
  readonly params: Promise<{ id: string }>;
}

export async function generateStaticParams() {
  return (await getProductIds()).map((id) => ({ id }));
}

export async function generateMetadata({ params }: ProductPageProps): Promise<Metadata> {
  const product = await getProduct((await params).id);
  return product ? { title: product.name, description: product.summary } : {};
}

export default async function ProductPage({ params }: ProductPageProps) {
  const product = await getProduct((await params).id);
  if (!product) notFound();

  return (
    <article className="detail">
      <Link href="/">← Quay lại catalog</Link>
      <span className="level">{product.level}</span>
      <h1>{product.name}</h1>
      <p>{product.summary}</p>
      <h2>Kết quả cần đạt</h2>
      <ul>{product.outcomes.map((outcome) => <li key={outcome}>{outcome}</li>)}</ul>
    </article>
  );
}
