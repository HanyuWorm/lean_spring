import type { Task, TaskRepository } from "../domain/task.js";

export class InMemoryTaskRepository implements TaskRepository {
  readonly #tasks = new Map<string, Task>();

  async findAll(): Promise<readonly Task[]> {
    return [...this.#tasks.values()].map((task) => ({ ...task }));
  }

  async save(task: Task): Promise<void> {
    this.#tasks.set(task.id, { ...task });
  }
}
