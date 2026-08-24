const terminal = (status, trace, extra = {}) => ({ status, trace, ...extra });

export class AgentHarness {
  constructor({
    model,
    registry,
    allowedTools,
    approve,
    maxSteps = 8,
    maxToolCalls = 4,
    maxRetries = 1
  }) {
    this.model = model;
    this.registry = registry;
    this.allowedTools = new Set(allowedTools);
    this.approve = approve;
    this.maxSteps = maxSteps;
    this.maxToolCalls = maxToolCalls;
    this.maxRetries = maxRetries;
  }

  async run(task) {
    const trace = [{ event: "task.started", task }];
    const observations = [];
    let toolCalls = 0;

    for (let step = 1; step <= this.maxSteps; step += 1) {
      let proposal;
      try {
        proposal = await this.model.next({
          task,
          step,
          observations: structuredClone(observations),
          tools: this.registry.schemas(this.allowedTools)
        });
      } catch (error) {
        trace.push({ event: "model.failed", step, message: error.message });
        return terminal("failed", trace, { reason: "model_error" });
      }

      trace.push({ event: "model.proposed", step, type: proposal?.type });
      if (proposal?.type === "final") {
        trace.push({ event: "task.completed", step });
        return terminal("completed", trace, { output: proposal.output });
      }
      if (proposal?.type !== "tool" || !proposal.name || !proposal.callId) {
        trace.push({ event: "proposal.invalid", step });
        return terminal("failed", trace, { reason: "invalid_proposal" });
      }

      const tool = this.registry.get(proposal.name);
      if (!tool || !this.allowedTools.has(proposal.name)) {
        trace.push({ event: "tool.denied", step, tool: proposal.name });
        return terminal("denied", trace, { reason: "tool_not_allowed" });
      }
      if (toolCalls >= this.maxToolCalls) {
        trace.push({ event: "budget.exhausted", step, budget: "tool_calls" });
        return terminal("budget_exhausted", trace, { reason: "max_tool_calls" });
      }

      if (tool.approvalRequired) {
        const approved = this.approve ? await this.approve({ proposal, tool, task }) : false;
        trace.push({ event: approved ? "approval.granted" : "approval.required", step, tool: tool.name });
        if (!approved) return terminal("human_required", trace, { reason: "approval_required" });
      }

      toolCalls += 1;
      let attempt = 0;
      while (true) {
        attempt += 1;
        try {
          const result = await tool.execute(proposal.args, { task, callId: proposal.callId });
          const observation = { callId: proposal.callId, tool: tool.name, result };
          observations.push(observation);
          trace.push({ event: "tool.succeeded", step, tool: tool.name, attempt });
          break;
        } catch (error) {
          trace.push({ event: "tool.failed", step, tool: tool.name, attempt, transient: error.transient === true });
          if (error.transient !== true || attempt > this.maxRetries) {
            return terminal("failed", trace, { reason: "tool_error" });
          }
        }
      }
    }

    trace.push({ event: "budget.exhausted", budget: "steps" });
    return terminal("budget_exhausted", trace, { reason: "max_steps" });
  }
}
