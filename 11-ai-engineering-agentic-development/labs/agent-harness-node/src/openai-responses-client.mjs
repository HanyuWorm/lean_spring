// Optional live adapter. The offline tests deliberately do not instantiate it.
export class OpenAIResponsesModel {
  #previousResponseId;
  #pendingCallId;

  constructor({ apiKey = process.env.OPENAI_API_KEY, model = process.env.AI_MODEL, fetchImpl = fetch } = {}) {
    if (!apiKey || !model) throw new Error("OPENAI_API_KEY and AI_MODEL are required");
    this.apiKey = apiKey;
    this.model = model;
    this.fetchImpl = fetchImpl;
  }

  async next({ task, observations, tools }) {
    const latest = observations.at(-1);
    const input = this.#pendingCallId && latest
      ? [{ type: "function_call_output", call_id: this.#pendingCallId, output: JSON.stringify(latest.result) }]
      : task;
    const body = {
      model: this.model,
      input,
      tools: tools.map((tool) => ({ type: "function", ...tool, strict: true })),
      ...(this.#previousResponseId ? { previous_response_id: this.#previousResponseId } : {})
    };
    const response = await this.fetchImpl("https://api.openai.com/v1/responses", {
      method: "POST",
      headers: { Authorization: `Bearer ${this.apiKey}`, "Content-Type": "application/json" },
      body: JSON.stringify(body)
    });
    if (!response.ok) throw new Error(`Responses API failed: ${response.status}`);
    const data = await response.json();
    this.#previousResponseId = data.id;
    const call = data.output?.find((item) => item.type === "function_call");
    if (call) {
      this.#pendingCallId = call.call_id;
      return { type: "tool", callId: call.call_id, name: call.name, args: JSON.parse(call.arguments) };
    }
    this.#pendingCallId = undefined;
    const output = data.output
      ?.flatMap((item) => item.content ?? [])
      .filter((item) => item.type === "output_text")
      .map((item) => item.text)
      .join("") ?? "";
    return { type: "final", output };
  }
}
