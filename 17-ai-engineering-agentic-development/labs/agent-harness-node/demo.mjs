import { finalAnswer, toolCall } from "./src/contracts.mjs";
import { ScriptedModel } from "./src/fake-model.mjs";
import { AgentHarness } from "./src/orchestrator.mjs";
import { ToolRegistry } from "./src/tool-registry.mjs";

const registry = new ToolRegistry().register({
  name: "lookup_order",
  description: "Read one demo order by ID",
  parameters: { type: "object", properties: { orderId: { type: "string" } }, required: ["orderId"], additionalProperties: false },
  execute: async ({ orderId }) => ({ orderId, status: "PAID" })
});

const model = new ScriptedModel([
  toolCall("lookup_order", { orderId: "ORD-42" }, "call-1"),
  finalAnswer("ORD-42 đã thanh toán.")
]);

const result = await new AgentHarness({ model, registry, allowedTools: ["lookup_order"] }).run("Kiểm tra ORD-42");
console.log(JSON.stringify(result, null, 2));
