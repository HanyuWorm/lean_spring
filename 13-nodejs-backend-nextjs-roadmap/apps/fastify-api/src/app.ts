import Fastify, { type FastifyInstance } from "fastify";
import { IdempotencyConflictError, TaskService } from "./application/task-service.js";
import { InMemoryTaskRepository } from "./infrastructure/in-memory-task-repository.js";

interface CreateTaskBody {
  title: string;
}

interface IdempotencyHeaders {
  "idempotency-key": string;
}

const taskSchema = {
  type: "object",
  additionalProperties: false,
  required: ["id", "title", "status", "createdAt"],
  properties: {
    id: { type: "string" },
    title: { type: "string" },
    status: { type: "string", enum: ["open", "completed"] },
    createdAt: { type: "string", format: "date-time" },
  },
} as const;

export interface BuildAppOptions {
  readonly logger?: boolean;
  readonly service?: TaskService;
}

export function buildApp(options: BuildAppOptions = {}): FastifyInstance {
  const app = Fastify({ logger: options.logger ?? false });
  const service = options.service ?? new TaskService(new InMemoryTaskRepository());

  app.get("/health", async () => ({ status: "up" }));

  app.get(
    "/tasks",
    { schema: { response: { 200: { type: "array", items: taskSchema } } } },
    async () => service.list(),
  );

  app.post<{ Body: CreateTaskBody; Headers: IdempotencyHeaders }>(
    "/tasks",
    {
      schema: {
        headers: {
          type: "object",
          required: ["idempotency-key"],
          properties: {
            "idempotency-key": { type: "string", minLength: 8, maxLength: 128 },
          },
        },
        body: {
          type: "object",
          additionalProperties: false,
          required: ["title"],
          properties: { title: { type: "string", minLength: 1, maxLength: 200 } },
        },
        response: { 201: taskSchema, 409: { $ref: "problem#" } },
      },
    },
    async (request, reply) => {
      try {
        const task = await service.create(request.body, request.headers["idempotency-key"]);
        return reply.code(201).send(task);
      } catch (error: unknown) {
        if (error instanceof IdempotencyConflictError) {
          return reply.code(409).send({
            code: "IDEMPOTENCY_CONFLICT",
            message: error.message,
          });
        }
        throw error;
      }
    },
  );

  app.addSchema({
    $id: "problem",
    type: "object",
    additionalProperties: false,
    required: ["code", "message"],
    properties: { code: { type: "string" }, message: { type: "string" } },
  });

  return app;
}
