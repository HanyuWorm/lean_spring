import Link from "next/link";
import { Suspense } from "react";
import { FavoriteButton } from "@/app/ui/favorite-button";
import { getProducts } from "@/lib/catalog";

interface HomePageProps {
  readonly searchParams: Promise<{ q?: string | string[] }>;
}

async function SearchableCatalog({ searchParams }: HomePageProps) {
  const rawQuery = (await searchParams).q;
  const query = typeof rawQuery === "string" ? rawQuery.slice(0, 80) : "";
  const products = await getProducts(query);

  return (
    <>
      <section className="searchArea">
        <form className="search" action="/" method="get">
          <label htmlFor="q">Tìm chủ đề</label>
          <div>
            <input id="q" name="q" defaultValue={query} placeholder="node, api, next..." />
            <button type="submit">Tìm</button>
          </div>
        </form>
      </section>

      <section className="catalog" aria-labelledby="catalog-title">
        <div className="sectionHeading">
          <h2 id="catalog-title">Learning modules</h2>
          <span>{products.length} kết quả</span>
        </div>
        <div className="grid">
          {products.map((product) => (
            <article className="card" key={product.id}>
              <div>
                <span className="level">{product.level}</span>
                <h3><Link href={`/products/${product.id}`}>{product.name}</Link></h3>
                <p>{product.summary}</p>
              </div>
              <FavoriteButton productId={product.id} />
            </article>
          ))}
        </div>
      </section>
    </>
  );
}

export default function HomePage({ searchParams }: HomePageProps) {
  return (
    <>
      <section className="hero">
        <p className="eyebrow">SERVER-FIRST, CLIENT WHEN NEEDED</p>
        <h1>Học ranh giới của Next.js bằng một catalog nhỏ.</h1>
        <p className="lede">
          Trang và dữ liệu render trên server. Chỉ nút yêu thích là Client Component.
        </p>
      </section>
      <Suspense fallback={<p className="catalogStatus">Đang tải catalog…</p>}>
        <SearchableCatalog searchParams={searchParams} />
      </Suspense>
    </>
  );
}
