data-replication

## Exercise 8 > Data Replication

In this exercise we replicate the `ScoresRepository` using Akka data replicaton. In order to do this, we will focus on changing the following:

- `application.conf`:
    - Increase the `nr-of-instances` for the `scores-repository` routees to 10.
- `ScoresRepository.scala`:
    - Replace the local scores storage with a suitable `ReplicatedData` type.
    - Communicate with the `Replicator` in order to update scores.
    - Subscribe to the score changes in order to log the changed scores at `info`
    - Uncomment the logic to ask for scores from the outside

We have taken our final step in making Akka Collect **reactive**. Let's test our solution.

- Use the `sj` command alias to bootstrap `ShareJournal`.
- Open a second terminal window and start another `sbt` session:
    - Use the `sr` command alias to bootstrap `ScoresRepository`.
- Open a third terminal window and start another `sbt` session:
    - Use the `sr2` command alias to bootstrap another `ScoresRepository`.
- Open a fourth terminal window and start another `sbt` session:
    - Use the `ge` command alias to bootstrap the `GameEngine`.
- Open a fifth terminal window and start another `sbt` session:
    - Use the `pr` command alias to bootstrap the `PlayerRegistry`.
    - From the `PlayerRegistry` prompt, create two `simple` players.
- Open a sixth terminal window and start another `sbt` session:
    - Use the `pr2` command alias to bootstrap another `PlayerRegistry`.
    - From the `PlayerRegistry` prompt, create two `simple` players.
- **QUIZ**: If you tried to create players with the same name as a previous exercise:
    - Why did the `PlayerRegistry` tell you they already **exist**?
- Open up a seventh terminal session:
    - `tail -f akka-collect.log` and verify the Akka Collect is working as expected.
- Use the `test` command to verify everything works as expected.

**Congratulations!** You have completed the course!
