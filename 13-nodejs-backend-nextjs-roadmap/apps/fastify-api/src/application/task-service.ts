import { randomUUID } from "node:crypto";
import type { CreateTaskCommand, Task, TaskRepository } from "../domain/task.js";

interface IdempotencyRecord {
  readonly fingerprint: string;
  readonly task: Task;
}

export class IdempotencyConflictError extends Error {
  constructor() {
    super("The idempotency key was already used with a different request");
    this.name = "IdempotencyConflictError";
  }
}

export class TaskService {
  readonly #idempotency = new Map<string, IdempotencyRecord>();

  constructor(
    private readonly repository: TaskRepository,
    private readonly now: () => Date = () => new Date(),
    private readonly newId: () => string = randomUUID,
  ) {}

  list(): Promise<readonly Task[]> {
    return this.repository.findAll();
  }

  async create(command: CreateTaskCommand, idempotencyKey: string): Promise<Task> {
    const fingerprint = JSON.stringify(command);
    const previous = this.#idempotency.get(idempotencyKey);

    if (previous) {
      if (previous.fingerprint !== fingerprint) throw new IdempotencyConflictError();
      return previous.task;
    }

    const task: Task = {
      id: this.newId(),
      title: command.title,
      status: "open",
      createdAt: this.now().toISOString(),
    };

    await this.repository.save(task);
    this.#idempotency.set(idempotencyKey, { fingerprint, task });
    return task;
  }
}
