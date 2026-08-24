export class TransientToolError extends Error {
  constructor(message) {
    super(message);
    this.name = "TransientToolError";
    this.transient = true;
  }
}

export const finalAnswer = (output) => ({ type: "final", output });

export const toolCall = (name, args = {}, callId = crypto.randomUUID()) => ({
  type: "tool",
  callId,
  name,
  args
});
