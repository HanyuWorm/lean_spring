import type { Metadata } from "next";
import Link from "next/link";
import type { ReactNode } from "react";
import "./styles.css";

export const metadata: Metadata = {
  title: { default: "Node Atlas", template: "%s · Node Atlas" },
  description: "A small Next.js 16 Server Components learning lab",
};

export default function RootLayout({ children }: Readonly<{ children: ReactNode }>) {
  return (
    <html lang="vi">
      <body>
        <header className="siteHeader">
          <Link className="brand" href="/">Node Atlas</Link>
          <span className="badge">Next.js 16 lab</span>
        </header>
        <main>{children}</main>
      </body>
    </html>
  );
}
