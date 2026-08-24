import test from "node:test";
import assert from "node:assert/strict";
import { finalAnswer, toolCall, TransientToolError } from "../src/contracts.mjs";
import { ScriptedModel } from "../src/fake-model.mjs";
import { AgentHarness } from "../src/orchestrator.mjs";
import { ToolRegistry } from "../src/tool-registry.mjs";

const harness = (script, tool, options = {}) => {
  const registry = new ToolRegistry().register(tool);
  return new AgentHarness({ model: new ScriptedModel(script), registry, allowedTools: [tool.name], ...options });
};

test("executes an allowed read tool and completes", async () => {
  const agent = harness(
    [toolCall("read", { id: 1 }, "c1"), finalAnswer("done")],
    { name: "read", description: "read", execute: async ({ id }) => ({ id }) }
  );
  const result = await agent.run("read one");
  assert.equal(result.status, "completed");
  assert.equal(result.output, "done");
  assert.ok(result.trace.some((event) => event.event === "tool.succeeded"));
});

test("denies a tool outside the allowlist", async () => {
  const registry = new ToolRegistry().register({ name: "admin", description: "admin", execute: async () => true });
  const agent = new AgentHarness({ model: new ScriptedModel([toolCall("admin", {}, "c2")]), registry, allowedTools: [] });
  assert.equal((await agent.run("try admin")).status, "denied");
});

test("requires approval before side effect", async () => {
  let executed = false;
  const agent = harness(
    [toolCall("refund", { amount: 10 }, "c3")],
    { name: "refund", description: "refund", sideEffect: true, execute: async () => { executed = true; } }
  );
  const result = await agent.run("refund");
  assert.equal(result.status, "human_required");
  assert.equal(executed, false);
});

test("retries only a transient tool error within budget", async () => {
  let attempts = 0;
  const agent = harness(
    [toolCall("unstable", {}, "c4"), finalAnswer("recovered")],
    {
      name: "unstable",
      description: "sometimes fails",
      execute: async () => {
        attempts += 1;
        if (attempts === 1) throw new TransientToolError("temporary");
        return "ok";
      }
    },
    { maxRetries: 1 }
  );
  assert.equal((await agent.run("retry")).status, "completed");
  assert.equal(attempts, 2);
});

test("stops when tool-call budget is exhausted", async () => {
  const agent = harness(
    [toolCall("read", {}, "c5"), toolCall("read", {}, "c6")],
    { name: "read", description: "read", execute: async () => true },
    { maxToolCalls: 1 }
  );
  const result = await agent.run("loop");
  assert.equal(result.status, "budget_exhausted");
  assert.equal(result.reason, "max_tool_calls");
});
