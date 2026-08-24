export class ScriptedModel {
  #script;

  constructor(script) {
    this.#script = [...script];
  }

  async next() {
    if (this.#script.length === 0) throw new Error("Fake model script exhausted");
    const item = this.#script.shift();
    return typeof item === "function" ? item() : item;
  }
}
