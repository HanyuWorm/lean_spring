import { getProducts } from "@/lib/catalog";

export async function GET(request: Request) {
  const query = new URL(request.url).searchParams.get("q")?.slice(0, 80) ?? "";
  return Response.json({ data: await getProducts(query) });
}
