cluster-singleton

## Exercise 5 > Cluster Singleton

In this exercise we use a **Cluster Singleton** to make the `PlayerRegistry` strictly consistent and highly available. In order to do this, we will focus on changing the following:

- `GameEngine.scala` (reference the cluster singleton):
    - Remove the listening to **cluster events**.
    - Use a **cluster singleton** proxy instead.
    - **HINT** factory method (createPlayerRegistry) for testing
    - Pass the proxy to `Tournament`.
- `PlayerRegistryApp.scala` (setup the cluster singleton):
    - Create a **cluster singleton** manager for `PlayerRegistry` with a role of `player-registry`.
    - You will need to use `ClusterSingletonManager` and `ClusterSingletonProxy`.
- `Tournament.scala`:
    - **HINT**: `playerRegistry` is no longer an `ActorSelection`.

Because the `PlayerRegistry` is crucial to Akka Collect, we have made it consistent as well as available. Now it is time to test our solution.

- Use the `ge` command alias to bootstrap `GameEngine`.
- Open a second terminal window and start another `sbt` session:
    - Use the `sr` command alias to bootstrap the `ScoresRepository`.
- Open a third terminal window and start another `sbt` session:
    - Use the `pr` command alias to bootstrap the `PlayerRegistry`.
    - From the `PlayerRegistry` prompt, create two `simple` players.
- Open up a fourth terminal session:
    - `tail -f akka-collect.log` and verify the Akka Collect is working as expected.
- Use the `test` command to verify everything works as expected.
- Use the `koan next` command to move to the next exercise.
