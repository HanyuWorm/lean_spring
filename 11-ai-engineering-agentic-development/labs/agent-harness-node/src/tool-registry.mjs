export class ToolRegistry {
  #tools = new Map();

  register(definition) {
    const { name, description, parameters = { type: "object" }, execute } = definition;
    if (!name || !description || typeof execute !== "function") {
      throw new TypeError("Tool requires name, description and execute");
    }
    if (this.#tools.has(name)) throw new Error(`Duplicate tool: ${name}`);
    this.#tools.set(name, {
      ...definition,
      parameters,
      sideEffect: definition.sideEffect ?? false,
      approvalRequired: definition.approvalRequired ?? definition.sideEffect ?? false
    });
    return this;
  }

  get(name) {
    return this.#tools.get(name);
  }

  schemas(allowedTools) {
    return [...this.#tools.values()]
      .filter((tool) => allowedTools.has(tool.name))
      .map(({ name, description, parameters }) => ({ name, description, parameters }));
  }
}
