import Link from "next/link";

export default function ProductNotFound() {
  return (
    <section className="detail">
      <p className="eyebrow">404</p>
      <h1>Không tìm thấy learning module.</h1>
      <Link href="/">Về catalog</Link>
    </section>
  );
}
