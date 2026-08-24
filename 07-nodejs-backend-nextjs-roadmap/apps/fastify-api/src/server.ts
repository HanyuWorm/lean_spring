import { buildApp } from "./app.js";

const app = buildApp({ logger: true });
let shuttingDown = false;

async function shutdown(signal: string): Promise<void> {
  if (shuttingDown) return;
  shuttingDown = true;
  app.log.info({ signal }, "graceful shutdown started");

  const forceExit = setTimeout(() => process.exit(1), 10_000);
  forceExit.unref();
  await app.close();
  clearTimeout(forceExit);
}

process.once("SIGTERM", () => void shutdown("SIGTERM"));
process.once("SIGINT", () => void shutdown("SIGINT"));

try {
  await app.listen({ host: "0.0.0.0", port: Number(process.env.PORT ?? 3001) });
} catch (error: unknown) {
  app.log.error(error);
  process.exitCode = 1;
}
