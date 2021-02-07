import { Construct } from "constructs";
import { App, Stack } from "cdkactions";
import {
  createGradlePluginPullRequestWorkflow,
  createGradlePluginPrepareForReleaseWorkflow,
  createGradlePluginReleaseWorkflow,
  GradlePluginConfig
} from "@pixeloutlaw/github-cdkactions";

export class MyStack extends Stack {
  constructor(scope: Construct, id: string) {
    super(scope, id);

    // define workflows here
    const pluginConfig: GradlePluginConfig = {pluginName: "pixeloutlaw-gradle-plugin"};
    createGradlePluginPullRequestWorkflow(this, pluginConfig);
    createGradlePluginPrepareForReleaseWorkflow(this, pluginConfig);
    createGradlePluginReleaseWorkflow(this);
  }
}

const app = new App();
new MyStack(app, 'cdk');
app.synth();
