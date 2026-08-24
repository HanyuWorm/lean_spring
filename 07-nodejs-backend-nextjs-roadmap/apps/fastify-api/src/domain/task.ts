export type TaskStatus = "open" | "completed";

export interface Task {
  readonly id: string;
  readonly title: string;
  readonly status: TaskStatus;
  readonly createdAt: string;
}

export interface CreateTaskCommand {
  readonly title: string;
}

export interface TaskRepository {
  findAll(): Promise<readonly Task[]>;
  save(task: Task): Promise<void>;
}
